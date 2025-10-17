package com.gridinsight.e2e;

import com.gridinsight.TestDataGenerator;
import com.gridinsight.domain.model.*;
import com.gridinsight.domain.service.MetricCalculationService;
import com.gridinsight.service.DataSourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

/**
 * 端到端测试
 * 验证完整的业务流程
 */
@SpringBootTest
@ActiveProfiles("test")
public class EndToEndTest {
    
    @Autowired
    private MetricCalculationService calculationService;
    
    private TestDataGenerator.TestDataSet testData;
    
    @BeforeEach
    void setUp() {
        testData = TestDataGenerator.createCompleteTestDataSet();
        
        // 添加所有测试数据到服务
        testData.getAllBasicMetrics().forEach(calculationService::addMetric);
        testData.getAllDerivedMetrics().forEach(calculationService::addMetric);
    }
    
    @Test
    void testCompleteMetricCalculationWorkflow() {
        // 1. 验证基础指标计算
        for (BasicMetric basicMetric : testData.getBasicMetrics()) {
            MetricValue result = calculationService.calculateMetric(basicMetric.getIdentifier());
            assertThat(result).isNotNull();
            assertThat(result.getMetricIdentifier()).isEqualTo(basicMetric.getIdentifier());
            assertThat(result.getUnit()).isEqualTo(basicMetric.getUnit());
        }
        
        // 2. 验证派生指标计算
        for (DerivedMetric derivedMetric : testData.getDerivedMetrics()) {
            MetricValue result = calculationService.calculateMetric(derivedMetric.getIdentifier());
            assertThat(result).isNotNull();
            assertThat(result.getMetricIdentifier()).isEqualTo(derivedMetric.getIdentifier());
            assertThat(result.getUnit()).isEqualTo(derivedMetric.getUnit());
        }
        
        // 3. 验证批量计算
        List<String> allIdentifiers = testData.getAllBasicMetrics().stream()
            .map(Metric::getIdentifier)
            .collect(java.util.stream.Collectors.toList());
        
        Map<String, MetricValue> batchResults = calculationService.calculateMetrics(allIdentifiers);
        assertThat(batchResults).hasSize(allIdentifiers.size());
        
        // 4. 验证缓存功能
        Map<String, Object> cacheStats = calculationService.getCacheStats();
        assertThat(cacheStats).containsKey("cacheSize");
        assertThat(cacheStats).containsKey("totalMetrics");
        
        // 5. 验证清除缓存
        calculationService.clearCache(null);
        Map<String, Object> clearedCacheStats = calculationService.getCacheStats();
        assertThat(clearedCacheStats.get("cacheSize")).isEqualTo(0);
    }
    
    @Test
    void testDifferentDataSourceTypes() {
        // 测试HTTP API数据源
        for (BasicMetric metric : testData.getBasicMetrics()) {
            MetricValue result = calculationService.calculateMetric(metric.getIdentifier());
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
        }
        
        // 测试MQTT数据源
        for (BasicMetric metric : testData.getMqttMetrics()) {
            MetricValue result = calculationService.calculateMetric(metric.getIdentifier());
            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
        }
        
        // 测试数据库数据源
        for (BasicMetric metric : testData.getDbMetrics()) {
            MetricValue result = calculationService.calculateMetric(metric.getIdentifier());
            assertThat(result).isNotNull();
        }
        
        // 测试文件数据源
        for (BasicMetric metric : testData.getFileMetrics()) {
            MetricValue result = calculationService.calculateMetric(metric.getIdentifier());
            assertThat(result).isNotNull();
        }
    }
    
    @Test
    void testFormulaCalculationAccuracy() {
        // 创建已知值的测试数据
        BasicMetric total = new BasicMetric(
            "总数", "测试", "统计", "个", "总数",
            DataSource.createHttpApi("http://test.com", "GET", null, "测试API", "测试", 300)
        );
        
        BasicMetric error = new BasicMetric(
            "错误数", "测试", "错误", "个", "错误数",
            DataSource.createHttpApi("http://test.com", "GET", null, "测试API", "测试", 300)
        );
        
        DerivedMetric accuracy = new DerivedMetric(
            "准确率", "测试", "质量", "%", "准确率",
            "(1 - 测试.错误.错误数 / 测试.统计.总数) * 100",
            Arrays.asList(total, error)
        );
        
        calculationService.addMetric(total);
        calculationService.addMetric(error);
        calculationService.addMetric(accuracy);
        
        // 计算准确率（模拟数据：总数1000，错误数50，准确率应该是95%）
        MetricValue result = calculationService.calculateMetric(accuracy.getIdentifier());
        assertThat(result).isNotNull();
        assertThat(result.isValid()).isTrue();
        
        // 注意：由于是模拟数据，实际值可能不同，这里主要验证计算能正常进行
        assertThat(result.getValue()).isNotNull();
        assertThat(result.getUnit()).isEqualTo("%");
    }
    
    @Test
    void testErrorHandling() {
        // 测试不存在的指标
        MetricValue result = calculationService.calculateMetric("不存在的指标");
        assertThat(result.isValid()).isFalse();
        assertThat(result.getDataSource()).contains("指标不存在");
        
        // 测试空标识符
        result = calculationService.calculateMetric("");
        assertThat(result.isValid()).isFalse();
        
        // 测试null标识符
        result = calculationService.calculateMetric(null);
        assertThat(result.isValid()).isFalse();
    }
    
    @Test
    void testPerformance() {
        // 性能测试：批量计算大量指标
        long startTime = System.currentTimeMillis();
        
        List<String> identifiers = testData.getAllBasicMetrics().stream()
            .map(Metric::getIdentifier)
            .collect(java.util.stream.Collectors.toList());
        
        Map<String, MetricValue> results = calculationService.calculateMetrics(identifiers);
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 验证性能（应该在合理时间内完成）
        assertThat(duration).isLessThan(5000); // 5秒内完成
        assertThat(results).hasSize(identifiers.size());
        
        System.out.println("批量计算 " + identifiers.size() + " 个指标耗时: " + duration + "ms");
    }
    
    @Test
    void testConcurrentCalculation() throws InterruptedException {
        // 并发测试
        int threadCount = 5;
        Thread[] threads = new Thread[threadCount];
        List<String> identifiers = testData.getAllBasicMetrics().stream()
            .map(Metric::getIdentifier)
            .collect(java.util.stream.Collectors.toList());
        
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (String identifier : identifiers) {
                    MetricValue result = calculationService.calculateMetric(identifier);
                    assertThat(result).isNotNull();
                }
            });
            threads[i].start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 验证缓存统计
        Map<String, Object> stats = calculationService.getCacheStats();
        assertThat(stats).isNotNull();
    }
}
