package com.gridinsight.domain.service;

import com.gridinsight.domain.model.*;
import com.gridinsight.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 指标计算服务
 * 负责计算派生指标的值，处理指标依赖关系和缓存
 */
@Service
public class MetricCalculationService {
    
    private final Map<String, Metric> metrics = new ConcurrentHashMap<>();
    private final Map<String, MetricValue> valueCache = new ConcurrentHashMap<>();
    private final Map<String, BasicMetric> basicMetrics = new ConcurrentHashMap<>();
    private final Map<String, DerivedMetric> derivedMetrics = new ConcurrentHashMap<>();
    
    @Autowired
    private DataSourceService dataSourceService;
    
    
    /**
     * 添加指标
     * @param metric 指标
     */
    public void addMetric(Metric metric) {
        metrics.put(metric.getIdentifier(), metric);
        
        if (metric instanceof BasicMetric) {
            basicMetrics.put(metric.getIdentifier(), (BasicMetric) metric);
        } else if (metric instanceof DerivedMetric) {
            derivedMetrics.put(metric.getIdentifier(), (DerivedMetric) metric);
        }
    }

    /**
     * 手动设置基础指标值（用于测试）
     * @param metricIdentifier 指标标识符
     * @param value 指标值
     */
    public void setBasicMetricValue(String metricIdentifier, Double value) {
        if (basicMetrics.containsKey(metricIdentifier)) {
            MetricValue metricValue = MetricValue.good(metricIdentifier, value, "个");
            valueCache.put(metricIdentifier, metricValue);
            System.out.println("设置基础指标值: " + metricIdentifier + " = " + value);
        }
    }


    /**
     * 获取指标（用于测试）
     * @param metricIdentifier 指标标识符
     * @return 指标
     */
    public Metric getMetric(String metricIdentifier) {
        return metrics.get(metricIdentifier);
    }
    
    /**
     * 计算派生指标值
     * @param metricIdentifier 指标标识符
     * @return 指标值
     */
    public MetricValue calculateMetric(String metricIdentifier) {
        if (metricIdentifier == null || metricIdentifier.trim().isEmpty()) {
            return MetricValue.error("", "指标标识符不能为空");
        }
        
        // 检查缓存
        MetricValue cachedValue = valueCache.get(metricIdentifier);
        if (cachedValue != null && isCacheValid(cachedValue)) {
            return cachedValue;
        }
        
        Metric metric = metrics.get(metricIdentifier);
        if (metric == null) {
            return MetricValue.error(metricIdentifier, "指标不存在: " + metricIdentifier);
        }
        
        MetricValue result;
        if (metric instanceof BasicMetric) {
            result = calculateBasicMetric((BasicMetric) metric);
        } else if (metric instanceof DerivedMetric) {
            result = calculateDerivedMetric((DerivedMetric) metric);
        } else {
            result = MetricValue.error(metricIdentifier, "未知的指标类型");
        }
        
        // 缓存结果
        if (result.isValid()) {
            valueCache.put(metricIdentifier, result);
        }
        
        return result;
    }
    
    /**
     * 计算基础指标值
     * @param basicMetric 基础指标
     * @return 指标值
     */
    private MetricValue calculateBasicMetric(BasicMetric basicMetric) {
        try {
            DataSource dataSource = basicMetric.getDataSource();
            if (dataSource == null || !dataSource.getEnabled()) {
                return MetricValue.error(basicMetric.getIdentifier(), "数据源未配置或已禁用");
            }
            
            // 使用数据源服务获取数据
            MetricValue result = dataSourceService.fetchData(dataSource);
            if (result.isValid()) {
                result.setMetricIdentifier(basicMetric.getIdentifier());
                result.setUnit(basicMetric.getUnit());
            }
            
            return result;
            
        } catch (Exception e) {
            return MetricValue.error(basicMetric.getIdentifier(), "数据获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算派生指标值
     * @param derivedMetric 派生指标
     * @return 指标值
     */
    private MetricValue calculateDerivedMetric(DerivedMetric derivedMetric) {
        try {
            // 1. 验证公式
            FormulaParser.FormulaValidationResult validation = 
                FormulaParser.validateDerivedMetric(derivedMetric, metrics);
            
            if (!validation.isValid()) {
                return MetricValue.error(derivedMetric.getIdentifier(), 
                    "公式验证失败: " + validation.getSummary());
            }
            
            // 2. 获取公式中引用的所有指标值
            Map<String, MetricValue> metricValues = new HashMap<>();
            List<String> formulaIdentifiers = FormulaParser.extractMetricIdentifiers(derivedMetric.getFormula());
            
            for (String identifier : formulaIdentifiers) {
                MetricValue metricValue = calculateMetric(identifier);
                if (!metricValue.isValid()) {
                    return MetricValue.error(derivedMetric.getIdentifier(), 
                        "指标计算失败: " + identifier);
                }
                metricValues.put(identifier, metricValue);
            }
            
            // 3. 计算公式
            MetricValue result = FormulaEngine.calculate(derivedMetric.getFormula(), metricValues);
            
            if (result.isValid()) {
                result.setMetricIdentifier(derivedMetric.getIdentifier());
                result.setUnit(derivedMetric.getUnit());
            }
            
            return result;
            
        } catch (Exception e) {
            return MetricValue.error(derivedMetric.getIdentifier(), 
                "派生指标计算失败: " + e.getMessage());
        }
    }
    
    
    /**
     * 批量计算指标
     * @param metricIdentifiers 指标标识符列表
     * @return 指标值映射
     */
    public Map<String, MetricValue> calculateMetrics(List<String> metricIdentifiers) {
        Map<String, MetricValue> results = new HashMap<>();
        
        for (String identifier : metricIdentifiers) {
            results.put(identifier, calculateMetric(identifier));
        }
        
        return results;
    }
    
    /**
     * 获取所有指标标识符
     * @return 指标标识符列表
     */
    public List<String> getAllMetricIdentifiers() {
        return new ArrayList<>(metrics.keySet());
    }
    
    /**
     * 获取派生指标的依赖图
     * @param metricIdentifier 指标标识符
     * @return 依赖图
     */
    public FormulaParser.DependencyGraph getDependencyGraph(String metricIdentifier) {
        DerivedMetric derivedMetric = derivedMetrics.get(metricIdentifier);
        if (derivedMetric == null) {
            return null;
        }
        
        return FormulaParser.generateDependencyGraph(derivedMetric, metrics);
    }
    
    /**
     * 清除缓存
     * @param metricIdentifier 可选的指标标识符，为null时清除所有缓存
     */
    public void clearCache(String metricIdentifier) {
        if (metricIdentifier == null) {
            valueCache.clear();
        } else {
            valueCache.remove(metricIdentifier);
        }
    }
    
    /**
     * 检查缓存是否有效
     * @param cachedValue 缓存的值
     * @return true 如果缓存有效
     */
    private boolean isCacheValid(MetricValue cachedValue) {
        // 简单的缓存策略：5分钟内的数据认为有效
        return cachedValue.getTimestamp().isAfter(
            java.time.LocalDateTime.now().minusMinutes(5));
    }
    
    /**
     * 获取缓存统计信息
     * @return 缓存统计
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", valueCache.size());
        stats.put("totalMetrics", metrics.size());
        stats.put("basicMetrics", basicMetrics.size());
        stats.put("derivedMetrics", derivedMetrics.size());
        return stats;
    }
}
