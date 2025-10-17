package com.gridinsight.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;
import java.util.Arrays;
import java.util.List;

/**
 * DerivedMetric测试
 */
public class DerivedMetricTest {
    
    private DerivedMetric derivedMetric;
    private BasicMetric dependency1;
    private BasicMetric dependency2;
    
    @BeforeEach
    void setUp() {
        // 创建依赖的基础指标
        dependency1 = new BasicMetric(
            "配变总数",
            "中压拓扑",
            "配变统计",
            "个",
            "配变总数",
            DataSource.createHttpApi("http://api1.com", "GET", null, "API1", "描述1", 300)
        );
        
        dependency2 = new BasicMetric(
            "不一致数量",
            "中压拓扑", 
            "拓扑不一致",
            "个",
            "不一致数量",
            DataSource.createHttpApi("http://api2.com", "GET", null, "API2", "描述2", 300)
        );
        
        // 创建派生指标
        derivedMetric = new DerivedMetric(
            "拓扑准确率",
            "拓扑质量",
            "准确性评估",
            "%",
            "拓扑关系准确率",
            "(1 - 中压拓扑.拓扑不一致.不一致数量 / 中压拓扑.配变统计.配变总数) * 100",
            Arrays.asList(dependency1, dependency2)
        );
    }
    
    @Test
    void testDerivedMetricCreation() {
        assertThat(derivedMetric.getName()).isEqualTo("拓扑准确率");
        assertThat(derivedMetric.getCategory()).isEqualTo("拓扑质量");
        assertThat(derivedMetric.getSubCategory()).isEqualTo("准确性评估");
        assertThat(derivedMetric.getUnit()).isEqualTo("%");
        assertThat(derivedMetric.getMetricType()).isEqualTo(MetricType.DERIVED);
        assertThat(derivedMetric.isBasic()).isFalse();
        assertThat(derivedMetric.isDerived()).isTrue();
    }
    
    @Test
    void testFormulaAndDependencies() {
        assertThat(derivedMetric.getFormula()).isEqualTo(
            "(1 - 中压拓扑.拓扑不一致.不一致数量 / 中压拓扑.配变统计.配变总数) * 100"
        );
        
        List<Metric> dependencies = derivedMetric.getDependencies();
        assertThat(dependencies).hasSize(2);
        assertThat(dependencies).contains(dependency1, dependency2);
        assertThat(derivedMetric.getDependencyCount()).isEqualTo(2);
    }
    
    @Test
    void testDependencyManagement() {
        BasicMetric newDependency = new BasicMetric(
            "新指标", "分类", "子分类", "个", "描述", null
        );
        
        // 测试添加依赖
        derivedMetric.addDependency(newDependency);
        assertThat(derivedMetric.getDependencyCount()).isEqualTo(3);
        assertThat(derivedMetric.dependsOn(newDependency)).isTrue();
        
        // 测试移除依赖
        derivedMetric.removeDependency(newDependency);
        assertThat(derivedMetric.getDependencyCount()).isEqualTo(2);
        assertThat(derivedMetric.dependsOn(newDependency)).isFalse();
    }
    
    @Test
    void testFormulaValidation() {
        // 测试有效公式
        assertThat(derivedMetric.isValidFormula()).isTrue();
        
        // 测试无效公式（缺少依赖指标）
        DerivedMetric invalidMetric = new DerivedMetric(
            "无效指标", "分类", "子分类", "%", "描述",
            "不存在的指标 * 100",
            Arrays.asList(dependency1)
        );
        assertThat(invalidMetric.isValidFormula()).isFalse();
    }
    
    @Test
    void testCircularDependencyDetection() {
        // 创建循环依赖场景
        DerivedMetric metricA = new DerivedMetric(
            "指标A", "分类", "子分类", "%", "描述",
            "分类.子分类.指标B * 2",
            Arrays.asList()
        );
        
        DerivedMetric metricB = new DerivedMetric(
            "指标B", "分类", "子分类", "%", "描述", 
            "分类.子分类.指标A * 3",
            Arrays.asList()
        );
        
        // 添加相互依赖
        metricA.addDependency(metricB);
        metricB.addDependency(metricA);
        
        // 测试循环依赖检测
        assertThat(metricA.hasCircularDependency(Arrays.asList())).isTrue();
    }
    
    @Test
    void testToString() {
        String toString = derivedMetric.toString();
        assertThat(toString).contains("DerivedMetric");
        assertThat(toString).contains("拓扑准确率");
        assertThat(toString).contains("拓扑质量");
        assertThat(toString).contains("2 items"); // 依赖数量
    }
}
