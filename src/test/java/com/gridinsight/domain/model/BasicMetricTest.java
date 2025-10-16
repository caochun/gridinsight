package com.gridinsight.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;

/**
 * BasicMetric测试
 */
public class BasicMetricTest {
    
    private BasicMetric basicMetric;
    private DataSource dataSource;
    
    @BeforeEach
    void setUp() {
        dataSource = DataSource.createHttpApi(
            "https://api.example.com/metrics",
            "测试API",
            "测试数据源"
        );
        
        basicMetric = new BasicMetric(
            "配变总数",
            "中压拓扑",
            "配变统计", 
            "个",
            "中压配电网中配变的总数量",
            dataSource
        );
    }
    
    @Test
    void testBasicMetricCreation() {
        assertThat(basicMetric.getName()).isEqualTo("配变总数");
        assertThat(basicMetric.getCategory()).isEqualTo("中压拓扑");
        assertThat(basicMetric.getSubCategory()).isEqualTo("配变统计");
        assertThat(basicMetric.getUnit()).isEqualTo("个");
        assertThat(basicMetric.getMetricType()).isEqualTo(MetricType.BASIC);
        assertThat(basicMetric.isBasic()).isTrue();
        assertThat(basicMetric.isDerived()).isFalse();
    }
    
    @Test
    void testDataSourceConfiguration() {
        assertThat(basicMetric.getDataSource()).isNotNull();
        assertThat(basicMetric.getDataSource().getSourceType()).isEqualTo(DataSource.SourceType.HTTP_API);
        assertThat(basicMetric.getDataSource().getSourceAddress()).isEqualTo("https://api.example.com/metrics");
        assertThat(basicMetric.getDataSource().getSourceName()).isEqualTo("测试API");
    }
    
    @Test
    void testBasicMetricWithoutDataSource() {
        BasicMetric metricWithoutSource = new BasicMetric(
            "测试指标",
            "测试分类",
            "测试子分类",
            "个",
            "测试描述",
            null
        );
        
        assertThat(metricWithoutSource.getDataSource()).isNull();
        assertThat(metricWithoutSource.getMetricType()).isEqualTo(MetricType.BASIC);
    }
    
    @Test
    void testToString() {
        String toString = basicMetric.toString();
        assertThat(toString).contains("BasicMetric");
        assertThat(toString).contains("配变总数");
        assertThat(toString).contains("中压拓扑");
    }
}
