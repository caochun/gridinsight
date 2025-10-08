package com.gridinsight.domain.model;

/**
 * 指标类型枚举
 * 定义电力行业数字化管控指标的基本类型
 */
public enum IndicatorType {
    /**
     * 绝对值 - 直接测量的数值
     */
    绝对值("绝对值"),
    
    /**
     * 比率 - 两个数值的比值
     */
    比率("比率"),
    
    /**
     * 同比 - 与去年同期相比的增长率
     */
    同比("同比"),
    
    /**
     * 环比 - 与上一周期相比的增长率
     */
    环比("环比"),
    
    /**
     * 派生指标 - 通过计算得出的复合指标
     */
    派生指标("派生指标");
    
    private final String displayName;
    
    IndicatorType(String displayName) {
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
