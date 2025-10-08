package com.gridinsight.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 派生指标类
 * 继承自基础指标类，用于表示通过计算得出的复合指标
 */
public class DerivedIndicator extends Indicator {
    
    /**
     * 算法表达式，如 "(A + B) / 2"
     */
    private String formula;
    
    /**
     * 所依赖的指标列表
     */
    private List<Indicator> dependencies;
    
    /**
     * 依赖指标数量
     */
    private int dependencyCount;
    
    /**
     * 公式是否有效
     */
    private boolean validFormula;
    
    /**
     * 构造函数
     */
    public DerivedIndicator() {
        super();
        this.dependencies = new ArrayList<>();
        // 派生指标的类型固定为派生指标
        this.setIndicatorType(IndicatorType.派生指标);
    }
    
    /**
     * 全参构造函数
     * @param name 指标名称
     * @param category 指标分类
     * @param subCategory 指标子分类
     * @param unit 指标单位
     * @param formula 算法表达式
     * @param dependencies 依赖的指标列表
     */
    public DerivedIndicator(String name, String category, String subCategory, 
                           String unit, String formula, List<Indicator> dependencies) {
        super(name, category, subCategory, IndicatorType.派生指标, unit);
        this.formula = formula;
        this.dependencies = dependencies != null ? new ArrayList<>(dependencies) : new ArrayList<>();
    }
    
    /**
     * 添加依赖指标
     * @param indicator 要添加的依赖指标
     */
    public void addDependency(Indicator indicator) {
        if (indicator != null && !dependencies.contains(indicator)) {
            dependencies.add(indicator);
        }
    }
    
    /**
     * 移除依赖指标
     * @param indicator 要移除的依赖指标
     */
    public void removeDependency(Indicator indicator) {
        dependencies.remove(indicator);
    }
    
    /**
     * 检查是否依赖指定指标
     * @param indicator 要检查的指标
     * @return true 如果依赖该指标
     */
    public boolean dependsOn(Indicator indicator) {
        return dependencies.contains(indicator);
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
     * 这里可以添加更复杂的公式验证逻辑
     * @return true 如果公式格式正确
     */
    public boolean isValidFormula() {
        if (formula == null || formula.trim().isEmpty()) {
            return false;
        }
        // 基本验证：检查是否包含依赖指标的引用
        for (Indicator dependency : dependencies) {
            if (!formula.contains(dependency.getName())) {
                return false;
            }
        }
        return true;
    }
    
    public void setDependencyCount(int dependencyCount) {
        this.dependencyCount = dependencyCount;
    }
    
    public void setValidFormula(boolean validFormula) {
        this.validFormula = validFormula;
    }
    
    // Getter 和 Setter 方法
    
    public String getFormula() {
        return formula;
    }
    
    public void setFormula(String formula) {
        this.formula = formula;
    }
    
    public List<Indicator> getDependencies() {
        return new ArrayList<>(dependencies);
    }
    
    public void setDependencies(List<Indicator> dependencies) {
        this.dependencies = dependencies != null ? new ArrayList<>(dependencies) : new ArrayList<>();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DerivedIndicator that = (DerivedIndicator) o;
        return Objects.equals(formula, that.formula) &&
               Objects.equals(dependencies, that.dependencies);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), formula, dependencies);
    }
    
    @Override
    public String toString() {
        return "DerivedIndicator{" +
               "name='" + getName() + '\'' +
               ", category='" + getCategory() + '\'' +
               ", subCategory='" + getSubCategory() + '\'' +
               ", unit='" + getUnit() + '\'' +
               ", formula='" + formula + '\'' +
               ", dependencies=" + dependencies.size() + " items" +
               '}';
    }
}
