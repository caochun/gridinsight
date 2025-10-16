package com.gridinsight;

import com.gridinsight.domain.event.MetricUpdateEvent;
import com.gridinsight.domain.model.*;
import com.gridinsight.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 事件驱动依赖更新测试
 * 测试不同层级的派生指标依赖更新机制
 */
@SpringBootTest
@ActiveProfiles("test")
public class EventDrivenDependencyUpdateTest {

    @Autowired
    private EventDrivenMetricUpdateService eventDrivenUpdateService;
    
    @Autowired
    private MetricConfigService metricConfigService;
    
    @Autowired
    private MetricSchedulerService metricSchedulerService;
    
    @Autowired
    private TimeSeriesDataService timeSeriesDataService;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;

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
     * 测试场景1：只依赖基础指标的派生指标
     * 当基础指标更新时，派生指标应该自动更新
     */
    @Test
    void testBasicToDerivedDependency() throws InterruptedException {
        System.out.println("\n=== 测试场景1：基础指标 → 派生指标 ===");
        
        // 设置等待机制，等待异步更新完成
        CountDownLatch latch = new CountDownLatch(2); // 期望2个指标更新
        
        // 模拟基础指标更新事件
        String basicMetricId = "中压拓扑.配变统计.配变总数";
        Double newValue = 1200.0;
        
        System.out.println("1. 发布基础指标更新事件: " + basicMetricId + " = " + newValue);
        
        // 发布事件
        eventDrivenUpdateService.publishMetricUpdateEvent(basicMetricId, newValue, "TEST");
        
        // 等待异步处理完成
        Thread.sleep(2000);
        
        // 验证依赖关系映射
        Map<String, Set<String>> dependencyMap = eventDrivenUpdateService.getDependencyMap();
        System.out.println("2. 依赖关系映射:");
        dependencyMap.forEach((dep, dependents) -> 
            System.out.println("   " + dep + " -> " + dependents));
        
        // 验证拓扑关系准确率应该依赖配变总数
        Set<String> dependents = eventDrivenUpdateService.getDependentMetrics(basicMetricId);
        assertTrue(dependents.contains("中压拓扑.拓扑关系准确率.拓扑关系准确率"), 
                   "拓扑关系准确率应该依赖配变总数");
        
        // 验证没有循环依赖
        assertFalse(eventDrivenUpdateService.hasCircularDependency(), 
                   "不应该存在循环依赖");
        
        System.out.println("3. 测试通过：基础指标更新成功触发派生指标依赖检查");
    }

    /**
     * 测试场景2：依赖基础指标和派生指标的派生指标
     * 当基础指标更新时，应该触发多级派生指标更新
     */
    @Test
    void testMultiLevelDependency() throws InterruptedException {
        System.out.println("\n=== 测试场景2：多级依赖链 ===");
        
        // 发布第一个基础指标更新
        String basicMetric1 = "中压拓扑.配变统计.配变总数";
        Double value1 = 1000.0;
        
        System.out.println("1. 更新基础指标1: " + basicMetric1 + " = " + value1);
        eventDrivenUpdateService.publishMetricUpdateEvent(basicMetric1, value1, "TEST");
        Thread.sleep(1000);
        
        // 发布第二个基础指标更新
        String basicMetric2 = "中压拓扑.拓扑不一致.配变挂接馈线不一致数量";
        Double value2 = 50.0;
        
        System.out.println("2. 更新基础指标2: " + basicMetric2 + " = " + value2);
        eventDrivenUpdateService.publishMetricUpdateEvent(basicMetric2, value2, "TEST");
        Thread.sleep(1000);
        
        // 验证依赖链
        System.out.println("3. 依赖链分析:");
        
        // 基础指标1的依赖链
        var chain1 = eventDrivenUpdateService.getDependencyChain(basicMetric1);
        System.out.println("   基础指标1依赖链: " + chain1);
        
        // 基础指标2的依赖链  
        var chain2 = eventDrivenUpdateService.getDependencyChain(basicMetric2);
        System.out.println("   基础指标2依赖链: " + chain2);
        
        // 验证依赖关系
        assertTrue(chain1.contains("中压拓扑.拓扑关系准确率.拓扑关系准确率"),
                  "依赖链应该包含拓扑关系准确率");
        assertTrue(chain2.contains("中压拓扑.拓扑关系准确率.拓扑关系准确率"),
                  "依赖链应该包含拓扑关系准确率");
        
        System.out.println("4. 测试通过：多级依赖链正确建立");
    }

    /**
     * 测试场景3：依赖多个派生指标的派生指标
     * 测试综合质量指标依赖两个派生指标的情况
     */
    @Test
    void testDerivedToDerivedDependency() throws InterruptedException {
        System.out.println("\n=== 测试场景3：派生指标 → 派生指标 ===");
        
        // 检查综合质量指标的依赖关系
        String comprehensiveMetric = "综合质量.综合质量指标.综合质量指标";
        DerivedMetric metric = metricConfigService.getAllDerivedMetrics().get(comprehensiveMetric);
        
        assertNotNull(metric, "综合质量指标应该存在");
        System.out.println("1. 综合质量指标依赖: " + metric.getDependencies().stream()
                .map(Metric::getIdentifier)
                .reduce((a, b) -> a + ", " + b)
                .orElse("无"));
        
        // 验证依赖的派生指标
        assertTrue(metric.getDependencies().stream()
                .anyMatch(dep -> dep.getIdentifier().equals("中压拓扑.拓扑关系准确率.拓扑关系准确率")),
                "应该依赖拓扑关系准确率");
        assertTrue(metric.getDependencies().stream()
                .anyMatch(dep -> dep.getIdentifier().equals("低压用户关系.变户关系准确率.变户关系准确率")),
                "应该依赖变户关系准确率");
        
        // 模拟其中一个依赖的派生指标更新
        String derivedMetricId = "中压拓扑.拓扑关系准确率.拓扑关系准确率";
        Double derivedValue = 95.5;
        
        System.out.println("2. 模拟派生指标更新: " + derivedMetricId + " = " + derivedValue);
        eventDrivenUpdateService.publishMetricUpdateEvent(derivedMetricId, derivedValue, "TEST");
        Thread.sleep(1000);
        
        // 验证依赖关系映射中包含了派生指标到派生指标的依赖
        Set<String> dependents = eventDrivenUpdateService.getDependentMetrics(derivedMetricId);
        System.out.println("3. 拓扑关系准确率的依赖者: " + dependents);
        
        // 注意：综合质量指标使用SCHEDULED策略，不会通过事件驱动更新
        // 但依赖关系映射应该正确建立
        System.out.println("4. 测试通过：派生指标间依赖关系正确建立");
    }

    /**
     * 测试场景4：完整的依赖链更新流程
     * 模拟真实场景中的完整更新流程
     */
    @Test
    void testCompleteDependencyChainUpdate() throws InterruptedException {
        System.out.println("\n=== 测试场景4：完整依赖链更新 ===");
        
        // 1. 更新基础指标，触发整个依赖链
        String baseMetric = "中压拓扑.配变统计.配变总数";
        Double baseValue = 1500.0;
        
        System.out.println("1. 更新基础指标: " + baseMetric + " = " + baseValue);
        
        // 直接通过调度服务触发更新（这会发布事件）
        BasicMetric basicMetric = metricConfigService.getAllBasicMetrics().get(baseMetric);
        assertNotNull(basicMetric, "基础指标应该存在");
        
        // 手动触发基础指标更新
        metricSchedulerService.triggerMetricUpdate(baseMetric);
        
        // 等待异步处理
        Thread.sleep(3000);
        
        // 2. 验证依赖链是否正确触发
        Map<String, Set<String>> dependencyMap = eventDrivenUpdateService.getDependencyMap();
        System.out.println("2. 完整依赖关系映射:");
        dependencyMap.forEach((dep, dependents) -> 
            System.out.println("   " + dep + " -> " + dependents));
        
        // 3. 验证没有循环依赖
        boolean hasCircularDependency = eventDrivenUpdateService.hasCircularDependency();
        System.out.println("3. 循环依赖检查: " + (hasCircularDependency ? "存在" : "不存在"));
        assertFalse(hasCircularDependency, "不应该存在循环依赖");
        
        // 4. 验证依赖链的完整性
        var chain = eventDrivenUpdateService.getDependencyChain(baseMetric);
        System.out.println("4. 完整依赖链: " + chain);
        assertTrue(chain.size() > 1, "依赖链应该包含多个指标");
        
        System.out.println("5. 测试通过：完整依赖链更新流程正常");
    }

    /**
     * 测试场景5：并发更新测试
     * 测试多个指标同时更新时的依赖处理
     */
    @Test
    void testConcurrentUpdates() throws InterruptedException {
        System.out.println("\n=== 测试场景5：并发更新测试 ===");
        
        // 同时更新多个基础指标
        String[] baseMetrics = {
            "中压拓扑.配变统计.配变总数",
            "中压拓扑.拓扑不一致.配变挂接馈线不一致数量",
            "低压用户关系.低压用户统计.全省低压用户总数"
        };
        
        Double[] values = {2000.0, 100.0, 50000.0};
        
        System.out.println("1. 并发更新多个基础指标:");
        for (int i = 0; i < baseMetrics.length; i++) {
            System.out.println("   " + baseMetrics[i] + " = " + values[i]);
            eventDrivenUpdateService.publishMetricUpdateEvent(baseMetrics[i], values[i], "CONCURRENT_TEST");
        }
        
        // 等待所有异步处理完成
        Thread.sleep(3000);
        
        // 验证依赖关系映射的完整性
        Map<String, Set<String>> dependencyMap = eventDrivenUpdateService.getDependencyMap();
        System.out.println("2. 并发更新后的依赖关系:");
        dependencyMap.forEach((dep, dependents) -> 
            System.out.println("   " + dep + " -> " + dependents));
        
        // 验证所有基础指标都有正确的依赖关系
        for (String metric : baseMetrics) {
            Set<String> dependents = eventDrivenUpdateService.getDependentMetrics(metric);
            System.out.println("3. " + metric + " 的依赖者: " + dependents);
            assertNotNull(dependents, "每个指标都应该有依赖关系记录");
        }
        
        // 验证没有循环依赖
        assertFalse(eventDrivenUpdateService.hasCircularDependency(), 
                   "并发更新不应该产生循环依赖");
        
        System.out.println("4. 测试通过：并发更新处理正常");
    }

    /**
     * 测试场景6：错误处理测试
     * 测试不存在指标的事件处理
     */
    @Test
    void testErrorHandling() {
        System.out.println("\n=== 测试场景6：错误处理测试 ===");
        
        // 测试不存在的指标
        String nonExistentMetric = "不存在的指标";
        Double value = 100.0;
        
        System.out.println("1. 测试不存在指标的更新事件");
        
        // 这应该不会抛出异常
        assertDoesNotThrow(() -> {
            eventDrivenUpdateService.publishMetricUpdateEvent(nonExistentMetric, value, "ERROR_TEST");
        }, "不存在的指标更新不应该抛出异常");
        
        // 验证依赖关系映射中不包含不存在的指标
        Set<String> dependents = eventDrivenUpdateService.getDependentMetrics(nonExistentMetric);
        assertTrue(dependents.isEmpty(), "不存在指标的依赖关系应该为空集合");
        
        System.out.println("2. 测试通过：错误处理机制正常");
    }
}
