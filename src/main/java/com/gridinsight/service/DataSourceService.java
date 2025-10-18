package com.gridinsight.service;

import com.gridinsight.domain.model.DataSource;
import com.gridinsight.domain.model.MetricValue;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        
        try {
            MetricValue result = fetchDataFromSource(dataSource);
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
        try {
            String url = dataSource.getSourceAddress();
            if (url == null || url.isEmpty()) {
                return MetricValue.error("", "HTTP API地址未配置");
            }
            
            // 创建HTTP客户端
            RestTemplate restTemplate = new RestTemplate();
            
            // 设置超时时间
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            factory.setConnectTimeout(5000); // 5秒连接超时
            factory.setReadTimeout(10000);   // 10秒读取超时
            restTemplate.setRequestFactory(factory);
            
            // 处理URL中的时间戳参数
            String finalUrl = url.replace("${timestamp}", String.valueOf(System.currentTimeMillis()));
            
            // 发送HTTP请求
            ResponseEntity<String> response = restTemplate.getForEntity(finalUrl, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 解析响应数据
                String responseBody = response.getBody().trim();
                Double value = Double.parseDouble(responseBody);
                return MetricValue.good("", value, "");
            } else {
                return MetricValue.error("", "HTTP API返回错误状态: " + response.getStatusCode());
            }
            
        } catch (NumberFormatException e) {
            return MetricValue.error("", "HTTP API返回数据格式错误: " + e.getMessage());
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
        try {
            String connectionString = dataSource.getConfig("connectionString", String.class);
            String username = dataSource.getConfig("username", String.class);
            String password = dataSource.getConfig("password", String.class);
            String query = dataSource.getConfig("query", String.class);
            String driver = dataSource.getConfig("driver", String.class);
            
            if (connectionString == null || query == null) {
                return MetricValue.error("", "数据库配置不完整");
            }
            
            // 加载数据库驱动
            if (driver != null) {
                Class.forName(driver);
            }
            
            try (Connection conn = DriverManager.getConnection(connectionString, username, password);
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                if (rs.next()) {
                    Double value = rs.getDouble(1);
                    return MetricValue.good("", value, "");
                } else {
                    return MetricValue.error("", "数据库查询无结果");
                }
            }
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
