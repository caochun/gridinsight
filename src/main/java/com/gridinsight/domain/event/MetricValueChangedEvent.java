package com.gridinsight.domain.event;

import java.time.LocalDateTime;

/**
 * 指标值变化事件
 * 当基础指标的值发生变化时发布此事件
 */
public class MetricValueChangedEvent {
    
    /**
     * 指标标识符
     */
    private String metricIdentifier;
    
    /**
     * 指标UUID
     */
    private String metricUuid;
    
    /**
     * 旧值
     */
    private Double oldValue;
    
    /**
     * 新值
     */
    private Double newValue;
    
    /**
     * 变化时间
     */
    private LocalDateTime changeTime;
    
    /**
     * 变化类型
     */
    private ChangeType changeType;
    
    /**
     * 变化类型枚举
     */
    public enum ChangeType {
        VALUE_CHANGED,  // 值发生变化
        FIRST_VALUE,    // 首次设置值
        ERROR_OCCURRED  // 发生错误
    }
    
    /**
     * 默认构造函数
     */
    public MetricValueChangedEvent() {
    }
    
    /**
     * 构造函数
     * @param metricIdentifier 指标标识符
     * @param metricUuid 指标UUID
     * @param oldValue 旧值
     * @param newValue 新值
     * @param changeTime 变化时间
     * @param changeType 变化类型
     */
    public MetricValueChangedEvent(String metricIdentifier, String metricUuid, 
                                 Double oldValue, Double newValue, 
                                 LocalDateTime changeTime, ChangeType changeType) {
        this.metricIdentifier = metricIdentifier;
        this.metricUuid = metricUuid;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changeTime = changeTime;
        this.changeType = changeType;
    }
    
    /**
     * 创建值变化事件
     */
    public static MetricValueChangedEvent valueChanged(String metricIdentifier, String metricUuid,
                                                     Double oldValue, Double newValue) {
        return new MetricValueChangedEvent(metricIdentifier, metricUuid, oldValue, newValue, 
                                         LocalDateTime.now(), ChangeType.VALUE_CHANGED);
    }
    
    /**
     * 创建首次值事件
     */
    public static MetricValueChangedEvent firstValue(String metricIdentifier, String metricUuid,
                                                   Double value) {
        return new MetricValueChangedEvent(metricIdentifier, metricUuid, null, value, 
                                         LocalDateTime.now(), ChangeType.FIRST_VALUE);
    }
    
    /**
     * 创建错误事件
     */
    public static MetricValueChangedEvent errorOccurred(String metricIdentifier, String metricUuid,
                                                       String errorMessage) {
        MetricValueChangedEvent event = new MetricValueChangedEvent();
        event.setMetricIdentifier(metricIdentifier);
        event.setMetricUuid(metricUuid);
        event.setChangeTime(LocalDateTime.now());
        event.setChangeType(ChangeType.ERROR_OCCURRED);
        return event;
    }
    
    // Getter 和 Setter 方法
    
    public String getMetricIdentifier() {
        return metricIdentifier;
    }
    
    public void setMetricIdentifier(String metricIdentifier) {
        this.metricIdentifier = metricIdentifier;
    }
    
    public String getMetricUuid() {
        return metricUuid;
    }
    
    public void setMetricUuid(String metricUuid) {
        this.metricUuid = metricUuid;
    }
    
    public Double getOldValue() {
        return oldValue;
    }
    
    public void setOldValue(Double oldValue) {
        this.oldValue = oldValue;
    }
    
    public Double getNewValue() {
        return newValue;
    }
    
    public void setNewValue(Double newValue) {
        this.newValue = newValue;
    }
    
    public LocalDateTime getChangeTime() {
        return changeTime;
    }
    
    public void setChangeTime(LocalDateTime changeTime) {
        this.changeTime = changeTime;
    }
    
    public ChangeType getChangeType() {
        return changeType;
    }
    
    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }
    
    @Override
    public String toString() {
        return "MetricValueChangedEvent{" +
               "metricIdentifier='" + metricIdentifier + '\'' +
               ", metricUuid='" + metricUuid + '\'' +
               ", oldValue=" + oldValue +
               ", newValue=" + newValue +
               ", changeTime=" + changeTime +
               ", changeType=" + changeType +
               '}';
    }
}
