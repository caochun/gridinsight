package com.gridinsight.domain.model;

/**
 * 基础指标类
 * 继承自Metric，表示从外部数据源直接获取的指标
 */
public class BasicMetric extends Metric {
    
    /**
     * 数据来源配置
     */
    private DataSource dataSource;
    
    /**
     * 默认构造函数
     */
    public BasicMetric() {
        super();
        this.setMetricType(MetricType.BASIC);
    }
    
    /**
     * 基础指标构造函数
     * @param name 指标名称
     * @param category 指标分类
     * @param subCategory 指标子分类
     * @param unit 指标单位（量纲）
     * @param description 指标描述
     * @param dataSource 数据来源配置
     */
    public BasicMetric(String name, String category, String subCategory, 
                       String unit, String description, DataSource dataSource) {
        super(name, category, subCategory, unit, description);
        this.dataSource = dataSource;
        this.setMetricType(MetricType.BASIC);
    }
    
    /**
     * 获取数据来源
     * @return 数据来源配置
     */
    public DataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * 设置数据来源
     * @param dataSource 数据来源配置
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public String toString() {
        return "BasicMetric{" +
               "name='" + getName() + '\'' +
               ", category='" + getCategory() + '\'' +
               ", subCategory='" + getSubCategory() + '\'' +
               ", unit='" + getUnit() + '\'' +
               ", identifier='" + getIdentifier() + '\'' +
               ", dataSource=" + dataSource +
               '}';
    }
}
