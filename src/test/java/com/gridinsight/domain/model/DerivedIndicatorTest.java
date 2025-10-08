package com.gridinsight.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

/**
 * 派生指标类测试
 */
public class DerivedIndicatorTest {
    
    private DerivedIndicator derivedIndicator;
    private Indicator powerGeneration;
    private Indicator powerConsumption;
    
    @BeforeEach
    void setUp() {
        powerGeneration = new Indicator(
            "发电量",
            "发电指标",
            "发电量统计",
            IndicatorType.绝对值,
            "万千瓦时",
            "mqtt://grid-monitor.com/power/generation"
        );
        
        powerConsumption = new Indicator(
            "用电量",
            "用电指标",
            "用电量统计",
            IndicatorType.绝对值,
            "万千瓦时",
            "mqtt://grid-monitor.com/power/consumption"
        );
        
        derivedIndicator = new DerivedIndicator(
            "电力供需平衡率",
            "平衡指标",
            "供需平衡",
            "%",
            "(发电量 - 用电量) / 发电量 * 100",
            Arrays.asList(powerGeneration, powerConsumption)
        );
    }
    
    @Test
    void testDerivedIndicatorCreation() {
        assertThat(derivedIndicator.getName()).isEqualTo("电力供需平衡率");
        assertThat(derivedIndicator.getCategory()).isEqualTo("平衡指标");
        assertThat(derivedIndicator.getSubCategory()).isEqualTo("供需平衡");
        assertThat(derivedIndicator.getUnit()).isEqualTo("%");
        assertThat(derivedIndicator.getFormula()).isEqualTo("(发电量 - 用电量) / 发电量 * 100");
        assertThat(derivedIndicator.getIndicatorType()).isEqualTo(IndicatorType.派生指标);
    }
    
    @Test
    void testDependencyManagement() {
        // 测试依赖数量
        assertThat(derivedIndicator.getDependencyCount()).isEqualTo(2);
        
        // 测试依赖检查
        assertThat(derivedIndicator.dependsOn(powerGeneration)).isTrue();
        assertThat(derivedIndicator.dependsOn(powerConsumption)).isTrue();
        
        // 测试添加新依赖
        Indicator newIndicator = new Indicator("新指标", "新分类", "新子分类", IndicatorType.绝对值, "单位");
        derivedIndicator.addDependency(newIndicator);
        assertThat(derivedIndicator.getDependencyCount()).isEqualTo(3);
        assertThat(derivedIndicator.dependsOn(newIndicator)).isTrue();
        
        // 测试移除依赖
        derivedIndicator.removeDependency(newIndicator);
        assertThat(derivedIndicator.getDependencyCount()).isEqualTo(2);
        assertThat(derivedIndicator.dependsOn(newIndicator)).isFalse();
    }
    
    @Test
    void testFormulaValidation() {
        // 测试有效公式
        assertThat(derivedIndicator.isValidFormula()).isTrue();
        
        // 测试无效公式（空公式）
        derivedIndicator.setFormula("");
        assertThat(derivedIndicator.isValidFormula()).isFalse();
        
        // 测试无效公式（null）
        derivedIndicator.setFormula(null);
        assertThat(derivedIndicator.isValidFormula()).isFalse();
    }
    
    @Test
    void testIsDerived() {
        assertThat(derivedIndicator.isDerived()).isTrue();
    }
    
    @Test
    void testEqualsAndHashCode() {
        DerivedIndicator sameDerivedIndicator = new DerivedIndicator(
            "电力供需平衡率",
            "平衡指标",
            "供需平衡",
            "%",
            "(发电量 - 用电量) / 发电量 * 100",
            Arrays.asList(powerGeneration, powerConsumption)
        );
        
        assertThat(derivedIndicator).isEqualTo(sameDerivedIndicator);
        assertThat(derivedIndicator.hashCode()).isEqualTo(sameDerivedIndicator.hashCode());
    }
}
