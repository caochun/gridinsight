package com.gridinsight.domain.model;

import java.util.Objects;

/**
 * 电力行业数字化管控指标基础类
 * 表示电力行业中的各种度量指标
 */
public class Metric {
    
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
     * 指标类型（基础指标或派生指标）
     */
    private MetricType metricType;
    
    /**
     * 指标单位（量纲）
     */
    private String unit;
    
    /**
     * 唯一标识符 - 用于唯一标识指标
     * 格式：分类.子分类.指标名称
     */
    private String identifier;
    
    /**
     * 指标描述
     */
    private String description;
    
    /**
     * 默认构造函数
     */
    public Metric() {
    }
    
    /**
     * 基础指标构造函数
     * @param name 指标名称
     * @param category 指标分类
     * @param subCategory 指标子分类
     * @param unit 指标单位（量纲）
     * @param description 指标描述
     */
    public Metric(String name, String category, String subCategory, 
                  String unit, String description) {
        this.name = name;
        this.category = category;
        this.subCategory = subCategory;
        this.unit = unit;
        this.description = description;
        this.identifier = generateIdentifier(category, subCategory, name);
        this.metricType = MetricType.BASIC;
    }
    
    /**
     * 生成唯一标识符
     * @param category 分类
     * @param subCategory 子分类
     * @param name 名称
     * @return 唯一标识符
     */
    protected String generateIdentifier(String category, String subCategory, String name) {
        return category + "." + subCategory + "." + name;
    }
    
    /**
     * 获取完整标识符
     * @return 唯一标识符
     */
    public String getIdentifier() {
        return identifier;
    }
    
    /**
     * 检查是否为派生指标
     * @return true 如果是派生指标
     */
    public boolean isDerived() {
        return metricType == MetricType.DERIVED;
    }
    
    /**
     * 检查是否为基础指标
     * @return true 如果是基础指标
     */
    public boolean isBasic() {
        return metricType == MetricType.BASIC;
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
    
    public MetricType getMetricType() {
        return metricType;
    }
    
    public void setMetricType(MetricType metricType) {
        this.metricType = metricType;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Metric metric = (Metric) o;
        return Objects.equals(identifier, metric.identifier);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }
    
    @Override
    public String toString() {
        return "Metric{" +
               "name='" + name + '\'' +
               ", category='" + category + '\'' +
               ", subCategory='" + subCategory + '\'' +
               ", metricType=" + metricType +
               ", unit='" + unit + '\'' +
               ", identifier='" + identifier + '\'' +
               ", description='" + description + '\'' +
               '}';
    }
}
