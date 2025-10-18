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
    @GetMapping("/basic/edit/{identifier:.+}")
    public String editBasicMetricForm(@PathVariable String identifier, Model model) {
        try {
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
                        String url = dataSource.getConfig("url", String.class);
                        String method = dataSource.getConfig("method", String.class);
                        String headers = dataSource.getConfig("headers", String.class);
                        Integer timeout = dataSource.getConfig("timeout", Integer.class);

                        form.setHttpUrl(url != null ? url : "");
                        form.setHttpMethod(method != null ? method : "GET");
                        form.setHttpHeaders(headers != null ? headers : "");
                        form.setHttpTimeout(timeout != null ? timeout : 30000);
                        break;
                    case MQTT:
                        String broker = dataSource.getConfig("broker", String.class);
                        Integer port = dataSource.getConfig("port", Integer.class);
                        String topic = dataSource.getConfig("topic", String.class);
                        Integer qos = dataSource.getConfig("qos", Integer.class);
                        String clientId = dataSource.getConfig("clientId", String.class);
                        String mqttUsername = dataSource.getConfig("username", String.class);
                        String mqttPassword = dataSource.getConfig("password", String.class);

                        form.setMqttBroker(broker != null ? broker : "");
                        form.setMqttPort(port != null ? port : 1883);
                        form.setMqttTopic(topic != null ? topic : "");
                        form.setMqttQos(qos != null ? qos : 0);
                        form.setMqttClientId(clientId != null ? clientId : "");
                        form.setMqttUsername(mqttUsername != null ? mqttUsername : "");
                        form.setMqttPassword(mqttPassword != null ? mqttPassword : "");
                        break;
                    case DATABASE:
                        String connectionString = dataSource.getConfig("connectionString", String.class);
                        String username = dataSource.getConfig("username", String.class);
                        String password = dataSource.getConfig("password", String.class);
                        String query = dataSource.getConfig("query", String.class);
                        String driver = dataSource.getConfig("driver", String.class);

                        form.setDbConnectionString(connectionString != null ? connectionString : "");
                        form.setDbUsername(username != null ? username : "");
                        form.setDbPassword(password != null ? password : "");
                        form.setDbQuery(query != null ? query : "");
                        form.setDbDriver(driver != null ? driver : "");
                        break;
                    case FILE:
                        String filePath = dataSource.getConfig("filePath", String.class);
                        String encoding = dataSource.getConfig("encoding", String.class);
                        String format = dataSource.getConfig("format", String.class);
                        String delimiter = dataSource.getConfig("delimiter", String.class);

                        form.setFilePath(filePath != null ? filePath : "");
                        form.setFileEncoding(encoding != null ? encoding : "UTF-8");
                        form.setFileFormat(format != null ? format : "CSV");
                        form.setFileDelimiter(delimiter != null ? delimiter : ",");
                        break;
                }

                // 设置兼容性字段
                form.setSourceAddress(dataSource.getSourceAddress());
            }

            model.addAttribute("metric", form);
            model.addAttribute("dataSourceTypes", DataSource.SourceType.values());
            return "edit-basic-metric";
        } catch (Exception e) {
            return "redirect:/admin/metrics/basic?error=编辑失败: " + e.getMessage();
        }
    }

    /**
     * 编辑派生指标页面
     */
    @GetMapping("/derived/edit/{identifier:.+}")
    public String editDerivedMetricForm(@PathVariable String identifier, Model model) {
        try {
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
        // updateStrategy和calculationInterval已移除，所有派生指标现在都使用事件驱动机制
        
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
        } catch (Exception e) {
            return "redirect:/admin/metrics/derived?error=编辑失败: " + e.getMessage();
        }
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
            
            return handleSuccess("/admin/metrics/basic", "基础指标保存成功");
        } catch (Exception e) {
            return handleException(e, "/admin/metrics/basic", "/admin/metrics/basic/add");
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
                dependencies
            );

            // 保存指标
            metricConfigService.addDerivedMetric(form.getIdentifier(), metric);
            
            return handleSuccess("/admin/metrics/derived", "派生指标保存成功");
        } catch (Exception e) {
            return handleException(e, "/admin/metrics/derived", "/admin/metrics/derived/add");
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
            
            return handleSuccess("/admin/metrics/basic", "基础指标更新成功");
        } catch (Exception e) {
            return handleException(e, "/admin/metrics/basic", "/admin/metrics/basic/edit/" + form.getIdentifier());
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
                dependencies
            );

            // 更新指标
            metricConfigService.updateDerivedMetric(form.getIdentifier(), metric);
            
            return handleSuccess("/admin/metrics/derived", "派生指标更新成功");
        } catch (Exception e) {
            return handleException(e, "/admin/metrics/derived", "/admin/metrics/derived/edit/" + form.getIdentifier());
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
    public ResponseEntity<Map<String, Object>> getMetricDetailsByParam(
            @RequestParam(value = "identifier", required = false) String identifier,
            @RequestParam(value = "uuid", required = false) String uuid) {
        Map<String, Object> response = new HashMap<>();
        Metric metric = null;
        
        // 优先使用UUID查询，如果没有UUID则使用identifier
        if (uuid != null && !uuid.trim().isEmpty()) {
            metric = metricConfigService.getMetricByUuid(uuid);
        } else if (identifier != null && !identifier.trim().isEmpty()) {
            metric = metricConfigService.getMetric(identifier);
        }

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

    // ========== 工具方法 ==========
    
    /**
     * 处理异常并返回重定向URL
     */
    private String handleException(Exception e, String successUrl, String errorUrl) {
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            return "redirect:" + errorUrl + "?error=" + e.getMessage();
        } else {
            return "redirect:" + errorUrl + "?error=操作失败";
        }
    }
    
    /**
     * 处理成功并返回重定向URL
     */
    private String handleSuccess(String successUrl, String message) {
        return "redirect:" + successUrl + "?success=" + message;
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
