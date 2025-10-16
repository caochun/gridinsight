package com.gridinsight.controller;

import com.gridinsight.service.MetricSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 指标调度管理控制器
 * 提供调度任务的监控和管理功能
 */
@RestController
@RequestMapping("/api/scheduler")
public class MetricSchedulerController {

    @Autowired
    private MetricSchedulerService metricSchedulerService;

    /**
     * 获取调度统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSchedulerStatistics() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> stats = metricSchedulerService.getUpdateStatistics();
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取待更新的指标列表
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingUpdates() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<String> pending = metricSchedulerService.getPendingUpdates();
            response.put("success", true);
            response.put("data", pending);
            response.put("count", pending.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "获取待更新列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 手动触发指标更新
     */
    @PostMapping("/trigger/{identifier}")
    public ResponseEntity<Map<String, Object>> triggerMetricUpdate(@PathVariable String identifier) {
        Map<String, Object> response = new HashMap<>();
        try {
            metricSchedulerService.triggerMetricUpdate(identifier);
            response.put("success", true);
            response.put("message", "指标更新已触发: " + identifier);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "触发更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量触发指标更新
     */
    @PostMapping("/trigger/batch")
    public ResponseEntity<Map<String, Object>> triggerBatchUpdate(@RequestBody List<String> identifiers) {
        Map<String, Object> response = new HashMap<>();
        try {
            metricSchedulerService.triggerBatchMetricUpdate(identifiers);
            response.put("success", true);
            response.put("message", "批量更新已触发: " + identifiers.size() + " 个指标");
            response.put("identifiers", identifiers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "批量触发更新失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 初始化所有指标
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initializeMetrics() {
        Map<String, Object> response = new HashMap<>();
        try {
            metricSchedulerService.initializeMetrics();
            response.put("success", true);
            response.put("message", "指标初始化完成");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "指标初始化失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取调度器健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSchedulerHealth() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> stats = metricSchedulerService.getUpdateStatistics();
            List<String> pending = metricSchedulerService.getPendingUpdates();
            
            // 计算健康状态
            int totalMetrics = (Integer) stats.get("totalMetrics");
            int recentlyUpdated = (Integer) stats.get("recentlyUpdated");
            int pendingCount = pending.size();
            
            String status = "healthy";
            if (pendingCount > totalMetrics * 0.5) {
                status = "warning";
            } else if (recentlyUpdated < totalMetrics * 0.3) {
                status = "critical";
            }
            
            response.put("success", true);
            response.put("data", Map.of(
                "status", status,
                "totalMetrics", totalMetrics,
                "recentlyUpdated", recentlyUpdated,
                "pendingUpdates", pendingCount,
                "healthScore", Math.max(0, 100 - (pendingCount * 100 / totalMetrics))
            ));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "获取健康状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
