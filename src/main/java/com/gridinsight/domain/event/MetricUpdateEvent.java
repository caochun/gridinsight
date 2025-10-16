package com.gridinsight.domain.event;

import java.time.LocalDateTime;

/**
 * 指标更新事件
 * 当指标更新时触发，用于通知依赖的派生指标进行更新
 */
public class MetricUpdateEvent {
    
    /**
     * 更新的指标标识符
     */
    private final String metricIdentifier;
    
    /**
     * 指标类型（基础指标或派生指标）
     */
    private final String metricType;
    
    /**
     * 更新后的指标值
     */
    private final Object metricValue;
    
    /**
     * 更新时间
     */
    private final LocalDateTime updateTime;
    
    /**
     * 事件源（哪个服务触发的更新）
     */
    private final String eventSource;

    public MetricUpdateEvent(String metricIdentifier, String metricType, 
                           Object metricValue, LocalDateTime updateTime, String eventSource) {
        this.metricIdentifier = metricIdentifier;
        this.metricType = metricType;
        this.metricValue = metricValue;
        this.updateTime = updateTime;
        this.eventSource = eventSource;
    }

    // Getters
    public String getMetricIdentifier() { return metricIdentifier; }
    public String getMetricType() { return metricType; }
    public Object getMetricValue() { return metricValue; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public String getEventSource() { return eventSource; }

    @Override
    public String toString() {
        return "MetricUpdateEvent{" +
               "metricIdentifier='" + metricIdentifier + '\'' +
               ", metricType='" + metricType + '\'' +
               ", updateTime=" + updateTime +
               ", eventSource='" + eventSource + '\'' +
               '}';
    }
}
