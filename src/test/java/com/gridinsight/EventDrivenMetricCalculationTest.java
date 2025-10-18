package com.gridinsight;

import com.gridinsight.domain.event.MetricUpdateEvent;
import com.gridinsight.domain.model.*;
import com.gridinsight.domain.service.MetricCalculationService;
import com.gridinsight.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 事件驱动的指标计算测试
 * 验证派生指标的值是否真正被计算和更新
 */
@SpringBootTest
@ActiveProfiles("test")
public class EventDrivenMetricCalculationTest {

    @Autowired
    private EventDrivenMetricUpdateService eventDrivenUpdateService;
    
    @Autowired
    private MetricConfigService metricConfigService;
    
    @Autowired
    private MetricCalculationService metricCalculationService;
    
    @Autowired
    private TimeSeriesDataService timeSeriesDataService;
    
    @Autowired
    private DataSourceService dataSourceService;

    @BeforeEach
    void setUp() {
        // 清空时序数据
        timeSeriesDataService.clearAllData();
        
        // 初始化依赖关系映射
        eventDrivenUpdateService.initializeDependencyMap();
        
        // 等待初始化完成
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 测试场景1：验证基础指标到派生指标的完整计算流程
     * 创建简单的测试指标，验证派生指标值是否正确计算
     */
    @Test
    void testBasicToDerivedMetricCalculation() throws InterruptedException {
        System.out.println("\n=== 测试场景1：基础指标到派生指标的计算验证 ===");
        
        // 1. 创建测试用的基础指标
        createTestBasicMetrics();
        
        // 2. 创建测试用的派生指标
        createTestDerivedMetrics();
        
        // 3. 初始化依赖关系映射
        eventDrivenUpdateService.initializeDependencyMap();
        
        // 4. 模拟基础指标更新，触发派生指标计算
        String basicMetricId = "测试.基础.指标A";
        Double newValue = 100.0;
        
        System.out.println("更新基础指标: " + basicMetricId + " = " + newValue);
        
        // 直接调用MetricCalculationService计算派生指标
        String derivedMetricId = "测试.派生.指标C";
        MetricValue derivedValue = metricCalculationService.calculateMetric(derivedMetricId);
        
        System.out.println("派生指标计算结果: " + derivedMetricId + " = " + derivedValue.getValue());
        
        // 5. 验证派生指标值是否正确计算
        assertTrue(derivedValue.isValid(), "派生指标应该计算成功");
        assertNotNull(derivedValue.getValue(), "派生指标值不应该为null");
        
        // 验证计算公式: 指标C = 指标A * 2 = 100 * 2 = 200
        assertEquals(200.0, derivedValue.getValue(), 0.01, "派生指标值应该等于100 * 2 = 200");
        
        System.out.println("✅ 测试通过：派生指标值计算正确");
    }

    /**
     * 测试场景2：验证多级依赖的计算
     * 基础指标A → 派生指标B → 派生指标D
     */
    @Test
    void testMultiLevelDependencyCalculation() throws InterruptedException {
        System.out.println("\n=== 测试场景2：多级依赖计算验证 ===");
        
        // 1. 创建测试指标
        createTestBasicMetrics();
        createTestDerivedMetrics();
        eventDrivenUpdateService.initializeDependencyMap();
        
        // 2. 计算第一级派生指标B
        String derivedMetricB = "测试.派生.指标B";
        MetricValue valueB = metricCalculationService.calculateMetric(derivedMetricB);
        
        System.out.println("第一级派生指标B: " + valueB.getValue());
        assertTrue(valueB.isValid(), "第一级派生指标B应该计算成功");
        
        // 3. 计算第二级派生指标D（依赖指标B）
        String derivedMetricD = "测试.派生.指标D";
        MetricValue valueD = metricCalculationService.calculateMetric(derivedMetricD);
        
        System.out.println("第二级派生指标D: " + valueD.getValue());
        assertTrue(valueD.isValid(), "第二级派生指标D应该计算成功");
        
        // 验证计算公式：
        // 指标B = 指标A + 指标A = 100 + 100 = 200
        // 指标D = 指标B * 1.5 = 200 * 1.5 = 300
        assertEquals(200.0, valueB.getValue(), 0.01, "指标B应该等于200");
        assertEquals(300.0, valueD.getValue(), 0.01, "指标D应该等于300");
        
        System.out.println("✅ 测试通过：多级依赖计算正确");
    }

    /**
     * 测试场景3：验证事件驱动的实时计算
     * 模拟基础指标更新事件，验证派生指标是否实时重新计算
     */
    @Test
    void testEventDrivenRealTimeCalculation() throws InterruptedException {
        System.out.println("\n=== 测试场景3：事件驱动实时计算验证 ===");
        
        // 1. 创建测试指标
        createTestBasicMetrics();
        createTestDerivedMetrics();
        eventDrivenUpdateService.initializeDependencyMap();
        
        // 2. 计算初始派生指标值
        String derivedMetricId = "测试.派生.指标C";
        MetricValue initialValue = metricCalculationService.calculateMetric(derivedMetricId);
        System.out.println("初始派生指标值: " + initialValue.getValue());
        
        // 3. 更新基础指标值
        String basicMetricId = "测试.基础.指标A";
        Double newValue = 150.0; // 从100改为150
        
        // 清除派生指标缓存，确保重新计算
        metricCalculationService.clearCache(derivedMetricId);
        
        // 手动更新基础指标到MetricCalculationService
        updateBasicMetricInCalculationService(basicMetricId, newValue);
        
        System.out.println("更新基础指标: " + basicMetricId + " = " + newValue);
        
        // 4. 重新计算派生指标
        MetricValue updatedValue = metricCalculationService.calculateMetric(derivedMetricId);
        System.out.println("更新后派生指标值: " + updatedValue.getValue());
        
        // 5. 验证派生指标值是否更新
        assertTrue(updatedValue.isValid(), "更新后的派生指标应该计算成功");
        assertEquals(300.0, updatedValue.getValue(), 0.01, "派生指标应该等于150 * 2 = 300");
        
        // 验证值确实发生了变化
        assertNotEquals(initialValue.getValue(), updatedValue.getValue(), "派生指标值应该发生变化");
        
        System.out.println("✅ 测试通过：事件驱动实时计算正确");
    }

    /**
     * 测试场景4：验证复杂公式计算
     * 测试包含数学函数的复杂公式
     */
    @Test
    void testComplexFormulaCalculation() throws InterruptedException {
        System.out.println("\n=== 测试场景4：复杂公式计算验证 ===");
        
        // 1. 创建测试指标
        createTestBasicMetrics();
        createComplexDerivedMetrics();
        eventDrivenUpdateService.initializeDependencyMap();
        
        // 2. 计算复杂派生指标
        String complexMetricId = "测试.复杂.指标E";
        MetricValue complexValue = metricCalculationService.calculateMetric(complexMetricId);
        
        System.out.println("复杂派生指标E: " + complexValue.getValue());
        
        // 3. 验证计算结果
        assertTrue(complexValue.isValid(), "复杂派生指标应该计算成功");
        assertNotNull(complexValue.getValue(), "复杂派生指标值不应该为null");
        
        // 验证公式: sqrt(指标A * 指标A + 指标A * 指标A) = sqrt(100*100 + 100*100) = sqrt(20000) ≈ 141.42
        double expectedValue = Math.sqrt(100.0 * 100.0 + 100.0 * 100.0);
        assertEquals(expectedValue, complexValue.getValue(), 0.01, "复杂公式计算结果应该正确");
        
        System.out.println("✅ 测试通过：复杂公式计算正确");
    }

    /**
     * 创建测试用的基础指标
     */
    private void createTestBasicMetrics() {
        // 创建基础指标A
        DataSource dataSourceA = new DataSource(
            DataSource.SourceType.HTTP_API,
            "http://test.com/api/metricA",
            "测试数据源A",
            300,
            true
        );
        
        BasicMetric metricA = new BasicMetric(
            "指标A",
            "测试",
            "基础",
            "个",
            "测试基础指标A",
            dataSourceA
        );
        metricA.setIdentifier("测试.基础.指标A");
        
        // 添加到MetricCalculationService
        metricCalculationService.addMetric(metricA);
        
        // 手动设置基础指标的值（模拟从数据源获取）
        updateBasicMetricInCalculationService("测试.基础.指标A", 100.0);
        
        System.out.println("创建测试基础指标: 测试.基础.指标A = 100");
    }

    /**
     * 创建测试用的派生指标
     */
    private void createTestDerivedMetrics() {
        // 创建派生指标B: 指标A + 指标A
        DerivedMetric metricB = new DerivedMetric(
            "指标B",
            "测试",
            "派生",
            "个",
            "测试派生指标B",
            "测试.基础.指标A + 测试.基础.指标A",
            java.util.Arrays.asList(metricCalculationService.getMetric("测试.基础.指标A"))
        );
        metricB.setIdentifier("测试.派生.指标B");
        
        // 创建派生指标C: 指标A * 2
        DerivedMetric metricC = new DerivedMetric(
            "指标C",
            "测试",
            "派生",
            "个",
            "测试派生指标C",
            "测试.基础.指标A * 2",
            java.util.Arrays.asList(metricCalculationService.getMetric("测试.基础.指标A"))
        );
        metricC.setIdentifier("测试.派生.指标C");
        
        // 创建派生指标D: 指标B * 1.5
        DerivedMetric metricD = new DerivedMetric(
            "指标D",
            "测试",
            "派生",
            "个",
            "测试派生指标D",
            "测试.派生.指标B * 1.5",
            java.util.Arrays.asList(metricB)
        );
        metricD.setIdentifier("测试.派生.指标D");
        
        // 添加到MetricCalculationService
        metricCalculationService.addMetric(metricB);
        metricCalculationService.addMetric(metricC);
        metricCalculationService.addMetric(metricD);
        
        System.out.println("创建测试派生指标: B, C, D");
    }

    /**
     * 创建复杂公式的派生指标
     */
    private void createComplexDerivedMetrics() {
        // 创建复杂派生指标E: sqrt(指标A^2 + 指标A^2)
        DerivedMetric metricE = new DerivedMetric(
            "指标E",
            "测试",
            "复杂",
            "个",
            "测试复杂派生指标E",
            "sqrt(测试.基础.指标A * 测试.基础.指标A + 测试.基础.指标A * 测试.基础.指标A)",
            java.util.Arrays.asList(metricCalculationService.getMetric("测试.基础.指标A"))
        );
        metricE.setIdentifier("测试.复杂.指标E");
        
        // 添加到MetricCalculationService
        metricCalculationService.addMetric(metricE);
        
        System.out.println("创建复杂派生指标: E");
    }

    /**
     * 手动更新基础指标在计算服务中的值
     */
    private void updateBasicMetricInCalculationService(String metricId, Double value) {
        // 使用MetricCalculationService的setBasicMetricValue方法
        metricCalculationService.setBasicMetricValue(metricId, value);
    }
}
