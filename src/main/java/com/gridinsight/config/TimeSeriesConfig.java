package com.gridinsight.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 时序数据库配置
 */
@Configuration
@ConfigurationProperties(prefix = "timeseries")
public class TimeSeriesConfig {
    
    /**
     * 数据库类型 (influxdb, timescaledb, clickhouse)
     */
    private String type = "influxdb";
    
    /**
     * 数据库连接URL
     */
    private String url = "http://localhost:8086";
    
    /**
     * 数据库名称
     */
    private String database = "gridinsight_metrics";
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 连接超时时间（毫秒）
     */
    private int connectTimeout = 10000;
    
    /**
     * 读取超时时间（毫秒）
     */
    private int readTimeout = 30000;
    
    /**
     * 批量写入大小
     */
    private int batchSize = 1000;
    
    /**
     * 批量写入间隔（毫秒）
     */
    private int batchInterval = 1000;

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public int getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(int connectTimeout) { this.connectTimeout = connectTimeout; }
    
    public int getReadTimeout() { return readTimeout; }
    public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }
    
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    
    public int getBatchInterval() { return batchInterval; }
    public void setBatchInterval(int batchInterval) { this.batchInterval = batchInterval; }
}
