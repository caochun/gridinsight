package com.gridinsight.controller;

import com.gridinsight.domain.model.MetricValue;
import com.gridinsight.domain.service.MetricCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private MetricCalculationService metricCalculationService;

    /**
     * 根据标识符查询指标值
     * GET /api/metrics/query?identifier=xxx
     */
    @GetMapping("/query")
    public ResponseEntity<Map<String, Object>> queryMetric(
            @RequestParam String identifier) {
        
        // 收到查询请求
        try {
            MetricValue response = metricCalculationService.calculateMetric(identifier);
            
            Map<String, Object> result = new HashMap<>();
            
            if (response.isValid()) {
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
                result.put("error", response.getDataSource()); // 错误信息存储在dataSource字段中
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "服务器内部错误: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 获取所有可用的指标标识符
     * GET /api/metrics/list
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listAllMetrics() {
        try {
            List<String> identifiers = metricCalculationService.getAllMetricIdentifiers();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", identifiers.size());
            result.put("identifiers", identifiers);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "获取指标列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 批量查询指标值
     * POST /api/metrics/batch-query
     */
    @PostMapping("/batch-query")
    public ResponseEntity<Map<String, Object>> batchQueryMetrics(
            @RequestBody List<String> identifiers) {
        
        try {
            Map<String, MetricValue> results = metricCalculationService.calculateMetrics(identifiers);
            Map<String, Object> successResults = new HashMap<>();
            Map<String, Object> errorResults = new HashMap<>();
            
            for (Map.Entry<String, MetricValue> entry : results.entrySet()) {
                String identifier = entry.getKey();
                MetricValue value = entry.getValue();
                
                if (value.isValid()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("identifier", value.getMetricIdentifier());
                    result.put("value", value.getValue());
                    result.put("unit", value.getUnit());
                    result.put("timestamp", value.getTimestamp());
                    result.put("quality", value.getQuality());
                    result.put("dataSource", value.getDataSource());
                    
                    successResults.put(identifier, result);
                } else {
                    errorResults.put(identifier, value.getDataSource()); // 错误信息
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
        result.put("service", "Metric Calculation Service");
        result.put("timestamp", System.currentTimeMillis());
        
        // 添加缓存统计信息
        Map<String, Object> cacheStats = metricCalculationService.getCacheStats();
        result.put("cacheStats", cacheStats);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 清除缓存
     * POST /api/metrics/clear-cache
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<Map<String, Object>> clearCache() {
        try {
            metricCalculationService.clearCache(null);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "缓存已清除");
            result.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "清除缓存失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }
}
