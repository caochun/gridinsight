package com.gridinsight.service;

import com.gridinsight.domain.model.MetricValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JSON文件存储时序数据服务测试
 */
class JsonTimeSeriesDataServiceTest {

    @TempDir
    File tempDir;

    private JsonTimeSeriesDataService service;

    @BeforeEach
    void setUp() {
        service = new JsonTimeSeriesDataService();
        // 设置临时目录作为数据路径
        ReflectionTestUtils.setField(service, "dataPath", tempDir.getAbsolutePath());
        service.init();
    }

    @Test
    void testStoreAndRetrieveMetricValue() {
        // 准备测试数据
        String metricId = "test.metric.1";
        MetricValue value = MetricValue.good(metricId, 100.5, "kW");
        LocalDateTime timestamp = LocalDateTime.now();

        // 存储指标值
        service.storeMetricValue(metricId, value, timestamp);

        // 查询最新值
        MetricValue retrieved = service.getLatestMetricValue(metricId);

        // 验证结果
        assertNotNull(retrieved);
        assertEquals(metricId, retrieved.getMetricIdentifier());
        assertEquals(100.5, retrieved.getValue());
        assertEquals("kW", retrieved.getUnit());
        assertEquals(MetricValue.DataQuality.GOOD, retrieved.getQuality());
    }

    @Test
    void testStoreMultipleValues() {
        String metricId = "test.metric.2";
        LocalDateTime baseTime = LocalDateTime.now();

        // 存储多个值
        for (int i = 0; i < 5; i++) {
            MetricValue value = MetricValue.good(metricId, 100.0 + i, "kW");
            service.storeMetricValue(metricId, value, baseTime.plusMinutes(i));
        }

        // 查询最新值
        MetricValue latest = service.getLatestMetricValue(metricId);
        assertEquals(104.0, latest.getValue());

        // 查询历史数据
        List<MetricValue> history = service.getMetricHistory(
            metricId, 
            baseTime.minusMinutes(1), 
            baseTime.plusMinutes(10)
        );

        assertEquals(5, history.size());
        assertEquals(100.0, history.get(0).getValue());
        assertEquals(104.0, history.get(4).getValue());
    }

    @Test
    void testBatchStore() {
        Map<String, MetricValue> values = Map.of(
            "test.metric.3", MetricValue.good("test.metric.3", 50.0, "MW"),
            "test.metric.4", MetricValue.good("test.metric.4", 75.0, "MW")
        );

        LocalDateTime timestamp = LocalDateTime.now();
        service.storeMetricValues(values, timestamp);

        // 验证批量存储
        MetricValue value1 = service.getLatestMetricValue("test.metric.3");
        MetricValue value2 = service.getLatestMetricValue("test.metric.4");

        assertEquals(50.0, value1.getValue());
        assertEquals(75.0, value2.getValue());
    }

    @Test
    void testGetMetricStatistics() {
        String metricId = "test.metric.5";
        LocalDateTime baseTime = LocalDateTime.now();

        // 存储测试数据
        double[] values = {10.0, 20.0, 30.0, 40.0, 50.0};
        for (int i = 0; i < values.length; i++) {
            MetricValue value = MetricValue.good(metricId, values[i], "kW");
            service.storeMetricValue(metricId, value, baseTime.plusMinutes(i));
        }

        // 获取统计信息
        Map<String, Object> stats = service.getMetricStatistics(metricId, "10m");

        assertEquals(5, stats.get("count"));
        assertEquals(30.0, (Double) stats.get("average"));
        assertEquals(50.0, (Double) stats.get("max"));
        assertEquals(10.0, (Double) stats.get("min"));
    }

    @Test
    void testGetStorageStats() {
        // 存储一些数据
        service.storeMetricValue("test.metric.6", MetricValue.good("test.metric.6", 100.0, "kW"), LocalDateTime.now());

        // 获取存储统计
        Map<String, Object> stats = service.getStorageStats();

        assertTrue((Integer) stats.get("totalMetrics") > 0);
        assertTrue((Integer) stats.get("cachedValues") > 0);
        assertTrue((Boolean) stats.get("dataRootExists"));
        assertTrue((Long) stats.get("totalSizeBytes") >= 0);
    }

    @Test
    void testClearAllData() {
        // 存储一些数据
        service.storeMetricValue("test.metric.7", MetricValue.good("test.metric.7", 100.0, "kW"), LocalDateTime.now());

        // 清空数据
        service.clearAllData();

        // 验证数据已清空
        Map<String, Object> stats = service.getStorageStats();
        assertEquals(0, stats.get("totalMetrics"));
        assertEquals(0, stats.get("cachedValues"));
    }

    @Test
    void testTimeRangeCalculation() {
        LocalDateTime now = LocalDateTime.now();
        
        // 测试不同时间范围
        String[] timeRanges = {"5m", "1h", "2h", "1d", "30"};
        for (String range : timeRanges) {
            Map<String, Object> stats = service.getMetricStatistics("nonexistent", range);
            assertNotNull(stats);
            // 对于不存在的指标，应该返回空统计信息
            assertTrue(stats.containsKey("count") || stats.containsKey("error"));
        }
    }

    @Test
    void testErrorHandling() {
        // 测试无效指标ID
        MetricValue result = service.getLatestMetricValue("nonexistent.metric");
        assertNotNull(result);
        assertEquals(0.0, result.getValue());

        // 测试空历史查询
        List<MetricValue> history = service.getMetricHistory(
            "nonexistent.metric", 
            LocalDateTime.now().minusHours(1), 
            LocalDateTime.now()
        );
        assertTrue(history.isEmpty());
    }
}
