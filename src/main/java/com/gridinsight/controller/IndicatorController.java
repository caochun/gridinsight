package com.gridinsight.controller;

import com.gridinsight.service.IndicatorQueryService;
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
@RequestMapping("/api/indicators")
@CrossOrigin(origins = "*")
public class IndicatorController {

    @Autowired
    private IndicatorQueryService indicatorQueryService;

    /**
     * 根据fullIdentifier查询指标值
     * GET /api/indicators/query?fullIdentifier=xxx
     */
    @GetMapping("/query")
    public ResponseEntity<Map<String, Object>> queryIndicator(
            @RequestParam String fullIdentifier) {
        
        System.out.println("收到查询请求: " + fullIdentifier);
        try {
            IndicatorQueryService.IndicatorValueResponse response = 
                indicatorQueryService.queryIndicatorValue(fullIdentifier);
            
            Map<String, Object> result = new HashMap<>();
            
            if (response.isSuccess()) {
                result.put("success", true);
                result.put("indicatorName", response.getIndicatorName());
                result.put("fullIdentifier", response.getFullIdentifier());
                result.put("unit", response.getUnit());
                result.put("value", response.getValue());
                result.put("dataSource", response.getDataSource());
                result.put("indicatorType", response.getIndicatorType());
                
                if (response.getFormula() != null) {
                    result.put("formula", response.getFormula());
                }
                if (response.getDependencyValues() != null) {
                    result.put("dependencyValues", response.getDependencyValues());
                }
                
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("error", response.getErrorMessage());
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
     * GET /api/indicators/list
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listAllIndicators() {
        try {
            List<String> identifiers = indicatorQueryService.getAllIndicatorIdentifiers();
            
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
     * POST /api/indicators/batch-query
     */
    @PostMapping("/batch-query")
    public ResponseEntity<Map<String, Object>> batchQueryIndicators(
            @RequestBody List<String> fullIdentifiers) {
        
        try {
            Map<String, Object> results = new HashMap<>();
            Map<String, Object> successResults = new HashMap<>();
            Map<String, Object> errorResults = new HashMap<>();
            
            for (String fullIdentifier : fullIdentifiers) {
                IndicatorQueryService.IndicatorValueResponse response = 
                    indicatorQueryService.queryIndicatorValue(fullIdentifier);
                
                if (response.isSuccess()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("indicatorName", response.getIndicatorName());
                    result.put("fullIdentifier", response.getFullIdentifier());
                    result.put("unit", response.getUnit());
                    result.put("value", response.getValue());
                    result.put("dataSource", response.getDataSource());
                    result.put("indicatorType", response.getIndicatorType());
                    
                    if (response.getFormula() != null) {
                        result.put("formula", response.getFormula());
                    }
                    if (response.getDependencyValues() != null) {
                        result.put("dependencyValues", response.getDependencyValues());
                    }
                    
                    successResults.put(fullIdentifier, result);
                } else {
                    errorResults.put(fullIdentifier, response.getErrorMessage());
                }
            }
            
            results.put("success", true);
            results.put("successCount", successResults.size());
            results.put("errorCount", errorResults.size());
            results.put("successResults", successResults);
            results.put("errorResults", errorResults);
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("error", "批量查询失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResult);
        }
    }

    /**
     * 健康检查接口
     * GET /api/indicators/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "Indicator Query Service");
        result.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(result);
    }
}
