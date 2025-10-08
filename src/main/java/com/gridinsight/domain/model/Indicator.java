package com.gridinsight.domain.model;

import java.util.Objects;

/**
 * 电力行业数字化管控指标基础类
 * 表示电力行业中的各种数字化管控指标
 */
public class Indicator {
    
    /**
     * 指标名称
     */
    private String name;
    
    /**
     * 指标分类
     */
    private String category;
    
    /**
     * 指标子分类
     */
    private String subCategory;
    
    /**
     * 指标类型
     */
    private IndicatorType indicatorType;
    
    /**
     * 指标单位
     */
    private String unit;
    
    /**
     * 数据来源 - MQTT Topic URL
     */
    private String dataSource;
    
    /**
     * 完整标识符 - 用于唯一标识指标
     */
    private String fullIdentifier;
    
    /**
     * 是否为衍生指标
     */
    private boolean derived;
    
    /**
     * 构造函数
     */
    public Indicator() {
    }
    
    /**
     * 全参构造函数
     * @param name 指标名称
     * @param category 指标分类
     * @param subCategory 指标子分类
     * @param indicatorType 指标类型
     * @param unit 指标单位
     * @param dataSource 数据来源 - MQTT Topic URL
     */
    public Indicator(String name, String category, String subCategory, 
                    IndicatorType indicatorType, String unit, String dataSource) {
        this.name = name;
        this.category = category;
        this.subCategory = subCategory;
        this.indicatorType = indicatorType;
        this.unit = unit;
        this.dataSource = dataSource;
        this.fullIdentifier = category + "." + subCategory + "." + name;
    }
    
    /**
     * 构造函数（不包含数据来源）
     * @param name 指标名称
     * @param category 指标分类
     * @param subCategory 指标子分类
     * @param indicatorType 指标类型
     * @param unit 指标单位
     */
    public Indicator(String name, String category, String subCategory, 
                    IndicatorType indicatorType, String unit) {
        this(name, category, subCategory, indicatorType, unit, null);
    }
    
    // Getter 和 Setter 方法
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getSubCategory() {
        return subCategory;
    }
    
    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }
    
    public IndicatorType getIndicatorType() {
        return indicatorType;
    }
    
    public void setIndicatorType(IndicatorType indicatorType) {
        this.indicatorType = indicatorType;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public String getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }
    
    public String getFullIdentifier() {
        return fullIdentifier;
    }
    
    public void setFullIdentifier(String fullIdentifier) {
        this.fullIdentifier = fullIdentifier;
    }
    
    public boolean isDerived() {
        return derived;
    }
    
    public void setDerived(boolean derived) {
        this.derived = derived;
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Indicator indicator = (Indicator) o;
        return Objects.equals(name, indicator.name) &&
               Objects.equals(category, indicator.category) &&
               Objects.equals(subCategory, indicator.subCategory) &&
               indicatorType == indicator.indicatorType &&
               Objects.equals(unit, indicator.unit) &&
               Objects.equals(dataSource, indicator.dataSource);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, category, subCategory, indicatorType, unit, dataSource);
    }
    
    @Override
    public String toString() {
        return "Indicator{" +
               "name='" + name + '\'' +
               ", category='" + category + '\'' +
               ", subCategory='" + subCategory + '\'' +
               ", indicatorType=" + indicatorType +
               ", unit='" + unit + '\'' +
               ", dataSource='" + dataSource + '\'' +
               '}';
    }
}
