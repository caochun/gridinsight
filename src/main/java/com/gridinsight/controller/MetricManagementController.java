package com.gridinsight.controller;

import com.gridinsight.domain.model.*;
import com.gridinsight.service.MetricConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 指标管理Web UI控制器
 * 提供指标定义的Web界面管理功能
 */
@Controller
@RequestMapping("/admin/metrics")
public class MetricManagementController {

    @Autowired
    private MetricConfigService metricConfigService;

    /**
     * 指标管理首页
     */
    @GetMapping
    public String index(Model model) {
        Map<String, Object> stats = metricConfigService.getMetricStats();
        model.addAttribute("stats", stats);
        return "metric-management";
    }

    /**
     * 基础指标管理页面
     */
    @GetMapping("/basic")
    public String basicMetrics(Model model) {
        Map<String, BasicMetric> basicMetrics = metricConfigService.getAllBasicMetrics();
        model.addAttribute("basicMetrics", basicMetrics);
        return "basic-metrics";
    }

    /**
     * 派生指标管理页面
     */
    @GetMapping("/derived")
    public String derivedMetrics(Model model) {
        Map<String, DerivedMetric> derivedMetrics = metricConfigService.getAllDerivedMetrics();
        model.addAttribute("derivedMetrics", derivedMetrics);
        return "derived-metrics";
    }

    /**
     * 添加基础指标页面
     */
    @GetMapping("/basic/add")
    public String addBasicMetricForm(Model model) {
        model.addAttribute("metric", new BasicMetricForm());
        model.addAttribute("dataSourceTypes", DataSource.SourceType.values());
        return "add-basic-metric";
    }

    /**
     * 添加派生指标页面
     */
    @GetMapping("/derived/add")
    public String addDerivedMetricForm(Model model) {
        model.addAttribute("metric", new DerivedMetricForm());
        
        // 创建简化的指标列表用于模板
        List<Map<String, String>> availableMetrics = metricConfigService.getAllMetrics().entrySet().stream()
                .map(entry -> {
                    Map<String, String> metricInfo = new HashMap<>();
                    metricInfo.put("identifier", entry.getKey());
                    metricInfo.put("name", entry.getValue().getName());
                    return metricInfo;
                })
                .collect(Collectors.toList());
        model.addAttribute("availableMetrics", availableMetrics);
        
        return "add-derived-metric";
    }

    /**
     * 编辑基础指标页面
     */
    @GetMapping("/basic/edit/{identifier}")
    public String editBasicMetricForm(@PathVariable String identifier, Model model) {
        BasicMetric metric = metricConfigService.getBasicMetric(identifier);
        if (metric == null) {
            return "redirect:/admin/metrics/basic?error=指标不存在";
        }
        
        BasicMetricForm form = new BasicMetricForm();
        form.setIdentifier(identifier);
        form.setName(metric.getName());
        form.setCategory(metric.getCategory());
        form.setSubCategory(metric.getSubCategory());
        form.setUnit(metric.getUnit());
        form.setDescription(metric.getDescription());
        
        if (metric.getDataSource() != null) {
            DataSource dataSource = metric.getDataSource();
            form.setSourceType(dataSource.getSourceType().name());
            form.setSourceName(dataSource.getSourceName());
            form.setRefreshInterval(dataSource.getRefreshInterval());
            form.setEnabled(dataSource.getEnabled());
            
            // 根据数据源类型填充相应配置
            switch (dataSource.getSourceType()) {
                case HTTP_API:
                    form.setHttpUrl(dataSource.getConfig("url", String.class));
                    form.setHttpMethod(dataSource.getConfig("method", String.class));
                    form.setHttpHeaders(dataSource.getConfig("headers", String.class));
                    form.setHttpTimeout(dataSource.getConfig("timeout", Integer.class) != null ? 
                        dataSource.getConfig("timeout", Integer.class) : 30000);
                    break;
                case MQTT:
                    form.setMqttBroker(dataSource.getConfig("broker", String.class));
                    form.setMqttPort(dataSource.getConfig("port", Integer.class) != null ? 
                        dataSource.getConfig("port", Integer.class) : 1883);
                    form.setMqttTopic(dataSource.getConfig("topic", String.class));
                    form.setMqttQos(dataSource.getConfig("qos", Integer.class) != null ? 
                        dataSource.getConfig("qos", Integer.class) : 0);
                    form.setMqttClientId(dataSource.getConfig("clientId", String.class));
                    form.setMqttUsername(dataSource.getConfig("username", String.class));
                    form.setMqttPassword(dataSource.getConfig("password", String.class));
                    break;
                case DATABASE:
                    form.setDbConnectionString(dataSource.getConfig("connectionString", String.class));
                    form.setDbUsername(dataSource.getConfig("username", String.class));
                    form.setDbPassword(dataSource.getConfig("password", String.class));
                    form.setDbQuery(dataSource.getConfig("query", String.class));
                    form.setDbDriver(dataSource.getConfig("driver", String.class));
                    break;
                case FILE:
                    form.setFilePath(dataSource.getConfig("filePath", String.class));
                    form.setFileEncoding(dataSource.getConfig("encoding", String.class));
                    form.setFileFormat(dataSource.getConfig("format", String.class));
                    form.setFileDelimiter(dataSource.getConfig("delimiter", String.class));
                    break;
            }
            
            // 设置兼容性字段
            form.setSourceAddress(dataSource.getSourceAddress());
        }
        
        model.addAttribute("metric", form);
        model.addAttribute("dataSourceTypes", DataSource.SourceType.values());
        return "edit-basic-metric";
    }

    /**
     * 编辑派生指标页面
     */
    @GetMapping("/derived/edit/{identifier}")
    public String editDerivedMetricForm(@PathVariable String identifier, Model model) {
        DerivedMetric metric = metricConfigService.getDerivedMetric(identifier);
        if (metric == null) {
            return "redirect:/admin/metrics/derived?error=指标不存在";
        }
        
        DerivedMetricForm form = new DerivedMetricForm();
        form.setIdentifier(identifier);
        form.setName(metric.getName());
        form.setCategory(metric.getCategory());
        form.setSubCategory(metric.getSubCategory());
        form.setUnit(metric.getUnit());
        form.setDescription(metric.getDescription());
        form.setFormula(metric.getFormula());
        form.setUpdateStrategy(metric.getUpdateStrategy().name());
        form.setCalculationInterval(metric.getCalculationInterval());
        
        List<String> dependencies = metric.getDependencies().stream()
                .map(Metric::getIdentifier)
                .collect(Collectors.toList());
        form.setDependencies(dependencies);
        
        model.addAttribute("metric", form);
        
        // 创建简化的指标列表用于模板
        List<Map<String, String>> availableMetrics = metricConfigService.getAllMetrics().entrySet().stream()
                .map(entry -> {
                    Map<String, String> metricInfo = new HashMap<>();
                    metricInfo.put("identifier", entry.getKey());
                    metricInfo.put("name", entry.getValue().getName());
                    return metricInfo;
                })
                .collect(Collectors.toList());
        model.addAttribute("availableMetrics", availableMetrics);
        
        return "edit-derived-metric";
    }

    /**
     * 保存基础指标
     */
    @PostMapping("/basic/save")
    public String saveBasicMetric(@ModelAttribute BasicMetricForm form) {
        try {
            // 根据数据源类型创建数据源
            DataSource dataSource = createDataSourceFromForm(form);

            // 创建基础指标
            BasicMetric metric = new BasicMetric(
                form.getName(),
                form.getCategory(),
                form.getSubCategory(),
                form.getUnit(),
                form.getDescription(),
                dataSource
            );

            // 保存指标
            metricConfigService.addBasicMetric(form.getIdentifier(), metric);
            
            return "redirect:/admin/metrics/basic?success=基础指标保存成功";
        } catch (Exception e) {
            return "redirect:/admin/metrics/basic/add?error=" + e.getMessage();
        }
    }

    /**
     * 保存派生指标
     */
    @PostMapping("/derived/save")
    public String saveDerivedMetric(@ModelAttribute DerivedMetricForm form) {
        try {
            // 创建依赖指标列表
            List<Metric> dependencies = form.getDependencies().stream()
                    .map(metricConfigService::getMetric)
                    .filter(metric -> metric != null)
                    .collect(Collectors.toList());

            // 解析更新策略
            DerivedMetricUpdateStrategy updateStrategy;
            try {
                updateStrategy = DerivedMetricUpdateStrategy.valueOf(form.getUpdateStrategy());
            } catch (Exception e) {
                updateStrategy = DerivedMetricUpdateStrategy.REALTIME;
            }

            // 创建派生指标
            DerivedMetric metric = new DerivedMetric(
                form.getName(),
                form.getCategory(),
                form.getSubCategory(),
                form.getUnit(),
                form.getDescription(),
                form.getFormula(),
                dependencies,
                updateStrategy,
                form.getCalculationInterval()
            );

            // 保存指标
            metricConfigService.addDerivedMetric(form.getIdentifier(), metric);
            
            return "redirect:/admin/metrics/derived?success=派生指标保存成功";
        } catch (Exception e) {
            return "redirect:/admin/metrics/derived/add?error=" + e.getMessage();
        }
    }

    /**
     * 更新基础指标
     */
    @PostMapping("/basic/update")
    public String updateBasicMetric(@ModelAttribute BasicMetricForm form) {
        try {
            // 根据数据源类型创建数据源
            DataSource dataSource = createDataSourceFromForm(form);

            // 创建基础指标
            BasicMetric metric = new BasicMetric(
                form.getName(),
                form.getCategory(),
                form.getSubCategory(),
                form.getUnit(),
                form.getDescription(),
                dataSource
            );

            // 更新指标
            metricConfigService.updateBasicMetric(form.getIdentifier(), metric);
            
            return "redirect:/admin/metrics/basic?success=基础指标更新成功";
        } catch (Exception e) {
            return "redirect:/admin/metrics/basic/edit/" + form.getIdentifier() + "?error=" + e.getMessage();
        }
    }

    /**
     * 更新派生指标
     */
    @PostMapping("/derived/update")
    public String updateDerivedMetric(@ModelAttribute DerivedMetricForm form) {
        try {
            // 创建依赖指标列表
            List<Metric> dependencies = form.getDependencies().stream()
                    .map(metricConfigService::getMetric)
                    .filter(metric -> metric != null)
                    .collect(Collectors.toList());

            // 解析更新策略
            DerivedMetricUpdateStrategy updateStrategy;
            try {
                updateStrategy = DerivedMetricUpdateStrategy.valueOf(form.getUpdateStrategy());
            } catch (Exception e) {
                updateStrategy = DerivedMetricUpdateStrategy.REALTIME;
            }

            // 创建派生指标
            DerivedMetric metric = new DerivedMetric(
                form.getName(),
                form.getCategory(),
                form.getSubCategory(),
                form.getUnit(),
                form.getDescription(),
                form.getFormula(),
                dependencies,
                updateStrategy,
                form.getCalculationInterval()
            );

            // 更新指标
            metricConfigService.updateDerivedMetric(form.getIdentifier(), metric);
            
            return "redirect:/admin/metrics/derived?success=派生指标更新成功";
        } catch (Exception e) {
            return "redirect:/admin/metrics/derived/edit/" + form.getIdentifier() + "?error=" + e.getMessage();
        }
    }

    /**
     * 删除指标
     */
    @PostMapping("/delete/{identifier}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMetric(@PathVariable String identifier) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = metricConfigService.removeMetric(identifier);
            if (deleted) {
                response.put("success", true);
                response.put("message", "指标删除成功");
            } else {
                response.put("success", false);
                response.put("message", "指标不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 获取指标详情API
     */
    @GetMapping("/api/{identifier}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMetricDetails(@PathVariable String identifier) {
        Map<String, Object> response = new HashMap<>();
        Metric metric = metricConfigService.getMetric(identifier);
        
        if (metric != null) {
            response.put("success", true);
            response.put("metric", metric);
        } else {
            response.put("success", false);
            response.put("message", "指标不存在");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取指标详情API（支持查询参数形式，避免路径变量中包含点/中文导致的问题）
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMetricDetailsByParam(@RequestParam("identifier") String identifier) {
        Map<String, Object> response = new HashMap<>();
        Metric metric = metricConfigService.getMetric(identifier);

        if (metric != null) {
            response.put("success", true);
            response.put("metric", metric);
        } else {
            response.put("success", false);
            response.put("message", "指标不存在");
        }

        return ResponseEntity.ok(response);
    }

    // ========== 辅助方法 ==========
    
    /**
     * 根据表单数据创建数据源
     */
    private DataSource createDataSourceFromForm(BasicMetricForm form) {
        DataSource.SourceType sourceType = DataSource.SourceType.valueOf(form.getSourceType());
        
        switch (sourceType) {
            case HTTP_API:
                Map<String, String> headers = new HashMap<>();
                if (form.getHttpHeaders() != null && !form.getHttpHeaders().trim().isEmpty()) {
                    // 解析请求头字符串，格式：key1:value1,key2:value2
                    String[] headerPairs = form.getHttpHeaders().split(",");
                    for (String pair : headerPairs) {
                        String[] keyValue = pair.split(":", 2);
                        if (keyValue.length == 2) {
                            headers.put(keyValue[0].trim(), keyValue[1].trim());
                        }
                    }
                }
                return DataSource.createHttpApi(
                    form.getHttpUrl(),
                    form.getHttpMethod(),
                    headers.isEmpty() ? null : headers,
                    form.getSourceName(),
                    form.getDescription(),
                    form.getRefreshInterval()
                );
                
            case MQTT:
                return DataSource.createMqtt(
                    form.getMqttBroker(),
                    form.getMqttPort(),
                    form.getMqttTopic(),
                    form.getMqttQos(),
                    form.getSourceName(),
                    form.getDescription(),
                    form.getRefreshInterval()
                );
                
            case DATABASE:
                return DataSource.createDatabase(
                    form.getDbConnectionString(),
                    form.getDbUsername(),
                    form.getDbPassword(),
                    form.getDbQuery(),
                    form.getDbDriver(),
                    form.getSourceName(),
                    form.getDescription(),
                    form.getRefreshInterval()
                );
                
            case FILE:
                return DataSource.createFile(
                    form.getFilePath(),
                    form.getFileEncoding(),
                    form.getFileFormat(),
                    form.getFileDelimiter(),
                    form.getSourceName(),
                    form.getDescription(),
                    form.getRefreshInterval()
                );
                
            default:
                throw new IllegalArgumentException("不支持的数据源类型: " + sourceType);
        }
    }

    // ========== 表单类定义 ==========

    /**
     * 基础指标表单类
     */
    public static class BasicMetricForm {
        private String identifier;
        private String name;
        private String category;
        private String subCategory;
        private String unit;
        private String description;
        private String sourceType;
        private String sourceName;
        private int refreshInterval = 300;
        private boolean enabled = true;
        
        // HTTP API 配置
        private String httpUrl;
        private String httpMethod = "GET";
        private String httpHeaders;
        private int httpTimeout = 30000;
        
        // MQTT 配置
        private String mqttBroker;
        private int mqttPort = 1883;
        private String mqttTopic;
        private int mqttQos = 0;
        private String mqttClientId;
        private String mqttUsername;
        private String mqttPassword;
        
        // DATABASE 配置
        private String dbConnectionString;
        private String dbUsername;
        private String dbPassword;
        private String dbQuery;
        private String dbDriver;
        
        // FILE 配置
        private String filePath;
        private String fileEncoding = "UTF-8";
        private String fileFormat = "CSV";
        private String fileDelimiter = ",";
        
        // 兼容性字段
        private String sourceAddress;

        // Getters and Setters
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getSubCategory() { return subCategory; }
        public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }
        public String getSourceName() { return sourceName; }
        public void setSourceName(String sourceName) { this.sourceName = sourceName; }
        public int getRefreshInterval() { return refreshInterval; }
        public void setRefreshInterval(int refreshInterval) { this.refreshInterval = refreshInterval; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        
        // HTTP API Getters and Setters
        public String getHttpUrl() { return httpUrl; }
        public void setHttpUrl(String httpUrl) { this.httpUrl = httpUrl; }
        public String getHttpMethod() { return httpMethod; }
        public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
        public String getHttpHeaders() { return httpHeaders; }
        public void setHttpHeaders(String httpHeaders) { this.httpHeaders = httpHeaders; }
        public int getHttpTimeout() { return httpTimeout; }
        public void setHttpTimeout(int httpTimeout) { this.httpTimeout = httpTimeout; }
        
        // MQTT Getters and Setters
        public String getMqttBroker() { return mqttBroker; }
        public void setMqttBroker(String mqttBroker) { this.mqttBroker = mqttBroker; }
        public int getMqttPort() { return mqttPort; }
        public void setMqttPort(int mqttPort) { this.mqttPort = mqttPort; }
        public String getMqttTopic() { return mqttTopic; }
        public void setMqttTopic(String mqttTopic) { this.mqttTopic = mqttTopic; }
        public int getMqttQos() { return mqttQos; }
        public void setMqttQos(int mqttQos) { this.mqttQos = mqttQos; }
        public String getMqttClientId() { return mqttClientId; }
        public void setMqttClientId(String mqttClientId) { this.mqttClientId = mqttClientId; }
        public String getMqttUsername() { return mqttUsername; }
        public void setMqttUsername(String mqttUsername) { this.mqttUsername = mqttUsername; }
        public String getMqttPassword() { return mqttPassword; }
        public void setMqttPassword(String mqttPassword) { this.mqttPassword = mqttPassword; }
        
        // DATABASE Getters and Setters
        public String getDbConnectionString() { return dbConnectionString; }
        public void setDbConnectionString(String dbConnectionString) { this.dbConnectionString = dbConnectionString; }
        public String getDbUsername() { return dbUsername; }
        public void setDbUsername(String dbUsername) { this.dbUsername = dbUsername; }
        public String getDbPassword() { return dbPassword; }
        public void setDbPassword(String dbPassword) { this.dbPassword = dbPassword; }
        public String getDbQuery() { return dbQuery; }
        public void setDbQuery(String dbQuery) { this.dbQuery = dbQuery; }
        public String getDbDriver() { return dbDriver; }
        public void setDbDriver(String dbDriver) { this.dbDriver = dbDriver; }
        
        // FILE Getters and Setters
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public String getFileEncoding() { return fileEncoding; }
        public void setFileEncoding(String fileEncoding) { this.fileEncoding = fileEncoding; }
        public String getFileFormat() { return fileFormat; }
        public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }
        public String getFileDelimiter() { return fileDelimiter; }
        public void setFileDelimiter(String fileDelimiter) { this.fileDelimiter = fileDelimiter; }
        
        // 兼容性字段
        public String getSourceAddress() { return sourceAddress; }
        public void setSourceAddress(String sourceAddress) { this.sourceAddress = sourceAddress; }
    }

    /**
     * 派生指标表单类
     */
    public static class DerivedMetricForm {
        private String identifier;
        private String name;
        private String category;
        private String subCategory;
        private String unit;
        private String description;
        private String formula;
        private List<String> dependencies;
        private String updateStrategy = "REALTIME";
        private Integer calculationInterval = 300;

        // Getters and Setters
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getSubCategory() { return subCategory; }
        public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getFormula() { return formula; }
        public void setFormula(String formula) { this.formula = formula; }
        public List<String> getDependencies() { return dependencies; }
        public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
        public String getUpdateStrategy() { return updateStrategy; }
        public void setUpdateStrategy(String updateStrategy) { this.updateStrategy = updateStrategy; }
        public Integer getCalculationInterval() { return calculationInterval; }
        public void setCalculationInterval(Integer calculationInterval) { this.calculationInterval = calculationInterval; }
    }
}
