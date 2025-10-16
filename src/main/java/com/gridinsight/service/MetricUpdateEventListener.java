package com.gridinsight.service;

import com.gridinsight.domain.event.MetricUpdateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 指标更新事件监听器
 * 监听指标更新事件，自动触发依赖的派生指标更新
 */
@Component
public class MetricUpdateEventListener {

    @Autowired
    private EventDrivenMetricUpdateService eventDrivenUpdateService;

    /**
     * 监听指标更新事件
     */
    @EventListener
    @Async
    public void handleMetricUpdateEvent(MetricUpdateEvent event) {
        System.out.println("📡 接收到指标更新事件: " + event);
        
        // 处理指标更新事件，触发依赖的派生指标更新
        eventDrivenUpdateService.handleMetricUpdateEvent(event);
    }
}
