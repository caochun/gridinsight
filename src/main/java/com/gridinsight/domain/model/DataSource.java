package com.gridinsight.domain.model;

import java.util.Objects;

/**
 * 数据源配置类
 * 定义指标数据来源的配置信息
 */
public class DataSource {
    
    /**
     * 数据源类型
     */
    public enum SourceType {
        HTTP_API("HTTP API"),
        MQTT("MQTT"),
        DATABASE("数据库"),
        FILE("文件");
        
        private final String displayName;
        
        SourceType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 数据源类型
     */
    private SourceType sourceType;
    
    /**
     * 数据源地址（URL、MQTT Topic等）
     */
    private String sourceAddress;
    
    /**
     * 数据源名称
     */
    private String sourceName;
    
    /**
     * 数据源描述
     */
    private String description;
    
    /**
     * 刷新频率（秒）
     */
    private Integer refreshInterval;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 默认构造函数
     */
    public DataSource() {
        this.enabled = true;
    }
    
    /**
     * 构造函数
     * @param sourceType 数据源类型
     * @param sourceAddress 数据源地址
     * @param sourceName 数据源名称
     * @param description 数据源描述
     */
    public DataSource(SourceType sourceType, String sourceAddress, 
                      String sourceName, String description) {
        this();
        this.sourceType = sourceType;
        this.sourceAddress = sourceAddress;
        this.sourceName = sourceName;
        this.description = description;
    }
    
    /**
     * 构造函数（包含刷新频率）
     * @param sourceType 数据源类型
     * @param sourceAddress 数据源地址
     * @param sourceName 数据源名称
     * @param description 数据源描述
     * @param refreshInterval 刷新频率（秒）
     */
    public DataSource(SourceType sourceType, String sourceAddress, 
                      String sourceName, String description, Integer refreshInterval) {
        this(sourceType, sourceAddress, sourceName, description);
        this.refreshInterval = refreshInterval;
    }
    
    /**
     * 构造函数（包含刷新频率和启用状态）
     * @param sourceType 数据源类型
     * @param sourceAddress 数据源地址
     * @param sourceName 数据源名称
     * @param refreshInterval 刷新频率（秒）
     * @param enabled 是否启用
     */
    public DataSource(SourceType sourceType, String sourceAddress, 
                      String sourceName, Integer refreshInterval, Boolean enabled) {
        this();
        this.sourceType = sourceType;
        this.sourceAddress = sourceAddress;
        this.sourceName = sourceName;
        this.refreshInterval = refreshInterval;
        this.enabled = enabled;
    }
    
    /**
     * 创建HTTP API数据源
     * @param url API地址
     * @param sourceName 数据源名称
     * @param description 数据源描述
     * @return HTTP API数据源
     */
    public static DataSource createHttpApi(String url, String sourceName, String description) {
        return new DataSource(SourceType.HTTP_API, url, sourceName, description);
    }
    
    /**
     * 创建MQTT数据源
     * @param topic MQTT主题
     * @param sourceName 数据源名称
     * @param description 数据源描述
     * @return MQTT数据源
     */
    public static DataSource createMqtt(String topic, String sourceName, String description) {
        return new DataSource(SourceType.MQTT, topic, sourceName, description);
    }
    
    // Getter 和 Setter 方法
    
    public SourceType getSourceType() {
        return sourceType;
    }
    
    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }
    
    public String getSourceAddress() {
        return sourceAddress;
    }
    
    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }
    
    public String getSourceName() {
        return sourceName;
    }
    
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getRefreshInterval() {
        return refreshInterval;
    }
    
    public void setRefreshInterval(Integer refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataSource that = (DataSource) o;
        return sourceType == that.sourceType &&
               Objects.equals(sourceAddress, that.sourceAddress) &&
               Objects.equals(sourceName, that.sourceName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sourceType, sourceAddress, sourceName);
    }
    
    @Override
    public String toString() {
        return "DataSource{" +
               "sourceType=" + sourceType +
               ", sourceAddress='" + sourceAddress + '\'' +
               ", sourceName='" + sourceName + '\'' +
               ", description='" + description + '\'' +
               ", refreshInterval=" + refreshInterval +
               ", enabled=" + enabled +
               '}';
    }
}
