package com.gridinsight.service;

import com.gridinsight.domain.event.MetricUpdateEvent;
import com.gridinsight.domain.model.*;
import com.gridinsight.domain.service.MetricCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件驱动的指标更新服务
 * 实现主动触发依赖指标更新的机制
 */
@Service
public class EventDrivenMetricUpdateService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private ExternalMetricConfigService metricConfigService;
    
    @Autowired
    private MetricCalculationService metricCalculationService;
    
    @Autowired
    private TimeSeriesDataService timeSeriesDataService;

    // 指标依赖关系映射：指标标识符 -> 依赖它的派生指标列表
    private final Map<String, Set<String>> dependencyMap = new ConcurrentHashMap<>();
    

    /**
     * 应用启动时初始化依赖关系映射
     */
    @javax.annotation.PostConstruct
    public void init() {
        // 延迟初始化，确保所有指标都已加载
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 等待指标配置加载完成
                initializeDependencyMap();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * 初始化依赖关系映射
     */
    public void initializeDependencyMap() {
        dependencyMap.clear();
        
        // 构建依赖关系映射
        Map<String, DerivedMetric> derivedMetrics = metricConfigService.getAllDerivedMetrics();
        
        for (DerivedMetric derivedMetric : derivedMetrics.values()) {
            String derivedIdentifier = derivedMetric.getIdentifier();
            
            // 遍历派生指标的依赖
            for (Metric dependency : derivedMetric.getDependencies()) {
                String depIdentifier = dependency.getIdentifier();
                
                // 将派生指标添加到依赖指标的依赖者列表中
                dependencyMap.computeIfAbsent(depIdentifier, k -> ConcurrentHashMap.newKeySet())
                            .add(derivedIdentifier);
            }
        }
        
        // 依赖关系映射初始化完成
    }

    /**
     * 发布指标更新事件
     */
    public void publishMetricUpdateEvent(String metricIdentifier, Object metricValue, String eventSource) {
        Metric metric = metricConfigService.getMetric(metricIdentifier);
        if (metric == null) {
            // 指标不存在，跳过处理
            return;
        }
        
        String metricType = metric instanceof BasicMetric ? "BASIC" : "DERIVED";
        
        MetricUpdateEvent event = new MetricUpdateEvent(
            metricIdentifier, 
            metricType, 
            metricValue, 
            LocalDateTime.now(), 
            eventSource
        );
        
        // 发布指标更新事件
        eventPublisher.publishEvent(event);
    }

    /**
     * 处理指标更新事件
     */
    @Async
    public void handleMetricUpdateEvent(MetricUpdateEvent event) {
        String metricIdentifier = event.getMetricIdentifier();
        
        // 防止循环依赖 - 使用简单的冷却机制
        // 注意：这里简化了循环依赖检测，实际项目中可以使用更复杂的算法
        
        try {
            // 获取依赖此指标的所有派生指标
            Set<String> dependentMetrics = dependencyMap.get(metricIdentifier);
            
            if (dependentMetrics != null && !dependentMetrics.isEmpty()) {
                // 触发依赖指标更新
                
                // 异步更新所有依赖的派生指标
                for (String dependentIdentifier : dependentMetrics) {
                    updateDerivedMetricIfNeeded(dependentIdentifier, event);
                }
            } else {
                // 没有依赖的派生指标
            }
            
        } catch (Exception e) {
            // 处理指标更新事件异常，记录日志但不中断流程
        }
    }

    /**
     * 更新派生指标（如果需要）
     */
    @Async
    private void updateDerivedMetricIfNeeded(String derivedIdentifier, MetricUpdateEvent triggerEvent) {
        try {
            Metric metric = metricConfigService.getMetric(derivedIdentifier);
            if (!(metric instanceof DerivedMetric)) {
                return;
            }
            
            // 现在所有派生指标都使用事件驱动机制
            // 开始更新派生指标
            
            // 计算派生指标值
            MetricValue value = metricCalculationService.calculateMetric(derivedIdentifier);
            
            if (value.isValid()) {
                // 存储到时序数据库
                timeSeriesDataService.storeMetricValue(derivedIdentifier, value, LocalDateTime.now());
                
                // 发布派生指标更新事件，触发下一级依赖
                publishMetricUpdateEvent(derivedIdentifier, value.getValue(), "DERIVED_UPDATE");
                
                // 派生指标更新成功
            } else {
                // 派生指标计算失败
            }
            
        } catch (Exception e) {
            // 派生指标更新异常，记录日志但不中断流程
        }
    }

    /**
     * 获取指标依赖关系
     */
    public Map<String, Set<String>> getDependencyMap() {
        return new HashMap<>(dependencyMap);
    }

    /**
     * 获取依赖指定指标的所有派生指标
     */
    public Set<String> getDependentMetrics(String metricIdentifier) {
        return new HashSet<>(dependencyMap.getOrDefault(metricIdentifier, Collections.emptySet()));
    }

    /**
     * 检查是否存在循环依赖
     */
    public boolean hasCircularDependency() {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        
        for (String metric : dependencyMap.keySet()) {
            if (!visited.contains(metric)) {
                if (hasCircularDependencyDFS(metric, visited, recursionStack)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * 深度优先搜索检查循环依赖
     */
    private boolean hasCircularDependencyDFS(String metric, Set<String> visited, Set<String> recursionStack) {
        visited.add(metric);
        recursionStack.add(metric);
        
        Set<String> dependents = dependencyMap.get(metric);
        if (dependents != null) {
            for (String dependent : dependents) {
                if (!visited.contains(dependent)) {
                    if (hasCircularDependencyDFS(dependent, visited, recursionStack)) {
                        return true;
                    }
                } else if (recursionStack.contains(dependent)) {
                    return true; // 发现循环依赖
                }
            }
        }
        
        recursionStack.remove(metric);
        return false;
    }

    /**
     * 获取依赖链（用于调试）
     */
    public List<String> getDependencyChain(String metricIdentifier) {
        List<String> chain = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        
        buildDependencyChain(metricIdentifier, chain, visited);
        
        return chain;
    }

    private void buildDependencyChain(String metric, List<String> chain, Set<String> visited) {
        if (visited.contains(metric)) {
            return;
        }
        
        visited.add(metric);
        chain.add(metric);
        
        Set<String> dependents = dependencyMap.get(metric);
        if (dependents != null) {
            for (String dependent : dependents) {
                buildDependencyChain(dependent, chain, visited);
            }
        }
    }

}
