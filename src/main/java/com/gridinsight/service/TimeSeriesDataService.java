package com.gridinsight.service;

import com.gridinsight.domain.model.MetricValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 时序数据服务
 * 负责指标的时序数据存储和查询
 * 基于JSON文件实现高性能本地存储
 */
@Service
public class TimeSeriesDataService {

    @Autowired
    private JsonTimeSeriesDataService jsonTimeSeriesService;

    /**
     * 存储指标值到时序数据库
     * @param metricIdentifier 指标标识符
     * @param value 指标值
     * @param timestamp 时间戳
     */
    public void storeMetricValue(String metricIdentifier, MetricValue value, LocalDateTime timestamp) {
        jsonTimeSeriesService.storeMetricValue(metricIdentifier, value, timestamp);
    }

    /**
     * 清空所有时序数据 (仅用于测试)
     */
    public void clearAllData() {
        jsonTimeSeriesService.clearAllData();
    }

    /**
     * 批量存储指标值
     * @param values 指标值映射
     * @param timestamp 时间戳
     */
    public void storeMetricValues(Map<String, MetricValue> values, LocalDateTime timestamp) {
        jsonTimeSeriesService.storeMetricValues(values, timestamp);
    }

    /**
     * 查询最新指标值
     * @param metricIdentifier 指标标识符
     * @return 最新指标值
     */
    public MetricValue getLatestMetricValue(String metricIdentifier) {
        return jsonTimeSeriesService.getLatestMetricValue(metricIdentifier);
    }

    /**
     * 查询指标历史数据
     * @param metricIdentifier 指标标识符
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史数据列表
     */
    public List<MetricValue> getMetricHistory(String metricIdentifier, LocalDateTime startTime, LocalDateTime endTime) {
        return jsonTimeSeriesService.getMetricHistory(metricIdentifier, startTime, endTime);
    }

    /**
     * 批量查询最新指标值
     * @param metricIdentifiers 指标标识符列表
     * @return 指标值映射
     */
    public Map<String, MetricValue> getLatestMetricValues(List<String> metricIdentifiers) {
        return jsonTimeSeriesService.getLatestMetricValues(metricIdentifiers);
    }

    /**
     * 获取指标统计信息
     * @param metricIdentifier 指标标识符
     * @param timeRange 时间范围
     * @return 统计信息（平均值、最大值、最小值等）
     */
    public Map<String, Object> getMetricStatistics(String metricIdentifier, String timeRange) {
        return jsonTimeSeriesService.getMetricStatistics(metricIdentifier, timeRange);
    }

    /**
     * 获取存储统计信息
     * @return 存储统计信息
     */
    public Map<String, Object> getStorageStats() {
        return jsonTimeSeriesService.getStorageStats();
    }
}
