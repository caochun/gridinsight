package com.gridinsight.domain.model;

/**
 * 派生指标更新策略枚举
 */
public enum DerivedMetricUpdateStrategy {
    
    /**
     * 实时计算策略
     * 每次查询时实时计算，不预存储
     * 优点：数据总是最新的，存储空间小
     * 缺点：查询时计算开销大，响应时间较长
     */
    REALTIME("实时计算"),
    
    /**
     * 依赖驱动策略
     * 当依赖的基础指标更新时，自动计算并存储派生指标
     * 优点：数据预计算，查询响应快
     * 缺点：存储空间较大，依赖关系复杂
     */
    DEPENDENCY_DRIVEN("依赖驱动"),
    
    /**
     * 定时计算策略
     * 设置固定的计算间隔，定时计算并存储
     * 优点：计算负载可控，查询响应快
     * 缺点：数据可能不是最新的
     */
    SCHEDULED("定时计算"),
    
    /**
     * 混合策略
     * 结合实时计算和预计算，根据指标重要性选择策略
     * 优点：平衡性能和实时性
     * 缺点：实现复杂度较高
     */
    HYBRID("混合策略");

    private final String displayName;

    DerivedMetricUpdateStrategy(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
