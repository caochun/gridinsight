package com.gridinsight.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 指标值类
 * 表示指标在特定时间点的数值
 */
public class MetricValue {
    
    /**
     * 指标标识符
     */
    private String metricIdentifier;
    
    /**
     * 指标数值
     */
    private Double value;
    
    /**
     * 数值单位
     */
    private String unit;
    
    /**
     * 数据时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 数据质量状态
     */
    public enum DataQuality {
        GOOD("良好"),
        WARNING("警告"),
        ERROR("错误"),
        UNKNOWN("未知");
        
        private final String displayName;
        
        DataQuality(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 数据质量
     */
    private DataQuality quality;
    
    /**
     * 数据来源
     */
    private String dataSource;
    
    /**
     * 默认构造函数
     */
    public MetricValue() {
        this.timestamp = LocalDateTime.now();
        this.quality = DataQuality.GOOD;
    }
    
    /**
     * 构造函数
     * @param metricIdentifier 指标标识符
     * @param value 指标数值
     * @param unit 数值单位
     */
    public MetricValue(String metricIdentifier, Double value, String unit) {
        this();
        this.metricIdentifier = metricIdentifier;
        this.value = value;
        this.unit = unit;
    }
    
    /**
     * 构造函数（包含时间戳）
     * @param metricIdentifier 指标标识符
     * @param value 指标数值
     * @param unit 数值单位
     * @param timestamp 数据时间戳
     */
    public MetricValue(String metricIdentifier, Double value, String unit, LocalDateTime timestamp) {
        this(metricIdentifier, value, unit);
        this.timestamp = timestamp;
    }
    
    /**
     * 构造函数（包含质量状态）
     * @param metricIdentifier 指标标识符
     * @param value 指标数值
     * @param unit 数值单位
     * @param timestamp 数据时间戳
     * @param quality 数据质量
     */
    public MetricValue(String metricIdentifier, Double value, String unit, 
                       LocalDateTime timestamp, DataQuality quality) {
        this(metricIdentifier, value, unit, timestamp);
        this.quality = quality;
    }
    
    /**
     * 创建良好质量的指标值
     * @param metricIdentifier 指标标识符
     * @param value 指标数值
     * @param unit 数值单位
     * @return 指标值
     */
    public static MetricValue good(String metricIdentifier, Double value, String unit) {
        return new MetricValue(metricIdentifier, value, unit, LocalDateTime.now(), DataQuality.GOOD);
    }
    
    /**
     * 创建警告质量的指标值
     * @param metricIdentifier 指标标识符
     * @param value 指标数值
     * @param unit 数值单位
     * @return 指标值
     */
    public static MetricValue warning(String metricIdentifier, Double value, String unit) {
        return new MetricValue(metricIdentifier, value, unit, LocalDateTime.now(), DataQuality.WARNING);
    }
    
    /**
     * 创建错误质量的指标值
     * @param metricIdentifier 指标标识符
     * @param errorMessage 错误信息
     * @return 指标值
     */
    public static MetricValue error(String metricIdentifier, String errorMessage) {
        MetricValue metricValue = new MetricValue();
        metricValue.metricIdentifier = metricIdentifier;
        metricValue.value = null;
        metricValue.unit = null;
        metricValue.timestamp = LocalDateTime.now();
        metricValue.quality = DataQuality.ERROR;
        metricValue.dataSource = "Error: " + errorMessage;
        return metricValue;
    }
    
    // Getter 和 Setter 方法
    
    public String getMetricIdentifier() {
        return metricIdentifier;
    }
    
    public void setMetricIdentifier(String metricIdentifier) {
        this.metricIdentifier = metricIdentifier;
    }
    
    public Double getValue() {
        return value;
    }
    
    public void setValue(Double value) {
        this.value = value;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public DataQuality getQuality() {
        return quality;
    }
    
    public void setQuality(DataQuality quality) {
        this.quality = quality;
    }
    
    public String getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * 检查数值是否有效
     * @return true 如果数值有效
     */
    public boolean isValid() {
        return value != null && quality != DataQuality.ERROR;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricValue that = (MetricValue) o;
        return Objects.equals(metricIdentifier, that.metricIdentifier) &&
               Objects.equals(value, that.value) &&
               Objects.equals(unit, that.unit) &&
               Objects.equals(timestamp, that.timestamp) &&
               quality == that.quality;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(metricIdentifier, value, unit, timestamp, quality);
    }
    
    @Override
    public String toString() {
        return "MetricValue{" +
               "metricIdentifier='" + metricIdentifier + '\'' +
               ", value=" + value +
               ", unit='" + unit + '\'' +
               ", timestamp=" + timestamp +
               ", quality=" + quality +
               ", dataSource='" + dataSource + '\'' +
               '}';
    }
}
