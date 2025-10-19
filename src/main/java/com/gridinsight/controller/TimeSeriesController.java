package com.gridinsight.controller;

import com.gridinsight.domain.model.MetricValue;
import com.gridinsight.domain.model.BasicMetric;
import com.gridinsight.domain.model.DerivedMetric;
import com.gridinsight.service.TimeSeriesDataService;
import com.gridinsight.service.ExternalMetricConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 时序数据查询控制器
 * 提供时序数据的查询和统计接口
 */
@RestController
@RequestMapping("/api/timeseries")
@CrossOrigin(origins = "*")
public class TimeSeriesController {

    @Autowired
    private TimeSeriesDataService timeSeriesDataService;
    
    @Autowired
    private ExternalMetricConfigService metricConfigService;

    /**
     * 查询指标历史数据
     * GET /api/timeseries/history?metric=xxx&start=xxx&end=xxx
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getMetricHistory(
            @RequestParam(required = false) String metric,
            @RequestParam(required = false) String metricUuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        try {
            String metricIdentifier = metric;
            
            // 如果提供了UUID，先通过UUID获取identifier
            if (metricUuid != null && !metricUuid.isEmpty()) {
                BasicMetric basicMetric = metricConfigService.getBasicMetricByUuid(metricUuid);
                if (basicMetric != null) {
                    metricIdentifier = basicMetric.getIdentifier();
                } else {
                    DerivedMetric derivedMetric = metricConfigService.getDerivedMetricByUuid(metricUuid);
                    if (derivedMetric != null) {
                        metricIdentifier = derivedMetric.getIdentifier();
                    } else {
                        Map<String, Object> result = new HashMap<>();
                        result.put("success", false);
                        result.put("error", "未找到UUID对应的指标: " + metricUuid);
                        return ResponseEntity.badRequest().body(result);
                    }
                }
            }
            
            if (metricIdentifier == null || metricIdentifier.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "必须提供metric或metricUuid参数");
                return ResponseEntity.badRequest().body(result);
            }
            
            List<MetricValue> history = timeSeriesDataService.getMetricHistory(metricIdentifier, start, end);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("metric", metricIdentifier);
            result.put("startTime", start);
            result.put("endTime", end);
            result.put("count", history.size());
            result.put("data", history);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "查询历史数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResult);
        }
    }

    /**
     * 查询指标最新值
     * GET /api/timeseries/latest?metric=xxx
     */
    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> getLatestMetricValue(@RequestParam String metric) {
        try {
            MetricValue value = timeSeriesDataService.getLatestMetricValue(metric);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("metric", metric);
            result.put("value", value);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "查询最新值失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResult);
        }
    }

    /**
     * 批量查询最新值
     * POST /api/timeseries/latest-batch
     */
    @PostMapping("/latest-batch")
    public ResponseEntity<Map<String, Object>> getLatestMetricValues(@RequestBody List<String> metrics) {
        try {
            Map<String, MetricValue> values = timeSeriesDataService.getLatestMetricValues(metrics);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", values.size());
            result.put("data", values);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "批量查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResult);
        }
    }

    /**
     * 获取指标统计信息
     * GET /api/timeseries/statistics?metric=xxx&range=1h
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getMetricStatistics(
            @RequestParam String metric,
            @RequestParam(defaultValue = "1h") String range) {
        
        try {
            Map<String, Object> stats = timeSeriesDataService.getMetricStatistics(metric, range);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("metric", metric);
            result.put("range", range);
            result.put("statistics", stats);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResult);
        }
    }

    /**
     * 获取存储统计信息
     * GET /api/timeseries/storage-stats
     */
    @GetMapping("/storage-stats")
    public ResponseEntity<Map<String, Object>> getStorageStats() {
        try {
            Map<String, Object> stats = timeSeriesDataService.getStorageStats();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("storageStats", stats);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "获取存储统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResult);
        }
    }

    /**
     * 清空所有时序数据
     * POST /api/timeseries/clear
     */
    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllData() {
        try {
            timeSeriesDataService.clearAllData();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "所有时序数据已清空");
            result.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "清空数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResult);
        }
    }

    /**
     * 健康检查
     * GET /api/timeseries/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "Time Series Data Service");
        result.put("timestamp", LocalDateTime.now());
        
        try {
            Map<String, Object> storageStats = timeSeriesDataService.getStorageStats();
            result.put("storageStats", storageStats);
        } catch (Exception e) {
            result.put("storageStats", "Error: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
}