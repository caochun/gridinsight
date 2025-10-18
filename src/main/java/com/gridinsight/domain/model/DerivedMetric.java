package com.gridinsight.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 派生指标类
 * 继承自Metric，表示通过计算得出的复合指标
 */
public class DerivedMetric extends Metric {
    
    /**
     * 计算公式
     * 公式中通过指标标识符引用其他指标
     * 例如：(1 - 配变挂接馈线不一致数量 / 配变总数) * 100
     */
    private String formula;
    
    /**
     * 依赖的指标列表
     * 包含基础指标和其他派生指标
     */
    private List<Metric> dependencies;
    
    // 注意：updateStrategy和calculationInterval已移除，所有派生指标现在都使用事件驱动机制
    
    /**
     * 公式是否有效
     */
    private boolean validFormula;
    
    /**
     * 默认构造函数
     */
    public DerivedMetric() {
        super();
        this.dependencies = new ArrayList<>();
        this.setMetricType(MetricType.DERIVED);
    }
    
    /**
     * 派生指标构造函数
     * @param name 指标名称
     * @param category 指标分类
     * @param subCategory 指标子分类
     * @param unit 指标单位（量纲）
     * @param description 指标描述
     * @param formula 计算公式
     * @param dependencies 依赖的指标列表
     */
    public DerivedMetric(String name, String category, String subCategory, 
                        String unit, String description, String formula, 
                        List<Metric> dependencies) {
        super(name, category, subCategory, unit, description);
        this.formula = formula;
        this.dependencies = dependencies != null ? new ArrayList<>(dependencies) : new ArrayList<>();
        this.setMetricType(MetricType.DERIVED);
        this.validFormula = validateFormula();
    }
    
    
    /**
     * 添加依赖指标
     * @param metric 要添加的依赖指标
     */
    public void addDependency(Metric metric) {
        if (metric != null && !dependencies.contains(metric)) {
            dependencies.add(metric);
            this.validFormula = validateFormula();
        }
    }
    
    /**
     * 移除依赖指标
     * @param metric 要移除的依赖指标
     */
    public void removeDependency(Metric metric) {
        if (dependencies.remove(metric)) {
            this.validFormula = validateFormula();
        }
    }
    
    /**
     * 检查是否依赖指定指标
     * @param metric 要检查的指标
     * @return true 如果依赖该指标
     */
    public boolean dependsOn(Metric metric) {
        return dependencies.contains(metric);
    }
    
    /**
     * 获取依赖指标数量
     * @return 依赖指标的数量
     */
    public int getDependencyCount() {
        return dependencies.size();
    }
    
    /**
     * 验证公式是否有效
     * 检查公式中是否包含所有依赖指标的标识符
     * @return true 如果公式格式正确
     */
    public boolean validateFormula() {
        if (formula == null || formula.trim().isEmpty()) {
            return false;
        }
        
        // 检查是否包含所有依赖指标的标识符
        for (Metric dependency : dependencies) {
            if (!formula.contains(dependency.getIdentifier())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 检查是否存在循环依赖
     * @param visited 已访问的指标集合
     * @return true 如果存在循环依赖
     */
    public boolean hasCircularDependency(List<Metric> visited) {
        if (visited == null) {
            visited = new ArrayList<>();
        }
        
        if (visited.contains(this)) {
            return true; // 发现循环依赖
        }
        
        // 创建新的列表来避免修改原始列表
        List<Metric> newVisited = new ArrayList<>(visited);
        newVisited.add(this);
        
        for (Metric dependency : dependencies) {
            if (dependency instanceof DerivedMetric) {
                DerivedMetric derivedDependency = (DerivedMetric) dependency;
                if (derivedDependency.hasCircularDependency(newVisited)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    // Getter 和 Setter 方法
    
    public String getFormula() {
        return formula;
    }
    
    public void setFormula(String formula) {
        this.formula = formula;
        this.validFormula = validateFormula();
    }
    
    public List<Metric> getDependencies() {
        return new ArrayList<>(dependencies);
    }
    
    public void setDependencies(List<Metric> dependencies) {
        this.dependencies = dependencies != null ? new ArrayList<>(dependencies) : new ArrayList<>();
        this.validFormula = validateFormula();
    }
    
    public boolean isValidFormula() {
        return validFormula;
    }
    
    public void setValidFormula(boolean validFormula) {
        this.validFormula = validFormula;
    }
    
    // updateStrategy和calculationInterval的getter/setter方法已移除，因为现在所有派生指标都使用事件驱动机制
    
    @Override
    public String toString() {
        return "DerivedMetric{" +
               "name='" + getName() + '\'' +
               ", category='" + getCategory() + '\'' +
               ", subCategory='" + getSubCategory() + '\'' +
               ", unit='" + getUnit() + '\'' +
               ", identifier='" + getIdentifier() + '\'' +
               ", formula='" + formula + '\'' +
               ", dependencies=" + dependencies.size() + " items" +
               ", validFormula=" + validFormula +
               '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DerivedMetric that = (DerivedMetric) o;
        return Objects.equals(formula, that.formula) &&
               Objects.equals(dependencies, that.dependencies);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), formula, dependencies);
    }
}
