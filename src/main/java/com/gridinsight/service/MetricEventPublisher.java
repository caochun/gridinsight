package com.gridinsight.service;

import com.gridinsight.domain.event.MetricValueChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 指标事件发布服务
 * 负责发布指标值变化事件
 */
@Service
public class MetricEventPublisher {
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * 发布指标值变化事件
     * @param event 指标值变化事件
     */
    public void publishMetricValueChanged(MetricValueChangedEvent event) {
        eventPublisher.publishEvent(event);
        System.out.println("发布指标值变化事件: " + event);
    }
    
    /**
     * 发布值变化事件
     * @param metricIdentifier 指标标识符
     * @param metricUuid 指标UUID
     * @param oldValue 旧值
     * @param newValue 新值
     */
    public void publishValueChanged(String metricIdentifier, String metricUuid,
                                  Double oldValue, Double newValue) {
        MetricValueChangedEvent event = MetricValueChangedEvent.valueChanged(
            metricIdentifier, metricUuid, oldValue, newValue);
        publishMetricValueChanged(event);
    }
    
    /**
     * 发布首次值事件
     * @param metricIdentifier 指标标识符
     * @param metricUuid 指标UUID
     * @param value 值
     */
    public void publishFirstValue(String metricIdentifier, String metricUuid, Double value) {
        MetricValueChangedEvent event = MetricValueChangedEvent.firstValue(
            metricIdentifier, metricUuid, value);
        publishMetricValueChanged(event);
    }
    
    /**
     * 发布错误事件
     * @param metricIdentifier 指标标识符
     * @param metricUuid 指标UUID
     * @param errorMessage 错误消息
     */
    public void publishErrorOccurred(String metricIdentifier, String metricUuid, String errorMessage) {
        MetricValueChangedEvent event = MetricValueChangedEvent.errorOccurred(
            metricIdentifier, metricUuid, errorMessage);
        publishMetricValueChanged(event);
    }
}

