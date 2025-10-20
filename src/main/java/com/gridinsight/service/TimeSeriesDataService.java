package com.gridinsight.service;

import com.gridinsight.domain.model.MetricValue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 时序数据服务接口
 * 定义指标的时序数据存储和查询规范
 * 支持多种实现：JSON文件、MapTSDB等
 */
public interface TimeSeriesDataService {

    /**
     * 存储指标值到时序数据库
     * @param metricIdentifier 指标标识符
     * @param value 指标值
     * @param timestamp 时间戳
     */
    void storeMetricValue(String metricIdentifier, MetricValue value, LocalDateTime timestamp);

    /**
     * 清空所有时序数据 (仅用于测试)
     */
    void clearAllData();

    /**
     * 批量存储指标值
     * @param values 指标值映射
     * @param timestamp 时间戳
     */
    void storeMetricValues(Map<String, MetricValue> values, LocalDateTime timestamp);

    /**
     * 查询最新指标值
     * @param metricIdentifier 指标标识符
     * @return 最新指标值
     */
    MetricValue getLatestMetricValue(String metricIdentifier);

    /**
     * 查询指标历史数据
     * @param metricIdentifier 指标标识符
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史数据列表
     */
    List<MetricValue> getMetricHistory(String metricIdentifier, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 批量查询最新指标值
     * @param metricIdentifiers 指标标识符列表
     * @return 指标值映射
     */
    Map<String, MetricValue> getLatestMetricValues(List<String> metricIdentifiers);

    /**
     * 获取指标统计信息
     * @param metricIdentifier 指标标识符
     * @param timeRange 时间范围
     * @return 统计信息（平均值、最大值、最小值等）
     */
    Map<String, Object> getMetricStatistics(String metricIdentifier, String timeRange);

    /**
     * 获取存储统计信息
     * @return 存储统计信息
     */
    Map<String, Object> getStorageStats();
}
