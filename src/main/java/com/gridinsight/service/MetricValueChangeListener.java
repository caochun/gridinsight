package com.gridinsight.service;

import com.gridinsight.domain.event.MetricValueChangedEvent;
import com.gridinsight.domain.model.DerivedMetric;
import com.gridinsight.domain.model.Metric;
import com.gridinsight.domain.model.MetricValue;
import com.gridinsight.domain.service.MetricCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 指标值变化事件监听器
 * 监听基础指标值变化事件，触发相关派生指标的重新计算
 */
@Service
public class MetricValueChangeListener {
    
    @Autowired
    private ExternalMetricConfigService metricConfigService;
    
    @Autowired
    private TimeSeriesDataService timeSeriesDataService;
    
    @Autowired
    private MetricCalculationService metricCalculationService;
    
    /**
     * 监听指标值变化事件
     * @param event 指标值变化事件
     */
    @EventListener
    @Async
    public void handleMetricValueChanged(MetricValueChangedEvent event) {
        System.out.println("收到指标值变化事件: " + event);
        
        // 查找依赖此基础指标的所有派生指标
        List<DerivedMetric> dependentMetrics = findDependentDerivedMetrics(event.getMetricIdentifier());
        
        for (DerivedMetric derivedMetric : dependentMetrics) {
            try {
                System.out.println("开始重新计算派生指标: " + derivedMetric.getIdentifier());
                
                // 重新计算派生指标
                MetricValue newValue = metricCalculationService.calculateMetric(derivedMetric.getIdentifier());
                
                if (newValue != null && newValue.isValid()) {
                    // 保存到时序数据库
                    timeSeriesDataService.storeMetricValue(
                        derivedMetric.getIdentifier(), 
                        newValue, 
                        LocalDateTime.now()
                    );
                    
                    System.out.println("派生指标计算完成: " + derivedMetric.getIdentifier() + 
                                     ", 新值: " + newValue.getValue());
                } else {
                    System.out.println("派生指标计算失败: " + derivedMetric.getIdentifier() + 
                                     ", 错误: " + (newValue != null ? newValue.getQuality() : "计算返回null"));
                }
                
            } catch (Exception e) {
                System.out.println("派生指标计算异常: " + derivedMetric.getIdentifier() + 
                                 ", 错误: " + e.getMessage());
            }
        }
    }
    
    /**
     * 查找依赖指定基础指标的所有派生指标
     * @param basicMetricIdentifier 基础指标标识符
     * @return 依赖的派生指标列表
     */
    private List<DerivedMetric> findDependentDerivedMetrics(String basicMetricIdentifier) {
        List<DerivedMetric> dependentMetrics = new java.util.ArrayList<>();
        
        // 获取所有派生指标
        Map<String, DerivedMetric> allDerivedMetrics = metricConfigService.getAllDerivedMetrics();
        
        for (DerivedMetric derivedMetric : allDerivedMetrics.values()) {
            // 检查是否依赖此基础指标
            if (isDependentOn(derivedMetric, basicMetricIdentifier)) {
                dependentMetrics.add(derivedMetric);
            }
        }
        
        return dependentMetrics;
    }
    
    /**
     * 检查派生指标是否依赖指定的基础指标
     * @param derivedMetric 派生指标
     * @param basicMetricIdentifier 基础指标标识符
     * @return 是否依赖
     */
    private boolean isDependentOn(DerivedMetric derivedMetric, String basicMetricIdentifier) {
        if (derivedMetric.getDependencies() == null) {
            return false;
        }
        
        for (Metric dependency : derivedMetric.getDependencies()) {
            if (basicMetricIdentifier.equals(dependency.getIdentifier())) {
                return true;
            }
        }
        
        return false;
    }
}
