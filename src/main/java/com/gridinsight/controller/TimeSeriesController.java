package com.gridinsight.controller;

import com.gridinsight.domain.model.MetricValue;
import com.gridinsight.service.TimeSeriesDataService;
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
 * 提供基于时序数据库的指标查询API
 */
@RestController
@RequestMapping("/api/timeseries")
public class TimeSeriesController {

    @Autowired
    private TimeSeriesDataService timeSeriesDataService;

    /**
     * 查询最新指标值
     */
    @GetMapping("/latest/{identifier}")
    public ResponseEntity<Map<String, Object>> getLatestMetricValue(@PathVariable String identifier) {
        Map<String, Object> response = new HashMap<>();
        try {
            MetricValue value = timeSeriesDataService.getLatestMetricValue(identifier);
            response.put("success", true);
            response.put("data", value);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 查询指标历史数据
     */
    @GetMapping("/history/{identifier}")
    public ResponseEntity<Map<String, Object>> getMetricHistory(
            @PathVariable String identifier,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            List<MetricValue> history = timeSeriesDataService.getMetricHistory(identifier, startTime, endTime);
            response.put("success", true);
            response.put("data", history);
            response.put("count", history.size());
            response.put("timeRange", Map.of(
                "startTime", startTime,
                "endTime", endTime
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "查询历史数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量查询最新指标值
     */
    @PostMapping("/latest/batch")
    public ResponseEntity<Map<String, Object>> getLatestMetricValues(@RequestBody List<String> identifiers) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, MetricValue> values = timeSeriesDataService.getLatestMetricValues(identifiers);
            response.put("success", true);
            response.put("data", values);
            response.put("count", values.size());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "批量查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取指标统计信息
     */
    @GetMapping("/statistics/{identifier}")
    public ResponseEntity<Map<String, Object>> getMetricStatistics(
            @PathVariable String identifier,
            @RequestParam(defaultValue = "1h") String timeRange) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> statistics = timeSeriesDataService.getMetricStatistics(identifier, timeRange);
            response.put("success", true);
            response.put("data", statistics);
            response.put("timeRange", timeRange);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 查询指标趋势数据
     */
    @GetMapping("/trend/{identifier}")
    public ResponseEntity<Map<String, Object>> getMetricTrend(
            @PathVariable String identifier,
            @RequestParam(defaultValue = "1d") String timeRange,
            @RequestParam(defaultValue = "1h") String interval) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // TODO: 实现趋势数据查询
            response.put("success", true);
            response.put("message", "趋势数据查询功能待实现");
            response.put("parameters", Map.of(
                "identifier", identifier,
                "timeRange", timeRange,
                "interval", interval
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "查询趋势数据失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取支持的统计时间范围
     */
    @GetMapping("/time-ranges")
    public ResponseEntity<Map<String, Object>> getSupportedTimeRanges() {
        Map<String, Object> response = new HashMap<>();
        List<String> timeRanges = List.of(
            "5m", "15m", "30m", "1h", "4h", "12h", "1d", "3d", "7d", "30d"
        );
        
        response.put("success", true);
        response.put("data", timeRanges);
        response.put("description", "支持的时间范围格式");
        
        return ResponseEntity.ok(response);
    }
}
