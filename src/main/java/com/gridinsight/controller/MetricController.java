package com.gridinsight.controller;

import com.gridinsight.domain.model.MetricValue;
import com.gridinsight.domain.model.Metric;
import com.gridinsight.domain.model.BasicMetric;
import com.gridinsight.domain.model.DerivedMetric;
import com.gridinsight.service.TimeSeriesDataService;
import com.gridinsight.service.ExternalMetricConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 指标查询REST控制器
 */
@RestController
@RequestMapping("/api/metrics")
@CrossOrigin(origins = "*")
public class MetricController {

    @Autowired
    private TimeSeriesDataService timeSeriesDataService;
    
    @Autowired
    private ExternalMetricConfigService metricConfigService;

    /**
     * 根据标识符查询指标值（从时序数据库获取最新值）
     * GET /api/metrics/query?identifier=xxx
     */
    @GetMapping("/query")
    public ResponseEntity<Map<String, Object>> queryMetric(
            @RequestParam String identifier) {
        
        try {
            // 直接从时序数据库获取最新值
            MetricValue response = timeSeriesDataService.getLatestMetricValue(identifier);
            
            Map<String, Object> result = new HashMap<>();
            
            if (response != null && response.isValid()) {
                result.put("success", true);
                result.put("identifier", response.getMetricIdentifier());
                result.put("value", response.getValue());
                result.put("unit", response.getUnit());
                result.put("timestamp", response.getTimestamp());
                result.put("quality", response.getQuality());
                result.put("dataSource", response.getDataSource());
                
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("identifier", identifier);
                result.put("error", response != null ? response.getQuality() : "指标不存在或没有数据");
                result.put("value", null);
                result.put("unit", null);
                result.put("timestamp", null);
                result.put("quality", "ERROR");
                result.put("dataSource", null);
                
                return ResponseEntity.ok(result);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "服务器内部错误: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
    
    /**
     * 根据UUID查询指标值（从时序数据库获取最新值）
     * GET /api/metrics/query-by-uuid?uuid=xxx
     */
    @GetMapping("/query-by-uuid")
    public ResponseEntity<Map<String, Object>> queryMetricByUuid(
            @RequestParam String uuid) {
        
        try {
            // 通过UUID查找指标定义
            Metric metric = metricConfigService.getMetricByUuid(uuid);
            if (metric == null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("uuid", uuid);
                result.put("error", "指标不存在");
                result.put("value", null);
                return ResponseEntity.ok(result);
            }
            
            // 从时序数据库获取最新值
            MetricValue response = timeSeriesDataService.getLatestMetricValue(metric.getIdentifier());
            
            Map<String, Object> result = new HashMap<>();
            
            if (response != null && response.isValid()) {
                result.put("success", true);
                result.put("uuid", uuid);
                result.put("identifier", response.getMetricIdentifier());
                result.put("value", response.getValue());
                result.put("unit", metric.getUnit());
                result.put("timestamp", response.getTimestamp());
                result.put("quality", response.getQuality());
                result.put("dataSource", response.getDataSource());
                
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("uuid", uuid);
                result.put("identifier", metric.getIdentifier());
                result.put("error", response != null ? response.getQuality() : "指标没有数据");
                result.put("value", null);
                
                return ResponseEntity.ok(result);
            }
            
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("uuid", uuid);
            result.put("error", "查询失败: " + e.getMessage());
            result.put("value", null);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    /**
     * 获取所有可用的指标标识符
     * GET /api/metrics/list
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listAllMetrics() {
        try {
            // 从配置服务获取所有指标标识符
            Map<String, BasicMetric> basicMetrics = metricConfigService.getAllBasicMetrics();
            Map<String, DerivedMetric> derivedMetrics = metricConfigService.getAllDerivedMetrics();
            
            // 合并所有指标标识符
            List<String> allIdentifiers = new java.util.ArrayList<>();
            allIdentifiers.addAll(basicMetrics.keySet());
            allIdentifiers.addAll(derivedMetrics.keySet());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", allIdentifiers.size());
            result.put("basicCount", basicMetrics.size());
            result.put("derivedCount", derivedMetrics.size());
            result.put("identifiers", allIdentifiers);
            result.put("basicMetrics", basicMetrics);
            result.put("derivedMetrics", derivedMetrics);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "获取指标列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 批量查询指标值（从时序数据库获取最新值）
     * POST /api/metrics/batch-query
     */
    @PostMapping("/batch-query")
    public ResponseEntity<Map<String, Object>> batchQueryMetrics(
            @RequestBody List<String> identifiers) {
        
        try {
            Map<String, Object> successResults = new HashMap<>();
            Map<String, Object> errorResults = new HashMap<>();
            
            for (String identifier : identifiers) {
                try {
                    // 直接从时序数据库获取最新值
                    MetricValue value = timeSeriesDataService.getLatestMetricValue(identifier);
                    
                    if (value != null && value.isValid()) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("identifier", value.getMetricIdentifier());
                        result.put("value", value.getValue());
                        result.put("unit", value.getUnit());
                        result.put("timestamp", value.getTimestamp());
                        result.put("quality", value.getQuality());
                        result.put("dataSource", value.getDataSource());
                        
                        successResults.put(identifier, result);
                    } else {
                        errorResults.put(identifier, value != null ? value.getQuality() : "指标不存在或没有数据");
                    }
                } catch (Exception e) {
                    errorResults.put(identifier, "查询失败: " + e.getMessage());
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("successCount", successResults.size());
            response.put("errorCount", errorResults.size());
            response.put("successResults", successResults);
            response.put("errorResults", errorResults);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "批量查询失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 健康检查接口
     * GET /api/metrics/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "Metric Query Service");
        result.put("timestamp", System.currentTimeMillis());
        
        // 添加时序数据库统计信息
        try {
            Map<String, Object> storageStats = timeSeriesDataService.getStorageStats();
            result.put("storageStats", storageStats);
        } catch (Exception e) {
            result.put("storageStats", "获取存储统计失败: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 清除时序数据
     * POST /api/metrics/clear-data
     */
    @PostMapping("/clear-data")
    public ResponseEntity<Map<String, Object>> clearData() {
        try {
            timeSeriesDataService.clearAllData();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "时序数据已清除");
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "清除数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
}
