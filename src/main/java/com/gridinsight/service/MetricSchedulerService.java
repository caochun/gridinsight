package com.gridinsight.service;

import com.gridinsight.domain.model.*;
import com.gridinsight.domain.service.MetricCalculationService;
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
    private MetricCalculationService metricCalculationService;
    
    @Autowired
    private TimeSeriesDataService timeSeriesDataService;
    
    @Autowired
    private EventDrivenMetricUpdateService eventDrivenUpdateService;
    

    // 指标最后更新时间记录
    private final Map<String, LocalDateTime> lastUpdateTimes = new ConcurrentHashMap<>();
    
    // 指标更新计数器
    private final Map<String, AtomicLong> updateCounters = new ConcurrentHashMap<>();

    /**
     * 定时任务：每分钟检查需要更新的指标
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void scheduleMetricUpdates() {
        LocalDateTime now = LocalDateTime.now();
        
        // 获取所有基础指标
        Map<String, BasicMetric> basicMetrics = metricConfigService.getAllBasicMetrics();
        
        for (Map.Entry<String, BasicMetric> entry : basicMetrics.entrySet()) {
            String identifier = entry.getKey();
            BasicMetric metric = entry.getValue();
            DataSource dataSource = metric.getDataSource();
            
            if (dataSource != null && dataSource.getEnabled()) {
                // 检查是否需要更新
                if (shouldUpdateMetric(identifier, dataSource.getRefreshInterval(), now)) {
                    // 异步更新指标
                    updateBasicMetricAsync(identifier, metric);
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
     * 异步更新基础指标值
     */
    @Async
    public void updateBasicMetricAsync(String identifier, BasicMetric metric) {
        try {
            // 开始更新指标
            
            // 计算指标值
            MetricValue value = metricCalculationService.calculateMetric(identifier);
            
            if (value.isValid()) {
                // 存储到时序数据库
                timeSeriesDataService.storeMetricValue(identifier, value, LocalDateTime.now());
                
                // 更新最后更新时间
                lastUpdateTimes.put(identifier, LocalDateTime.now());
                
                // 更新计数器
                updateCounters.computeIfAbsent(identifier, k -> new AtomicLong(0)).incrementAndGet();
                
                // 🎯 关键改进：发布指标更新事件，主动触发依赖的派生指标更新
                eventDrivenUpdateService.publishMetricUpdateEvent(identifier, value.getValue(), "BASIC_METRIC_UPDATE");
                
                // 指标更新成功
            } else {
                // 指标计算失败
            }
            
        } catch (Exception e) {
            // 指标更新异常，记录日志但不中断流程
        }
    }

    /**
     * 异步更新派生指标值
     */
    @Async
    public void updateDerivedMetricAsync(String identifier, DerivedMetric metric) {
        try {
            // 开始更新派生指标
            
            // 计算派生指标值
            MetricValue value = metricCalculationService.calculateMetric(identifier);
            
            if (value.isValid()) {
                // 存储到时序数据库
                timeSeriesDataService.storeMetricValue(identifier, value, LocalDateTime.now());
                
                // 更新最后更新时间
                lastUpdateTimes.put(identifier, LocalDateTime.now());
                
                // 更新计数器
                updateCounters.computeIfAbsent(identifier, k -> new AtomicLong(0)).incrementAndGet();
                
                // 派生指标更新成功
            } else {
                // 派生指标计算失败
            }
            
        } catch (Exception e) {
            // 派生指标更新异常，记录日志但不中断流程
        }
    }

    /**
     * 手动触发指标更新
     */
    public void triggerMetricUpdate(String identifier) {
        Metric metric = metricConfigService.getMetric(identifier);
        if (metric instanceof BasicMetric) {
            updateBasicMetricAsync(identifier, (BasicMetric) metric);
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
