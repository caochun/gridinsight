package com.gridinsight.domain.model;

/**
 * 指标类型枚举
 * 定义电力行业数字化管控指标的基本类型
 */
public enum MetricType {
    /**
     * 基础指标 - 直接从外部数据源获取的指标
     */
    BASIC("基础指标"),
    
    /**
     * 派生指标 - 通过计算得出的复合指标
     */
    DERIVED("派生指标");
    
    private final String displayName;
    
    MetricType(String displayName) {
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
