package com.gridinsight.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gridinsight.domain.model.MetricValue;
import com.gridinsight.domain.model.StoredMetricValue;
import com.gridinsight.service.MetricConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 基于JSON文件存储的时序数据服务
 * 提供高性能的本地时序数据存储和查询
 */
@Service
public class JsonTimeSeriesDataService {

    @Value("${gridinsight.timeseries.data-path:data/timeseries}")
    private String dataPath;

    @Autowired
    private MetricConfigService metricConfigService;

    // 每个指标的历史数据存储（使用简化格式）
    private final Map<String, List<StoredMetricValue>> metricHistory = new ConcurrentHashMap<>();
    
    // 每个指标的最新值缓存
    private final Map<String, MetricValue> latestValues = new ConcurrentHashMap<>();
    
    // 数据根目录
    private Path dataRootPath;
    
    // JSON序列化器
    private final ObjectMapper objectMapper;
    
    // 读写锁
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public JsonTimeSeriesDataService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void init() {
        // 创建数据根目录
        dataRootPath = Paths.get(dataPath);
        try {
            Files.createDirectories(dataRootPath);
        } catch (IOException e) {
            System.err.println("创建数据目录失败: " + e.getMessage());
        }
        
        // 加载现有数据
        loadExistingData();
    }

    @PreDestroy
    public void cleanup() {
        // 保存所有数据到文件
        saveAllData();
    }

    /**
     * 获取指标数据文件路径（使用UUID作为文件名）
     */
    private Path getMetricDataPath(String metricIdentifier) {
        try {
            // 通过标识符获取UUID
            String uuid = getMetricUuid(metricIdentifier);
            if (uuid != null) {
                return dataRootPath.resolve(uuid + ".json");
            } else {
                // 如果无法获取UUID，回退到原来的方式
                String fileName = sanitizeFileName(metricIdentifier) + ".json";
                return dataRootPath.resolve(fileName);
            }
        } catch (Exception e) {
            // 如果出现异常，回退到原来的方式
            String fileName = sanitizeFileName(metricIdentifier) + ".json";
            return dataRootPath.resolve(fileName);
        }
    }

    /**
     * 根据指标标识符获取UUID
     */
    private String getMetricUuid(String metricIdentifier) {
        try {
            // 先尝试从基础指标获取
            var basicMetric = metricConfigService.getMetric(metricIdentifier);
            if (basicMetric != null) {
                return basicMetric.getUuid();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 清理文件名中的特殊字符（保留作为备用）
     */
    private String sanitizeFileName(String identifier) {
        return identifier.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * 存储指标值到时序数据库
     */
    public void storeMetricValue(String metricIdentifier, MetricValue value, LocalDateTime timestamp) {
        if (value == null || !value.isValid()) {
            System.out.println("跳过存储无效的指标值: " + metricIdentifier);
            return;
        }

        try {
            lock.writeLock().lock();
            
            // 创建简化的存储对象，只保存核心字段
            StoredMetricValue storedValue = new StoredMetricValue(
                value.getValue(),
                timestamp,
                value.getQuality()
            );
            
            // 添加到内存历史数据（使用简化格式）
            metricHistory.computeIfAbsent(metricIdentifier, k -> new ArrayList<>()).add(storedValue);
            
            // 更新最新值缓存（保持完整的MetricValue对象用于API返回）
            latestValues.put(metricIdentifier, value);
            
            // 异步保存到文件
            saveMetricData(metricIdentifier);
            
        } catch (Exception e) {
            System.err.println("存储指标值失败: " + metricIdentifier + ", 错误: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 批量存储指标值
     */
    public void storeMetricValues(Map<String, MetricValue> values, LocalDateTime timestamp) {
        for (Map.Entry<String, MetricValue> entry : values.entrySet()) {
            storeMetricValue(entry.getKey(), entry.getValue(), timestamp);
        }
    }

    /**
     * 查询最新指标值
     */
    public MetricValue getLatestMetricValue(String metricIdentifier) {
        // 先从缓存获取
        MetricValue cached = latestValues.get(metricIdentifier);
        if (cached != null) {
            return cached;
        }
        
        // 从内存历史数据获取
        List<StoredMetricValue> storedHistory = metricHistory.get(metricIdentifier);
        if (storedHistory != null && !storedHistory.isEmpty()) {
            StoredMetricValue lastStored = storedHistory.get(storedHistory.size() - 1);
            MetricValue latest = new MetricValue(
                metricIdentifier,
                lastStored.getValue(),
                "", // 单位从指标定义获取
                lastStored.getTimestamp(),
                lastStored.getQuality()
            );
            latestValues.put(metricIdentifier, latest);
            return latest;
        }
        
        // 从文件加载
        loadMetricData(metricIdentifier);
        MetricValue result = latestValues.get(metricIdentifier);
        if (result != null) {
            return result;
        }
        
        // 如果仍然没有找到数据，返回一个表示"无数据"的MetricValue
        return MetricValue.error(metricIdentifier, "指标不存在或没有数据");
    }

    /**
     * 查询指标历史数据
     */
    public List<MetricValue> getMetricHistory(String metricIdentifier, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            lock.readLock().lock();
            
            List<StoredMetricValue> storedHistory = metricHistory.get(metricIdentifier);
            if (storedHistory == null) {
                // 尝试从文件加载
                loadMetricData(metricIdentifier);
                storedHistory = metricHistory.get(metricIdentifier);
            }
            
            if (storedHistory == null) {
                return new ArrayList<>();
            }
            
            // 过滤时间范围并转换为MetricValue
            return storedHistory.stream()
                    .filter(storedValue -> {
                        LocalDateTime timestamp = storedValue.getTimestamp();
                        return timestamp != null && 
                               !timestamp.isBefore(startTime) && 
                               !timestamp.isAfter(endTime);
                    })
                    .map(storedValue -> new MetricValue(
                        metricIdentifier,
                        storedValue.getValue(),
                        "", // 单位从指标定义获取
                        storedValue.getTimestamp(),
                        storedValue.getQuality()
                    ))
                    .collect(Collectors.toList());
                    
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 批量查询最新指标值
     */
    public Map<String, MetricValue> getLatestMetricValues(List<String> metricIdentifiers) {
        Map<String, MetricValue> results = new HashMap<>();
        
        for (String identifier : metricIdentifiers) {
            results.put(identifier, getLatestMetricValue(identifier));
        }
        
        return results;
    }

    /**
     * 获取指标统计信息
     */
    public Map<String, Object> getMetricStatistics(String metricIdentifier, String timeRange) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 根据时间范围计算开始时间
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = calculateStartTime(endTime, timeRange);
            
            List<MetricValue> history = getMetricHistory(metricIdentifier, startTime, endTime);
            
            if (history.isEmpty()) {
                stats.put("count", 0);
                stats.put("average", 0.0);
                stats.put("max", 0.0);
                stats.put("min", 0.0);
                return stats;
            }
            
            // 计算统计信息
            double sum = 0.0;
            double max = Double.MIN_VALUE;
            double min = Double.MAX_VALUE;
            int validCount = 0;
            
            for (MetricValue value : history) {
                if (value.isValid() && value.getValue() != null) {
                    double val = value.getValue();
                    sum += val;
                    max = Math.max(max, val);
                    min = Math.min(min, val);
                    validCount++;
                }
            }
            
            stats.put("count", validCount);
            stats.put("average", validCount > 0 ? sum / validCount : 0.0);
            stats.put("max", max == Double.MIN_VALUE ? 0.0 : max);
            stats.put("min", min == Double.MAX_VALUE ? 0.0 : min);
            stats.put("timeRange", timeRange);
            stats.put("startTime", startTime);
            stats.put("endTime", endTime);
            
        } catch (Exception e) {
            System.err.println("计算统计信息失败: " + metricIdentifier + ", 错误: " + e.getMessage());
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    /**
     * 清空所有时序数据
     */
    public void clearAllData() {
        try {
            lock.writeLock().lock();
            
            // 清空内存数据
            metricHistory.clear();
            latestValues.clear();
            
            // 删除所有数据文件
            if (Files.exists(dataRootPath)) {
                try {
                    Files.walk(dataRootPath)
                            .filter(Files::isRegularFile)
                            .filter(path -> path.toString().endsWith(".json"))
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                } catch (IOException e) {
                                    System.err.println("删除文件失败: " + path + ", 错误: " + e.getMessage());
                                }
                            });
                } catch (IOException e) {
                    System.err.println("遍历目录失败: " + e.getMessage());
                }
            }
            
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 保存指标数据到文件（使用简化格式）
     */
    private void saveMetricData(String metricIdentifier) {
        try {
            List<StoredMetricValue> history = metricHistory.get(metricIdentifier);
            if (history != null) {
                Path filePath = getMetricDataPath(metricIdentifier);
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), history);
            }
        } catch (IOException e) {
            System.err.println("保存指标数据失败: " + metricIdentifier + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 从文件加载指标数据（兼容新旧格式）
     */
    private void loadMetricData(String metricIdentifier) {
        try {
            Path filePath = getMetricDataPath(metricIdentifier);
            if (Files.exists(filePath)) {
                // 先尝试加载简化格式
                try {
                    List<StoredMetricValue> storedHistory = objectMapper.readValue(
                        filePath.toFile(), 
                        new TypeReference<List<StoredMetricValue>>() {}
                    );
                    
                    if (storedHistory != null && !storedHistory.isEmpty()) {
                        metricHistory.put(metricIdentifier, storedHistory);
                        // 从最后一个存储值重建MetricValue用于缓存
                        StoredMetricValue lastStored = storedHistory.get(storedHistory.size() - 1);
                        MetricValue lastValue = new MetricValue(
                            metricIdentifier,
                            lastStored.getValue(),
                            "", // 单位从指标定义获取
                            lastStored.getTimestamp(),
                            lastStored.getQuality()
                        );
                        latestValues.put(metricIdentifier, lastValue);
                    }
                } catch (Exception e) {
                    // 如果简化格式加载失败，尝试旧格式
                    List<MetricValue> history = objectMapper.readValue(
                        filePath.toFile(), 
                        new TypeReference<List<MetricValue>>() {}
                    );
                    
                    if (history != null && !history.isEmpty()) {
                        // 转换为简化格式存储
                        List<StoredMetricValue> storedHistory = history.stream()
                            .map(mv -> new StoredMetricValue(mv.getValue(), mv.getTimestamp(), mv.getQuality()))
                            .collect(Collectors.toList());
                        metricHistory.put(metricIdentifier, storedHistory);
                        latestValues.put(metricIdentifier, history.get(history.size() - 1));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("加载指标数据失败: " + metricIdentifier + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 加载所有现有数据
     */
    private void loadExistingData() {
        try {
            if (Files.exists(dataRootPath)) {
                Files.walk(dataRootPath)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json"))
                        .forEach(path -> {
                            String fileName = path.getFileName().toString();
                            String metricIdentifier = fileName.substring(0, fileName.lastIndexOf('.'));
                            loadMetricData(metricIdentifier);
                        });
            }
        } catch (IOException e) {
            System.err.println("加载现有数据失败: " + e.getMessage());
        }
    }

    /**
     * 保存所有数据到文件
     */
    private void saveAllData() {
        try {
            lock.readLock().lock();
            
            for (String metricIdentifier : metricHistory.keySet()) {
                saveMetricData(metricIdentifier);
            }
            
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 根据时间范围字符串计算开始时间
     */
    private LocalDateTime calculateStartTime(LocalDateTime endTime, String timeRange) {
        if (timeRange == null || timeRange.isEmpty()) {
            return endTime.minusHours(1); // 默认1小时
        }
        
        try {
            if (timeRange.endsWith("m")) {
                int minutes = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
                return endTime.minusMinutes(minutes);
            } else if (timeRange.endsWith("h")) {
                int hours = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
                return endTime.minusHours(hours);
            } else if (timeRange.endsWith("d")) {
                int days = Integer.parseInt(timeRange.substring(0, timeRange.length() - 1));
                return endTime.minusDays(days);
            } else {
                // 尝试解析为数字（分钟）
                int minutes = Integer.parseInt(timeRange);
                return endTime.minusMinutes(minutes);
            }
        } catch (NumberFormatException e) {
            return endTime.minusHours(1); // 默认1小时
        }
    }

    /**
     * 获取存储统计信息
     */
    public Map<String, Object> getStorageStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            lock.readLock().lock();
            
            stats.put("totalMetrics", metricHistory.size());
            stats.put("cachedValues", latestValues.size());
            stats.put("dataPath", dataPath);
            stats.put("dataRootExists", Files.exists(dataRootPath));
            
            // 计算总存储大小
            long totalSize = 0;
            if (Files.exists(dataRootPath)) {
                totalSize = Files.walk(dataRootPath)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json"))
                        .mapToLong(path -> {
                            try {
                                return Files.size(path);
                            } catch (IOException e) {
                                return 0;
                            }
                        })
                        .sum();
            }
            
            stats.put("totalSizeBytes", totalSize);
            stats.put("totalSizeMB", totalSize / (1024.0 * 1024.0));
            
        } catch (IOException e) {
            stats.put("error", e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
        
        return stats;
    }
}
