package com.gridinsight.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * 存储用的简化版指标值，只包含核心字段
 */
public class StoredMetricValue {
    
    @JsonProperty("value")
    private Double value;
    
    @JsonProperty("timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonProperty("quality")
    private MetricValue.DataQuality quality;
    
    public StoredMetricValue() {}
    
    public StoredMetricValue(Double value, LocalDateTime timestamp, MetricValue.DataQuality quality) {
        this.value = value;
        this.timestamp = timestamp;
        this.quality = quality;
    }
    
    // Getters and Setters
    public Double getValue() {
        return value;
    }
    
    public void setValue(Double value) {
        this.value = value;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public MetricValue.DataQuality getQuality() {
        return quality;
    }
    
    public void setQuality(MetricValue.DataQuality quality) {
        this.quality = quality;
    }
    
    @Override
    public String toString() {
        return "StoredMetricValue{" +
                "value=" + value +
                ", timestamp=" + timestamp +
                ", quality=" + quality +
                '}';
    }
}
