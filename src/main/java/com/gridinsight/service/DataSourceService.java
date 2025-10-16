package com.gridinsight.service;

import com.gridinsight.domain.model.DataSource;
import com.gridinsight.domain.model.MetricValue;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 数据源服务
 * 负责从各种数据源获取指标数据
 */
@Service
public class DataSourceService {
    
    private final Random random = new Random();
    private final Map<String, MetricValue> dataCache = new HashMap<>();
    
    /**
     * 根据数据源配置获取数据
     * @param dataSource 数据源配置
     * @return 指标值
     */
    public MetricValue fetchData(DataSource dataSource) {
        if (dataSource == null || !dataSource.getEnabled()) {
            return MetricValue.error("", "数据源未配置或已禁用");
        }
        
        // 检查缓存
        String cacheKey = dataSource.getSourceAddress();
        MetricValue cachedValue = dataCache.get(cacheKey);
        if (cachedValue != null && isCacheValid(cachedValue)) {
            return cachedValue;
        }
        
        try {
            MetricValue result = fetchDataFromSource(dataSource);
            
            // 缓存结果
            if (result.isValid()) {
                dataCache.put(cacheKey, result);
            }
            
            return result;
            
        } catch (Exception e) {
            return MetricValue.error("", "数据获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据数据源类型获取数据
     */
    private MetricValue fetchDataFromSource(DataSource dataSource) {
        switch (dataSource.getSourceType()) {
            case HTTP_API:
                return fetchFromHttpApi(dataSource);
            case MQTT:
                return fetchFromMqtt(dataSource);
            case DATABASE:
                return fetchFromDatabase(dataSource);
            case FILE:
                return fetchFromFile(dataSource);
            default:
                return MetricValue.error("", "不支持的数据源类型: " + dataSource.getSourceType());
        }
    }
    
    /**
     * 从HTTP API获取数据
     */
    private MetricValue fetchFromHttpApi(DataSource dataSource) {
        // 模拟HTTP API调用
        try {
            // 这里应该实现真实的HTTP调用
            // 暂时返回模拟数据
            Double value = generateMockValue(dataSource);
            return MetricValue.good("", value, "");
        } catch (Exception e) {
            return MetricValue.error("", "HTTP API调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 从MQTT获取数据
     */
    private MetricValue fetchFromMqtt(DataSource dataSource) {
        // 模拟MQTT数据订阅
        try {
            Double value = generateMockValue(dataSource);
            return MetricValue.good("", value, "");
        } catch (Exception e) {
            return MetricValue.error("", "MQTT数据获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 从数据库获取数据
     */
    private MetricValue fetchFromDatabase(DataSource dataSource) {
        // 模拟数据库查询
        try {
            Double value = generateMockValue(dataSource);
            return MetricValue.good("", value, "");
        } catch (Exception e) {
            return MetricValue.error("", "数据库查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 从文件获取数据
     */
    private MetricValue fetchFromFile(DataSource dataSource) {
        // 模拟文件读取
        try {
            Double value = generateMockValue(dataSource);
            return MetricValue.good("", value, "");
        } catch (Exception e) {
            return MetricValue.error("", "文件读取失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成模拟数据
     */
    private Double generateMockValue(DataSource dataSource) {
        String address = dataSource.getSourceAddress();
        
        if (address.contains("transformer-total")) {
            // 配变总数：1000-5000个
            return 1000 + random.nextDouble() * 4000;
        } else if (address.contains("inconsistent-count")) {
            // 拓扑不一致数量：0-50个
            return random.nextDouble() * 50;
        } else if (address.contains("total-count")) {
            // 低压用户总数：10000-100000户
            return 10000 + random.nextDouble() * 90000;
        } else if (address.contains("incorrect-count")) {
            // 变户关系错误数量：0-200户
            return random.nextDouble() * 200;
        } else {
            // 默认生成0-1000的随机值
            return random.nextDouble() * 1000;
        }
    }
    
    /**
     * 检查缓存是否有效
     */
    private boolean isCacheValid(MetricValue cachedValue) {
        // 简单的缓存策略：5分钟内的数据认为有效
        return cachedValue.getTimestamp().isAfter(
            java.time.LocalDateTime.now().minusMinutes(5));
    }
    
    /**
     * 清除缓存
     */
    public void clearCache() {
        dataCache.clear();
    }
    
    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", dataCache.size());
        stats.put("timestamp", System.currentTimeMillis());
        return stats;
    }
}
