package com.gridinsight.service;

import com.gridinsight.domain.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 指标调度服务
 * 根据refreshInterval定期更新指标值并存储到时序数据库
 */
@Service
public class MetricSchedulerService {

    @Autowired
    private MetricConfigService metricConfigService;
    
    @Autowired
    private DataSourceService dataSourceService;
    
    @Autowired
    private TimeSeriesDataService timeSeriesDataService;
    
    @Autowired
    private EventDrivenMetricUpdateService eventDrivenUpdateService;
    
    @Autowired
    private MetricEventPublisher metricEventPublisher;
    

    // 指标最后更新时间记录
    private final Map<String, LocalDateTime> lastUpdateTimes = new ConcurrentHashMap<>();
    
    // 指标更新计数器
    private final Map<String, AtomicLong> updateCounters = new ConcurrentHashMap<>();

    /**
     * 定时任务：每1秒检查需要更新的指标
     */
    @Scheduled(fixedRate = 1000) // 每1秒执行一次
    public void scheduleMetricUpdates() {
        LocalDateTime now = LocalDateTime.now();
        
        // 获取所有基础指标
        Map<String, BasicMetric> basicMetrics = metricConfigService.getAllBasicMetrics();
        
        // 调试日志：显示调度器正在运行
        System.out.println("调度器运行中，当前时间: " + now + ", 基础指标数量: " + basicMetrics.size());
        
        for (Map.Entry<String, BasicMetric> entry : basicMetrics.entrySet()) {
            String identifier = entry.getKey();
            BasicMetric metric = entry.getValue();
            DataSource dataSource = metric.getDataSource();
            
            if (dataSource != null && dataSource.getEnabled()) {
                // 根据数据源类型决定更新策略
                if (dataSource.isActiveDataSource()) {
                    // 主动获取类数据源：检查刷新间隔
                    if (shouldUpdateMetric(identifier, dataSource.getRefreshInterval(), now)) {
                        updateActiveDataSourceMetric(identifier, metric);
                    }
                } else if (dataSource.isPassiveDataSource()) {
                    // 被动订阅类数据源：检查采样间隔
                    if (shouldUpdateMetric(identifier, dataSource.getSamplingInterval(), now)) {
                        updatePassiveDataSourceMetric(identifier, metric);
                    }
                }
            }
        }
        
        // 🎯 改进：派生指标现在通过事件驱动自动更新，这里只处理定时计算策略
        Map<String, DerivedMetric> derivedMetrics = metricConfigService.getAllDerivedMetrics();
        
        for (Map.Entry<String, DerivedMetric> entry : derivedMetrics.entrySet()) {
            String identifier = entry.getKey();
            DerivedMetric metric = entry.getValue();
            
            // 只处理定时计算策略，依赖驱动策略通过事件自动处理
            if (metric.getUpdateStrategy() == DerivedMetricUpdateStrategy.SCHEDULED) {
                if (shouldUpdateMetric(identifier, metric.getCalculationInterval(), now)) {
                    updateDerivedMetricAsync(identifier, metric);
                }
            }
            // REALTIME 和 DEPENDENCY_DRIVEN 策略不需要在这里处理
        }
    }

    /**
     * 检查指标是否需要更新
     */
    private boolean shouldUpdateMetric(String identifier, Integer refreshInterval, LocalDateTime now) {
        LocalDateTime lastUpdate = lastUpdateTimes.get(identifier);
        
        if (lastUpdate == null) {
            return true; // 首次更新
        }
        
        // 检查是否超过了刷新间隔
        return lastUpdate.plusSeconds(refreshInterval).isBefore(now);
    }


    /**
     * 更新主动获取类数据源指标
     */
    @Async
    public void updateActiveDataSourceMetric(String identifier, BasicMetric metric) {
        try {
            System.out.println("开始更新主动获取类指标: " + identifier);
            
            // 直接从数据源获取数据
            MetricValue value = dataSourceService.fetchData(metric.getDataSource());
            
            if (value.isValid()) {
                // 设置正确的标识符
                value.setMetricIdentifier(identifier);
                
                // 获取旧值用于比较
                MetricValue oldValue = timeSeriesDataService.getLatestMetricValue(identifier);
                Double oldValueDouble = (oldValue != null && oldValue.isValid()) ? oldValue.getValue() : null;
                Double newValueDouble = value.getValue();
                
                // 直接存储到时序数据库
                timeSeriesDataService.storeMetricValue(identifier, value, LocalDateTime.now());
                
                // 更新最后更新时间
                lastUpdateTimes.put(identifier, LocalDateTime.now());
                
                // 更新计数器
                updateCounters.computeIfAbsent(identifier, k -> new AtomicLong(0)).incrementAndGet();
                
                // 检查值是否发生变化，如果变化则发布事件
                if (oldValueDouble == null) {
                    // 首次设置值
                    metricEventPublisher.publishFirstValue(identifier, metric.getUuid(), newValueDouble);
                } else if (!oldValueDouble.equals(newValueDouble)) {
                    // 值发生变化
                    metricEventPublisher.publishValueChanged(identifier, metric.getUuid(), 
                                                           oldValueDouble, newValueDouble);
                }
                
                // 发布指标更新事件，触发依赖的派生指标更新（保持向后兼容）
                eventDrivenUpdateService.publishMetricUpdateEvent(identifier, value.getValue(), "BASIC_METRIC_UPDATE");
                
                System.out.println("主动获取类指标更新成功: " + identifier + ", 值: " + value.getValue());
            } else {
                System.out.println("主动获取类指标更新失败: " + identifier + ", 错误: " + value.getQuality());
            }
            
        } catch (Exception e) {
            System.out.println("主动获取类指标更新异常: " + identifier + ", 错误: " + e.getMessage());
        }
    }
    
    /**
     * 更新被动订阅类数据源指标
     */
    @Async
    public void updatePassiveDataSourceMetric(String identifier, BasicMetric metric) {
        try {
            System.out.println("开始更新被动订阅类指标: " + identifier);
            
            // 对于MQTT等被动订阅类数据源，这里应该从订阅的数据流中采样
            // 目前先模拟从数据源获取数据（实际应该从MQTT订阅缓存中获取）
            MetricValue value = dataSourceService.fetchData(metric.getDataSource());
            
            if (value.isValid()) {
                // 设置正确的标识符
                value.setMetricIdentifier(identifier);
                
                // 获取旧值用于比较
                MetricValue oldValue = timeSeriesDataService.getLatestMetricValue(identifier);
                Double oldValueDouble = (oldValue != null && oldValue.isValid()) ? oldValue.getValue() : null;
                Double newValueDouble = value.getValue();
                
                // 直接存储到时序数据库
                timeSeriesDataService.storeMetricValue(identifier, value, LocalDateTime.now());
                
                // 更新最后更新时间
                lastUpdateTimes.put(identifier, LocalDateTime.now());
                
                // 更新计数器
                updateCounters.computeIfAbsent(identifier, k -> new AtomicLong(0)).incrementAndGet();
                
                // 检查值是否发生变化，如果变化则发布事件
                if (oldValueDouble == null) {
                    // 首次设置值
                    metricEventPublisher.publishFirstValue(identifier, metric.getUuid(), newValueDouble);
                } else if (!oldValueDouble.equals(newValueDouble)) {
                    // 值发生变化
                    metricEventPublisher.publishValueChanged(identifier, metric.getUuid(), 
                                                           oldValueDouble, newValueDouble);
                }
                
                // 发布指标更新事件，触发依赖的派生指标更新（保持向后兼容）
                eventDrivenUpdateService.publishMetricUpdateEvent(identifier, value.getValue(), "BASIC_METRIC_UPDATE");
                
                System.out.println("被动订阅类指标更新成功: " + identifier + ", 值: " + value.getValue());
            } else {
                System.out.println("被动订阅类指标更新失败: " + identifier + ", 错误: " + value.getQuality());
            }
            
        } catch (Exception e) {
            System.out.println("被动订阅类指标更新异常: " + identifier + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 异步更新派生指标值
     * 注意：派生指标的计算仍然需要通过计算服务，因为需要从时序数据库读取依赖指标的值
     */
    @Async
    public void updateDerivedMetricAsync(String identifier, DerivedMetric metric) {
        try {
            System.out.println("开始更新派生指标: " + identifier);
            
            // 派生指标需要从时序数据库读取依赖指标的值进行计算
            // 这里暂时保留通过计算服务的逻辑，但未来可以优化为直接从时序数据库读取
            // 目前先跳过，因为需要重构计算服务
            
            System.out.println("派生指标更新跳过（需要重构计算服务）: " + identifier);
            
        } catch (Exception e) {
            System.out.println("派生指标更新异常: " + identifier + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 手动触发指标更新
     */
    public void triggerMetricUpdate(String identifier) {
        Metric metric = metricConfigService.getMetric(identifier);
        if (metric instanceof BasicMetric) {
            BasicMetric basicMetric = (BasicMetric) metric;
            DataSource dataSource = basicMetric.getDataSource();
            if (dataSource != null && dataSource.getEnabled()) {
                if (dataSource.isActiveDataSource()) {
                    updateActiveDataSourceMetric(identifier, basicMetric);
                } else if (dataSource.isPassiveDataSource()) {
                    updatePassiveDataSourceMetric(identifier, basicMetric);
                }
            }
        } else if (metric instanceof DerivedMetric) {
            updateDerivedMetricAsync(identifier, (DerivedMetric) metric);
        } else {
            // 未知的指标类型
        }
    }

    /**
     * 批量触发指标更新
     */
    public void triggerBatchMetricUpdate(List<String> identifiers) {
        for (String identifier : identifiers) {
            triggerMetricUpdate(identifier);
        }
    }

    /**
     * 获取指标更新统计信息
     */
    public Map<String, Object> getUpdateStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMetrics", lastUpdateTimes.size());
        stats.put("lastUpdateTimes", new HashMap<>(lastUpdateTimes));
        stats.put("updateCounters", updateCounters.entrySet().stream()
            .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().get()), HashMap::putAll));
        
        // 计算最近更新的指标
        LocalDateTime now = LocalDateTime.now();
        long recentlyUpdated = lastUpdateTimes.values().stream()
            .mapToLong(lastUpdate -> lastUpdate.plusMinutes(5).isAfter(now) ? 1 : 0)
            .sum();
        stats.put("recentlyUpdated", recentlyUpdated);
        
        return stats;
    }

    /**
     * 获取需要更新的指标列表
     */
    public List<String> getPendingUpdates() {
        List<String> pending = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        Map<String, BasicMetric> basicMetrics = metricConfigService.getAllBasicMetrics();
        for (Map.Entry<String, BasicMetric> entry : basicMetrics.entrySet()) {
            String identifier = entry.getKey();
            BasicMetric metric = entry.getValue();
            DataSource dataSource = metric.getDataSource();
            
            if (dataSource != null && dataSource.getEnabled()) {
                if (shouldUpdateMetric(identifier, dataSource.getRefreshInterval(), now)) {
                    pending.add(identifier);
                }
            }
        }
        
        return pending;
    }

    /**
     * 启动时初始化所有指标
     */
    public void initializeMetrics() {
        // 初始化指标调度服务
        Map<String, BasicMetric> basicMetrics = metricConfigService.getAllBasicMetrics();
        
        for (String identifier : basicMetrics.keySet()) {
            // 立即更新一次
            triggerMetricUpdate(identifier);
        }
        
        // 指标调度服务初始化完成
    }
}
