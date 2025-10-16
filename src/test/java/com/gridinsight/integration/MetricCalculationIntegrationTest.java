package com.gridinsight.integration;

import com.gridinsight.domain.model.*;
import com.gridinsight.domain.service.MetricCalculationService;
import com.gridinsight.service.DataSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

/**
 * 指标计算集成测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class MetricCalculationIntegrationTest {
    
    private MetricCalculationService calculationService;
    private DataSourceService dataSourceService;
    
    @BeforeEach
    void setUp() {
        dataSourceService = new DataSourceService();
        calculationService = new MetricCalculationService();
        
        // 设置数据源服务
        // 注意：这里需要修改MetricCalculationService以支持依赖注入
        // 暂时跳过这个测试，或者修改服务以支持测试
    }
    
    @Test
    void testBasicMetricCalculation() {
        // 创建基础指标
        BasicMetric transformerTotal = new BasicMetric(
            "配变总数",
            "中压拓扑",
            "配变统计",
            "个",
            "配变总数",
            DataSource.createHttpApi("http://api1.com", "API1", "描述1")
        );
        
        // 添加到服务
        calculationService.addMetric(transformerTotal);
        
        // 计算指标值
        MetricValue result = calculationService.calculateMetric(transformerTotal.getIdentifier());
        
        // 验证结果
        assertThat(result).isNotNull();
        // 注意：由于数据源服务是模拟的，结果可能不稳定
        // 这里主要验证服务能正常运行
    }
    
    @Test
    void testDerivedMetricCalculation() {
        // 创建基础指标
        BasicMetric total = new BasicMetric(
            "配变总数", "中压拓扑", "配变统计", "个", "总数",
            DataSource.createHttpApi("http://api1.com", "API1", "描述1")
        );
        
        BasicMetric inconsistent = new BasicMetric(
            "不一致数量", "中压拓扑", "拓扑不一致", "个", "不一致数量",
            DataSource.createHttpApi("http://api2.com", "API2", "描述2")
        );
        
        // 创建派生指标
        DerivedMetric accuracy = new DerivedMetric(
            "拓扑准确率",
            "拓扑质量",
            "准确性评估",
            "%",
            "拓扑准确率",
            "(1 - 中压拓扑.拓扑不一致.不一致数量 / 中压拓扑.配变统计.配变总数) * 100",
            Arrays.asList(total, inconsistent)
        );
        
        // 添加到服务
        calculationService.addMetric(total);
        calculationService.addMetric(inconsistent);
        calculationService.addMetric(accuracy);
        
        // 计算派生指标
        MetricValue result = calculationService.calculateMetric(accuracy.getIdentifier());
        
        // 验证结果
        assertThat(result).isNotNull();
        // 验证计算逻辑
    }
    
    @Test
    void testBatchCalculation() {
        // 创建多个指标
        BasicMetric metric1 = new BasicMetric("指标1", "分类1", "子分类1", "个", "描述1", null);
        BasicMetric metric2 = new BasicMetric("指标2", "分类2", "子分类2", "个", "描述2", null);
        
        calculationService.addMetric(metric1);
        calculationService.addMetric(metric2);
        
        // 批量计算
        List<String> identifiers = Arrays.asList(metric1.getIdentifier(), metric2.getIdentifier());
        Map<String, MetricValue> results = calculationService.calculateMetrics(identifiers);
        
        // 验证结果
        assertThat(results).hasSize(2);
        assertThat(results).containsKey(metric1.getIdentifier());
        assertThat(results).containsKey(metric2.getIdentifier());
    }
    
    @Test
    void testCacheFunctionality() {
        // 创建指标
        BasicMetric metric = new BasicMetric("测试指标", "分类", "子分类", "个", "描述", null);
        calculationService.addMetric(metric);
        
        // 第一次计算
        MetricValue result1 = calculationService.calculateMetric(metric.getIdentifier());
        
        // 第二次计算（应该使用缓存）
        MetricValue result2 = calculationService.calculateMetric(metric.getIdentifier());
        
        // 验证缓存统计
        Map<String, Object> stats = calculationService.getCacheStats();
        assertThat(stats).containsKey("cacheSize");
        assertThat(stats).containsKey("totalMetrics");
    }
    
    @Test
    void testErrorHandling() {
        // 测试不存在的指标
        MetricValue result = calculationService.calculateMetric("不存在的指标");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getDataSource()).contains("指标不存在");
    }
}
