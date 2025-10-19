package com.gridinsight.service;

import com.gridinsight.domain.model.*;
import com.gridinsight.domain.service.MetricCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 外部指标配置管理服务
 */
@Service
public class ExternalMetricConfigService {

    private final Map<String, BasicMetric> basicMetrics = new ConcurrentHashMap<>();
    private final Map<String, DerivedMetric> derivedMetrics = new ConcurrentHashMap<>();
    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    @Autowired
    private MetricCalculationService metricCalculationService;

    @Value("${gridinsight.metrics.basic-config-file:config/metrics/basic-metrics.yaml}")
    private String basicConfigFile;

    @Value("${gridinsight.metrics.derived-config-file:config/metrics/derived-metrics.yaml}")
    private String derivedConfigFile;

    @PostConstruct
    public void initialize() {
        loadConfigurations();
    }

    /**
     * 加载所有配置
     */
    public void loadConfigurations() {
        loadBasicMetrics();
        loadDerivedMetrics();
        syncToCalculationService();
        System.out.println("外部指标配置加载完成 - 基础指标: " + basicMetrics.size() + ", 派生指标: " + derivedMetrics.size());
    }


    /**
     * 加载基础指标配置
     */
    private void loadBasicMetrics() {
        try {
            File configFile = new File(basicConfigFile);
            if (!configFile.exists()) {
                System.out.println("基础指标配置文件不存在: " + basicConfigFile);
                return;
            }

            InputStream inputStream = new FileInputStream(configFile);
            LoaderOptions loaderOptions = new LoaderOptions();
            Constructor constructor = new Constructor(BasicMetricsConfig.class, loaderOptions);
            Yaml yaml = new Yaml(constructor);
            BasicMetricsConfig config = yaml.load(inputStream);

            if (config.getBasicMetrics() != null) {
                // 清空现有配置
                basicMetrics.clear();
                dataSources.clear();

                for (Map.Entry<String, BasicMetricConfig> entry : config.getBasicMetrics().entrySet()) {
                    String identifier = entry.getKey();
                    BasicMetricConfig metricConfig = entry.getValue();
                    
                    BasicMetric metric = createBasicMetric(identifier, metricConfig);
                    basicMetrics.put(identifier, metric);
                    dataSources.put(identifier, metric.getDataSource());
                }
            }

            inputStream.close();
            System.out.println("基础指标配置加载完成: " + basicMetrics.size() + " 个指标");
        } catch (Exception e) {
            System.err.println("加载基础指标配置失败: " + e.getMessage());
        }
    }

    /**
     * 加载派生指标配置
     */
    private void loadDerivedMetrics() {
        try {
            File configFile = new File(derivedConfigFile);
            if (!configFile.exists()) {
                System.out.println("派生指标配置文件不存在: " + derivedConfigFile);
                return;
            }

            InputStream inputStream = new FileInputStream(configFile);
            LoaderOptions loaderOptions = new LoaderOptions();
            Constructor constructor = new Constructor(DerivedMetricsConfig.class, loaderOptions);
            Yaml yaml = new Yaml(constructor);
            DerivedMetricsConfig config = yaml.load(inputStream);

            if (config.getDerivedMetrics() != null) {
                // 清空现有配置
                derivedMetrics.clear();

                for (Map.Entry<String, DerivedMetricConfig> entry : config.getDerivedMetrics().entrySet()) {
                    String identifier = entry.getKey();
                    DerivedMetricConfig metricConfig = entry.getValue();
                    
                    DerivedMetric metric = createDerivedMetric(identifier, metricConfig);
                    derivedMetrics.put(identifier, metric);
                }
            }

            inputStream.close();
            System.out.println("派生指标配置加载完成: " + derivedMetrics.size() + " 个指标");
        } catch (Exception e) {
            System.err.println("加载派生指标配置失败: " + e.getMessage());
        }
    }

    /**
     * 将已加载的指标同步到计算服务
     */
    private void syncToCalculationService() {
        // 清理并重新注册
        basicMetrics.forEach((id, m) -> metricCalculationService.addMetric(m));
        derivedMetrics.forEach((id, m) -> metricCalculationService.addMetric(m));
    }

    /**
     * 创建基础指标
     */
    private BasicMetric createBasicMetric(String identifier, BasicMetricConfig config) {
        DataSource dataSource = createDataSource(config.getDataSource());
        
        BasicMetric metric = new BasicMetric(
            config.getName(),
            config.getCategory(),
            config.getSubCategory(),
            config.getUnit(),
            config.getDescription(),
            dataSource
        );
        
        metric.setIdentifier(identifier);
        metric.setUuid(config.getUuid());
        
        return metric;
    }

    /**
     * 创建派生指标
     */
    private DerivedMetric createDerivedMetric(String identifier, DerivedMetricConfig config) {
        List<Metric> dependencies = new ArrayList<>();
        if (config.getDependencies() != null) {
            for (String depId : config.getDependencies()) {
                Metric dep = getMetric(depId);
                if (dep != null) {
                    dependencies.add(dep);
                }
            }
        }
        
        DerivedMetric metric = new DerivedMetric(
            config.getName(),
            config.getCategory(),
            config.getSubCategory(),
            config.getUnit(),
            config.getDescription(),
            config.getFormula(),
            dependencies
        );
        
        metric.setIdentifier(identifier);
        metric.setUuid(config.getUuid());
        
        return metric;
    }

    /**
     * 创建数据源
     */
    private DataSource createDataSource(DataSourceConfig config) {
        if (config == null) {
            return null;
        }
        
        DataSource.SourceType sourceType = DataSource.SourceType.valueOf(config.getSourceType());
        
        switch (sourceType) {
            case HTTP_API:
                return DataSource.createHttpApi(
                    config.getSourceAddress(),
                    "GET",
                    null,
                    config.getSourceName(),
                    "从外部配置文件加载",
                    config.getRefreshInterval()
                );
            case MQTT:
                return DataSource.createMqtt(
                    "localhost",
                    1883,
                    "topic",
                    0,
                    config.getSourceName(),
                    "从外部配置文件加载",
                    config.getRefreshInterval()
                );
            case DATABASE:
                String connectionString = config.getSourceAddress();
                String username = config.getConfig() != null ? (String) config.getConfig().get("username") : "";
                String password = config.getConfig() != null ? (String) config.getConfig().get("password") : "";
                String query = config.getConfig() != null ? (String) config.getConfig().get("query") : "SELECT * FROM metrics";
                String driver = config.getConfig() != null ? (String) config.getConfig().get("driver") : "org.sqlite.JDBC";
                
                return DataSource.createDatabase(
                    connectionString,
                    username,
                    password,
                    query,
                    driver,
                    config.getSourceName(),
                    "从外部配置文件加载",
                    config.getRefreshInterval()
                );
            case FILE:
                return DataSource.createFile(
                    config.getSourceAddress(),
                    "UTF-8",
                    "CSV",
                    ",",
                    config.getSourceName(),
                    "从外部配置文件加载",
                    config.getRefreshInterval()
                );
            default:
                return null;
        }
    }

    // ========== 公共API方法 ==========

    public Map<String, BasicMetric> getAllBasicMetrics() {
        return new HashMap<>(basicMetrics);
    }

    public Map<String, DerivedMetric> getAllDerivedMetrics() {
        return new HashMap<>(derivedMetrics);
    }

    public Map<String, Metric> getAllMetrics() {
        Map<String, Metric> allMetrics = new HashMap<>();
        allMetrics.putAll(basicMetrics);
        allMetrics.putAll(derivedMetrics);
        return allMetrics;
    }

    public BasicMetric getBasicMetric(String identifier) {
        return basicMetrics.get(identifier);
    }

    public DerivedMetric getDerivedMetric(String identifier) {
        return derivedMetrics.get(identifier);
    }

    public Metric getMetric(String identifier) {
        Metric metric = basicMetrics.get(identifier);
        if (metric == null) {
            metric = derivedMetrics.get(identifier);
        }
        return metric;
    }

    public Metric getMetricByUuid(String uuid) {
        // 先查找基础指标
        for (BasicMetric metric : basicMetrics.values()) {
            if (uuid.equals(metric.getUuid())) {
                return metric;
            }
        }
        // 再查找派生指标
        for (DerivedMetric metric : derivedMetrics.values()) {
            if (uuid.equals(metric.getUuid())) {
                return metric;
            }
        }
        return null;
    }

    public Map<String, Object> getMetricStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBasicMetrics", basicMetrics.size());
        stats.put("totalDerivedMetrics", derivedMetrics.size());
        stats.put("totalMetrics", basicMetrics.size() + derivedMetrics.size());
        stats.put("totalDataSources", dataSources.size());
        return stats;
    }

    // ========== 管理方法 ==========
    
    public void addBasicMetric(String identifier, BasicMetric metric) {
        basicMetrics.put(identifier, metric);
        if (metric.getDataSource() != null) {
            dataSources.put(identifier, metric.getDataSource());
        }
        // 同步到计算服务
        metricCalculationService.addMetric(metric);
    }

    public void addDerivedMetric(String identifier, DerivedMetric metric) {
        derivedMetrics.put(identifier, metric);
        // 同步到计算服务
        metricCalculationService.addMetric(metric);
    }

    public void updateBasicMetric(String identifier, BasicMetric metric) {
        basicMetrics.put(identifier, metric);
        if (metric.getDataSource() != null) {
            dataSources.put(identifier, metric.getDataSource());
        }
        // 同步到计算服务
        metricCalculationService.addMetric(metric);
    }

    public void updateDerivedMetric(String identifier, DerivedMetric metric) {
        derivedMetrics.put(identifier, metric);
        // 同步到计算服务
        metricCalculationService.addMetric(metric);
    }

    public boolean removeMetric(String identifier) {
        boolean removed = false;
        if (basicMetrics.remove(identifier) != null) {
            dataSources.remove(identifier);
            removed = true;
        }
        if (derivedMetrics.remove(identifier) != null) {
            removed = true;
        }
        return removed;
    }

    public BasicMetric getBasicMetricByUuid(String uuid) {
        for (BasicMetric metric : basicMetrics.values()) {
            if (uuid.equals(metric.getUuid())) {
                return metric;
            }
        }
        return null;
    }

    public DerivedMetric getDerivedMetricByUuid(String uuid) {
        for (DerivedMetric metric : derivedMetrics.values()) {
            if (uuid.equals(metric.getUuid())) {
                return metric;
            }
        }
        return null;
    }

    // ========== 配置类定义 ==========

    public static class BasicMetricConfig {
        private String name;
        private String category;
        private String subCategory;
        private String unit;
        private String description;
        private String uuid;
        private DataSourceConfig dataSource;

        // Getters and Setters
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
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public DataSourceConfig getDataSource() { return dataSource; }
        public void setDataSource(DataSourceConfig dataSource) { this.dataSource = dataSource; }
    }

    public static class DerivedMetricConfig {
        private String name;
        private String category;
        private String subCategory;
        private String unit;
        private String description;
        private String uuid;
        private String formula;
        private List<String> dependencies;

        // Getters and Setters
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
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getFormula() { return formula; }
        public void setFormula(String formula) { this.formula = formula; }
        public List<String> getDependencies() { return dependencies; }
        public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
    }

    public static class DataSourceConfig {
        private String sourceType;
        private String sourceAddress;
        private String sourceName;
        private Integer refreshInterval;
        private boolean enabled;
        private Map<String, Object> config;

        // Getters and Setters
        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }
        public String getSourceAddress() { return sourceAddress; }
        public void setSourceAddress(String sourceAddress) { this.sourceAddress = sourceAddress; }
        public String getSourceName() { return sourceName; }
        public void setSourceName(String sourceName) { this.sourceName = sourceName; }
        public Integer getRefreshInterval() { return refreshInterval; }
        public void setRefreshInterval(Integer refreshInterval) { this.refreshInterval = refreshInterval; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public Map<String, Object> getConfig() { return config; }
        public void setConfig(Map<String, Object> config) { this.config = config; }
    }

    public static class BasicMetricsConfig {
        private Map<String, BasicMetricConfig> basicMetrics;
        public Map<String, BasicMetricConfig> getBasicMetrics() { return basicMetrics; }
        public void setBasicMetrics(Map<String, BasicMetricConfig> basicMetrics) { this.basicMetrics = basicMetrics; }
    }

    public static class DerivedMetricsConfig {
        private Map<String, DerivedMetricConfig> derivedMetrics;
        public Map<String, DerivedMetricConfig> getDerivedMetrics() { return derivedMetrics; }
        public void setDerivedMetrics(Map<String, DerivedMetricConfig> derivedMetrics) { this.derivedMetrics = derivedMetrics; }
    }
}
