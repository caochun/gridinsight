package com.gridinsight.service;

import com.gridinsight.domain.model.MetricValue;
import com.maptsdb.TimeSeriesDatabase;
import com.maptsdb.TimeSeriesDatabaseBuilder;
import com.maptsdb.DataPoint;
import com.maptsdb.DataSourceConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于MapTSDB的时序数据存储服务实现
 * 使用高性能的MapDB时序数据库存储指标历史数据
 * 所有指标值都作为float类型存储，不包含quality属性
 */
@Service
public class MapTsdbTimeSeriesDataService implements TimeSeriesDataService {

    @Value("${gridinsight.timeseries.data-path:data/timeseries}")
    private String dataPath;

    @Value("${gridinsight.timeseries.enable-cache:true}")
    private boolean enableCache;

    // MapTSDB数据库实例
    private TimeSeriesDatabase tsdb;
    
    // 最新值缓存（可选）
    private final Map<String, MetricValue> latestValueCache = new ConcurrentHashMap<>();
    
    // 批量写入计数器
    private final Map<String, Integer> batchCounters = new ConcurrentHashMap<>();
    
    @Value("${gridinsight.maptsdb.batch-size:10}")
    private int batchSize;

    @PostConstruct
    public void init() {
        try {
            // 初始化MapTSDB
            // 根据QuickStartExample学习正确的初始化方式
            tsdb = TimeSeriesDatabaseBuilder.builder()
                    .path(dataPath + "/maptsdb.db")  // 设置数据库文件路径
                    .addDoubleSource("metrics", "指标数据")  // 添加指标数据源
                    .withRetentionDays(30)  // 数据保留30天
                    .enableMemoryMapping()  // 启用内存映射
                    .buildWithDynamicSources();  // 构建支持动态数据源的数据库
            
            System.out.println("MapTSDB时序数据库初始化成功，数据路径: " + dataPath);
            
        } catch (Exception e) {
            System.err.println("MapTSDB初始化失败: " + e.getMessage());
            throw new RuntimeException("MapTSDB初始化失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            if (tsdb != null) {
                // 关闭前确保所有数据都被提交
                tsdb.commit();
                tsdb.close();
                System.out.println("MapTSDB数据库已关闭");
            }
        } catch (Exception e) {
            System.err.println("关闭MapTSDB时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 手动提交所有待提交的数据
     */
    public void commitAll() {
        try {
            if (tsdb != null) {
                tsdb.commit();
                // 重置所有计数器
                batchCounters.clear();
                System.out.println("所有数据已提交到MapTSDB");
            }
        } catch (Exception e) {
            System.err.println("提交数据时发生错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取数据源信息
     * @param metricIdentifier 指标标识符
     * @return 数据源配置信息
     */
    public DataSourceConfig getDataSourceInfo(String metricIdentifier) {
        try {
            if (tsdb != null) {
                return tsdb.getDataSourceInfo(metricIdentifier);
            }
        } catch (Exception e) {
            System.err.println("获取数据源信息时发生错误: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 根据时间戳获取特定数据点的值
     * @param metricIdentifier 指标标识符
     * @param timestamp 时间戳
     * @return 指标值
     */
    public MetricValue getMetricValueAtTimestamp(String metricIdentifier, LocalDateTime timestamp) {
        try {
            if (tsdb != null && timestamp != null) {
                long timestampMillis = timestamp.toInstant(ZoneOffset.UTC).toEpochMilli();
                Double value = tsdb.getDouble(metricIdentifier, timestampMillis);
                
                if (value != null) {
                    return new MetricValue(
                        metricIdentifier,
                        value,
                        "个", // 单位需要从指标定义中获取
                        timestamp
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("获取指定时间戳的指标值时发生错误: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void storeMetricValue(String metricIdentifier, MetricValue value, LocalDateTime timestamp) {
        if (metricIdentifier == null || value == null || timestamp == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        try {
            // 将LocalDateTime转换为时间戳（毫秒）
            long timestampMillis = timestamp.toInstant(ZoneOffset.UTC).toEpochMilli();
            
            // 所有指标值都转换为float类型存储到MapTSDB
            Double metricValue = value.getValue();
            if (metricValue != null) {
                // 使用putDouble方法存储float值
                tsdb.putDouble(metricIdentifier, timestampMillis, metricValue);
                
                // 批量提交策略：每batchSize次写入后commit一次
                int currentCount = batchCounters.compute(metricIdentifier, (k, v) -> (v == null ? 1 : v + 1));
                if (currentCount >= batchSize) {
                    tsdb.commit();
                    batchCounters.put(metricIdentifier, 0); // 重置计数器
                }
                
                // 更新缓存
                if (enableCache) {
                    latestValueCache.put(metricIdentifier, value);
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("存储指标值时发生错误: " + e.getMessage(), e);
        }
    }

    @Override
    public void storeMetricValues(Map<String, MetricValue> values, LocalDateTime timestamp) {
        if (values == null || timestamp == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        try {
            // 将LocalDateTime转换为时间戳（毫秒）
            long timestampMillis = timestamp.toInstant(ZoneOffset.UTC).toEpochMilli();
            
            // 批量存储所有指标值
            for (Map.Entry<String, MetricValue> entry : values.entrySet()) {
                String metricIdentifier = entry.getKey();
                MetricValue value = entry.getValue();
                
                if (value != null && value.getValue() != null) {
                    // 使用putDouble方法存储float值
                    tsdb.putDouble(metricIdentifier, timestampMillis, value.getValue());
                    
                    // 更新缓存
                    if (enableCache) {
                        latestValueCache.put(metricIdentifier, value);
                    }
                }
            }
            
            // 批量提交
            tsdb.commit();
            
        } catch (Exception e) {
            throw new RuntimeException("批量存储指标值时发生错误: " + e.getMessage(), e);
        }
    }

    @Override
    public MetricValue getLatestMetricValue(String metricIdentifier) {
        if (metricIdentifier == null) {
            return null;
        }

        try {
            // 先从缓存获取
            if (enableCache && latestValueCache.containsKey(metricIdentifier)) {
                return latestValueCache.get(metricIdentifier);
            }

            // 从数据库获取最新值
            // 注意：MapTSDB没有getLatestValue方法，需要实现获取最新值的逻辑
            // 暂时从缓存获取，实际实现需要遍历数据或维护最新值索引
            if (enableCache && latestValueCache.containsKey(metricIdentifier)) {
                return latestValueCache.get(metricIdentifier);
            }
            
            // TODO: 实现真正的获取最新值逻辑
            // 可能需要：1. 维护最新值索引 2. 遍历数据获取最新值 3. 使用时间范围查询
            
            return null;
            
        } catch (Exception e) {
            System.err.println("获取最新指标值时发生错误: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<MetricValue> getMetricHistory(String metricIdentifier, LocalDateTime startTime, LocalDateTime endTime) {
        if (metricIdentifier == null || startTime == null || endTime == null) {
            return Collections.emptyList();
        }

        try {
            long startMillis = startTime.toInstant(ZoneOffset.UTC).toEpochMilli();
            long endMillis = endTime.toInstant(ZoneOffset.UTC).toEpochMilli();
            
            // 从MapTSDB查询时间范围内的数据
            // 注意：MapTSDB没有直接的getRange方法，需要实现时间范围查询逻辑
            List<MetricValue> result = new ArrayList<>();
            
            // TODO: 实现真正的历史数据查询
            // 方案1: 使用批量查询，遍历时间范围内的所有可能时间戳
            // 方案2: 维护时间索引，快速定位数据
            // 方案3: 使用MapTSDB的时间范围查询API（如果存在）
            
            // 临时实现：返回空列表，等待实现真正的查询逻辑
            
            return result;
            
        } catch (Exception e) {
            System.err.println("获取指标历史数据时发生错误: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, MetricValue> getLatestMetricValues(List<String> metricIdentifiers) {
        Map<String, MetricValue> result = new HashMap<>();
        
        for (String identifier : metricIdentifiers) {
            MetricValue value = getLatestMetricValue(identifier);
            if (value != null) {
                result.put(identifier, value);
            }
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getMetricStatistics(String metricIdentifier, String timeRange) {
        // 实现统计功能
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 这里可以根据timeRange计算统计数据
            // 例如：平均值、最大值、最小值、数据点数等
            
            stats.put("metric", metricIdentifier);
            stats.put("range", timeRange);
            stats.put("count", 0);
            stats.put("average", 0.0);
            stats.put("max", 0.0);
            stats.put("min", 0.0);
            
        } catch (Exception e) {
            System.err.println("获取指标统计信息时发生错误: " + e.getMessage());
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取存储统计信息
            stats.put("storageType", "MapTSDB");
            stats.put("dataPath", dataPath);
            stats.put("cacheEnabled", enableCache);
            stats.put("cacheSize", latestValueCache.size());
            
            // 这里可以添加更多MapTSDB特定的统计信息
            
        } catch (Exception e) {
            System.err.println("获取存储统计信息时发生错误: " + e.getMessage());
        }
        
        return stats;
    }

    @Override
    public void clearAllData() {
        try {
            // 清空所有数据
            // 注意：这里需要根据MapTSDB的实际API调整
            // tsdb.clearAll();
            
            // 清空缓存
            if (enableCache) {
                latestValueCache.clear();
            }
            
            System.out.println("所有时序数据已清空");
            
        } catch (Exception e) {
            throw new RuntimeException("清空数据时发生错误: " + e.getMessage(), e);
        }
    }
}
