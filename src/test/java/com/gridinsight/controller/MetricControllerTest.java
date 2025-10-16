package com.gridinsight.controller;

import com.gridinsight.domain.model.*;
import com.gridinsight.domain.service.MetricCalculationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MetricController测试
 */
@WebMvcTest(MetricController.class)
public class MetricControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private MetricCalculationService metricCalculationService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private MetricValue mockMetricValue;
    
    @BeforeEach
    void setUp() {
        mockMetricValue = MetricValue.good(
            "中压拓扑.配变统计.配变总数",
            1000.0,
            "个"
        );
    }
    
    @Test
    void testQueryMetric_Success() throws Exception {
        // 模拟服务返回
        when(metricCalculationService.calculateMetric(any(String.class)))
            .thenReturn(mockMetricValue);
        
        // 执行请求
        mockMvc.perform(get("/api/metrics/query")
                .param("identifier", "中压拓扑.配变统计.配变总数"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.identifier").value("中压拓扑.配变统计.配变总数"))
                .andExpect(jsonPath("$.value").value(1000.0))
                .andExpect(jsonPath("$.unit").value("个"));
    }
    
    @Test
    void testQueryMetric_Error() throws Exception {
        // 模拟服务返回错误
        MetricValue errorValue = MetricValue.error("测试指标", "指标不存在");
        when(metricCalculationService.calculateMetric(any(String.class)))
            .thenReturn(errorValue);
        
        // 执行请求
        mockMvc.perform(get("/api/metrics/query")
                .param("identifier", "不存在的指标"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Error: 指标不存在"));
    }
    
    @Test
    void testListMetrics() throws Exception {
        // 模拟服务返回指标列表
        when(metricCalculationService.getAllMetricIdentifiers())
            .thenReturn(Arrays.asList(
                "中压拓扑.配变统计.配变总数",
                "中压拓扑.拓扑不一致.不一致数量"
            ));
        
        // 执行请求
        mockMvc.perform(get("/api/metrics/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.identifiers").isArray())
                .andExpect(jsonPath("$.identifiers[0]").value("中压拓扑.配变统计.配变总数"));
    }
    
    @Test
    void testBatchQuery() throws Exception {
        // 准备请求数据
        List<String> identifiers = Arrays.asList(
            "中压拓扑.配变统计.配变总数",
            "中压拓扑.拓扑不一致.不一致数量"
        );
        
        // 模拟服务返回
        Map<String, MetricValue> results = new HashMap<>();
        results.put("中压拓扑.配变统计.配变总数", 
            MetricValue.good("中压拓扑.配变统计.配变总数", 1000.0, "个"));
        results.put("中压拓扑.拓扑不一致.不一致数量", 
            MetricValue.good("中压拓扑.拓扑不一致.不一致数量", 50.0, "个"));
        
        when(metricCalculationService.calculateMetrics(any(List.class)))
            .thenReturn(results);
        
        // 执行请求
        mockMvc.perform(post("/api/metrics/batch-query")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(identifiers)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.successCount").value(2))
                .andExpect(jsonPath("$.errorCount").value(0))
                .andExpect(jsonPath("$.successResults").isMap());
    }
    
    @Test
    void testHealthCheck() throws Exception {
        // 模拟服务返回缓存统计
        Map<String, Object> cacheStats = new HashMap<>();
        cacheStats.put("cacheSize", 5);
        cacheStats.put("totalMetrics", 10);
        
        when(metricCalculationService.getCacheStats())
            .thenReturn(cacheStats);
        
        // 执行请求
        mockMvc.perform(get("/api/metrics/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Metric Calculation Service"))
                .andExpect(jsonPath("$.cacheStats").isMap());
    }
    
    @Test
    void testClearCache() throws Exception {
        // 执行请求
        mockMvc.perform(post("/api/metrics/clear-cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("缓存已清除"));
    }
    
    @Test
    void testQueryMetric_MissingParameter() throws Exception {
        // 执行请求（缺少参数）
        mockMvc.perform(get("/api/metrics/query"))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testBatchQuery_EmptyList() throws Exception {
        // 执行请求（空列表）
        mockMvc.perform(post("/api/metrics/batch-query")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.successCount").value(0));
    }
}
