package com.gridinsight.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;

/**
 * Metric基础类测试
 */
public class MetricTest {
    
    private Metric metric;
    
    @BeforeEach
    void setUp() {
        metric = new Metric(
            "配变总数",
            "中压拓扑", 
            "配变统计",
            "个",
            "中压配电网中配变的总数量"
        );
    }
    
    @Test
    void testMetricCreation() {
        assertThat(metric.getName()).isEqualTo("配变总数");
        assertThat(metric.getCategory()).isEqualTo("中压拓扑");
        assertThat(metric.getSubCategory()).isEqualTo("配变统计");
        assertThat(metric.getUnit()).isEqualTo("个");
        assertThat(metric.getDescription()).isEqualTo("中压配电网中配变的总数量");
        assertThat(metric.getMetricType()).isEqualTo(MetricType.BASIC);
    }
    
    @Test
    void testIdentifierGeneration() {
        assertThat(metric.getIdentifier()).isEqualTo("中压拓扑.配变统计.配变总数");
    }
    
    @Test
    void testIsBasic() {
        assertThat(metric.isBasic()).isTrue();
        assertThat(metric.isDerived()).isFalse();
    }
    
    @Test
    void testEqualsAndHashCode() {
        Metric sameMetric = new Metric(
            "配变总数",
            "中压拓扑",
            "配变统计", 
            "个",
            "中压配电网中配变的总数量"
        );
        
        Metric differentMetric = new Metric(
            "配变数量",
            "中压拓扑",
            "配变统计",
            "个", 
            "不同的描述"
        );
        
        assertThat(metric).isEqualTo(sameMetric);
        assertThat(metric).isNotEqualTo(differentMetric);
        assertThat(metric.hashCode()).isEqualTo(sameMetric.hashCode());
    }
    
    @Test
    void testToString() {
        String toString = metric.toString();
        assertThat(toString).contains("配变总数");
        assertThat(toString).contains("中压拓扑");
        assertThat(toString).contains("配变统计");
    }
}
