package com.gridinsight.service;

import com.gridinsight.domain.model.*;
import com.gridinsight.domain.service.MetricCalculationService;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 指标配置管理服务
 * 负责从配置文件加载指标定义，并提供运行时管理功能
 */
@Service
public class MetricConfigService {

    private final Map<String, BasicMetric> basicMetrics = new ConcurrentHashMap<>();
    private final Map<String, DerivedMetric> derivedMetrics = new ConcurrentHashMap<>();
    private final Map<String, DataSource> dataSources = new ConcurrentHashMap<>();

    @org.springframework.beans.factory.annotation.Autowired
    private MetricCalculationService metricCalculationService;

    /**
     * 启动时加载配置
     */
    @PostConstruct
    public void loadConfigurations() {
        loadBasicMetrics();
        loadDerivedMetrics();
        syncToCalculationService();
        // 指标配置加载完成
    }

    /**
     * 将已加载的指标同步到计算服务
     */
    private void syncToCalculationService() {
        // 清理并重新注册：此处简单覆盖式注册
        basicMetrics.forEach((id, m) -> metricCalculationService.addMetric(m));
        derivedMetrics.forEach((id, m) -> metricCalculationService.addMetric(m));
    }

    /**
     * 加载基础指标配置
     */
    private void loadBasicMetrics() {
        try {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("metrics/basic-metrics.yaml");
            
            if (inputStream == null) {
                // 未找到基础指标配置文件
                return;
            }

            LoaderOptions loaderOptions = new LoaderOptions();
            Constructor constructor = new Constructor(BasicMetricsConfig.class, loaderOptions);
            Yaml yaml = new Yaml(constructor);
            BasicMetricsConfig config = yaml.load(inputStream);

            if (config.getBasicMetrics() != null) {
                for (Map.Entry<String, BasicMetricConfig> entry : config.getBasicMetrics().entrySet()) {
                    String identifier = entry.getKey();
                    BasicMetricConfig metricConfig = entry.getValue();
                    
                    BasicMetric metric = createBasicMetric(identifier, metricConfig);
                    basicMetrics.put(identifier, metric);
                    dataSources.put(identifier, metric.getDataSource());
                }
            }

            inputStream.close();
        } catch (Exception e) {
            // 加载基础指标配置失败
        }
    }

    /**
     * 加载派生指标配置
     */
    private void loadDerivedMetrics() {
        try {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("metrics/derived-metrics.yaml");
            
            if (inputStream == null) {
                // 未找到派生指标配置文件
                return;
            }

            LoaderOptions loaderOptions = new LoaderOptions();
            Constructor constructor = new Constructor(DerivedMetricsConfig.class, loaderOptions);
            Yaml yaml = new Yaml(constructor);
            DerivedMetricsConfig config = yaml.load(inputStream);

            if (config.getDerivedMetrics() != null) {
                for (Map.Entry<String, DerivedMetricConfig> entry : config.getDerivedMetrics().entrySet()) {
                    String identifier = entry.getKey();
                    DerivedMetricConfig metricConfig = entry.getValue();
                    
                    DerivedMetric metric = createDerivedMetric(identifier, metricConfig);
                    derivedMetrics.put(identifier, metric);
                }
            }

            inputStream.close();
        } catch (Exception e) {
            // 加载派生指标配置失败
        }
    }

    /**
     * 创建基础指标对象
     */
    private BasicMetric createBasicMetric(String identifier, BasicMetricConfig config) {
        DataSourceConfig dataSourceConfig = config.getDataSource();
        DataSource.SourceType sourceType = DataSource.SourceType.valueOf(dataSourceConfig.getSourceType());
        
        DataSource dataSource;
        if (dataSourceConfig.getConfig() != null && !dataSourceConfig.getConfig().isEmpty()) {
            // 使用新的结构化配置
            dataSource = new DataSource(sourceType, dataSourceConfig.getSourceName(), 
                "从YAML配置加载", dataSourceConfig.getRefreshInterval(), dataSourceConfig.isEnabled());
            dataSource.setConfig(dataSourceConfig.getConfig());
        } else {
            // 兼容旧的sourceAddress方式
            dataSource = new DataSource(sourceType, dataSourceConfig.getSourceName(), 
                "从YAML配置加载", dataSourceConfig.getRefreshInterval(), dataSourceConfig.isEnabled());
            if (dataSourceConfig.getSourceAddress() != null) {
                dataSource.setSourceAddress(dataSourceConfig.getSourceAddress());
            }
        }

        return new BasicMetric(
            config.getName(),
            config.getCategory(),
            config.getSubCategory(),
            config.getUnit(),
            config.getDescription(),
            dataSource
        );
    }

    /**
     * 创建派生指标对象
     */
    private DerivedMetric createDerivedMetric(String identifier, DerivedMetricConfig config) {
        List<Metric> dependencies = new ArrayList<>();
        
        // 根据依赖标识符查找对应的指标对象
        for (String depIdentifier : config.getDependencies()) {
            Metric dependency = getMetric(depIdentifier);
            if (dependency != null) {
                dependencies.add(dependency);
            } else {
                // 未找到依赖指标
            }
        }

        // 解析更新策略
        DerivedMetricUpdateStrategy updateStrategy;
        try {
            updateStrategy = DerivedMetricUpdateStrategy.valueOf(config.getUpdateStrategy());
        } catch (Exception e) {
            // 无效的更新策略，使用默认策略
            updateStrategy = DerivedMetricUpdateStrategy.REALTIME;
        }

        return new DerivedMetric(
            config.getName(),
            config.getCategory(),
            config.getSubCategory(),
            config.getUnit(),
            config.getDescription(),
            config.getFormula(),
            dependencies,
            updateStrategy,
            config.getCalculationInterval()
        );
    }

    // ========== 公共接口方法 ==========

    /**
     * 获取所有基础指标
     */
    public Map<String, BasicMetric> getAllBasicMetrics() {
        return new HashMap<>(basicMetrics);
    }

    /**
     * 获取所有派生指标
     */
    public Map<String, DerivedMetric> getAllDerivedMetrics() {
        return new HashMap<>(derivedMetrics);
    }

    /**
     * 获取所有指标
     */
    public Map<String, Metric> getAllMetrics() {
        Map<String, Metric> allMetrics = new HashMap<>();
        allMetrics.putAll(basicMetrics);
        allMetrics.putAll(derivedMetrics);
        return allMetrics;
    }

    /**
     * 根据标识符获取指标
     */
    public Metric getMetric(String identifier) {
        Metric metric = basicMetrics.get(identifier);
        if (metric == null) {
            metric = derivedMetrics.get(identifier);
        }
        return metric;
    }

    /**
     * 获取基础指标
     */
    public BasicMetric getBasicMetric(String identifier) {
        return basicMetrics.get(identifier);
    }

    /**
     * 获取派生指标
     */
    public DerivedMetric getDerivedMetric(String identifier) {
        return derivedMetrics.get(identifier);
    }

    /**
     * 添加基础指标
     */
    public void addBasicMetric(String identifier, BasicMetric metric) {
        basicMetrics.put(identifier, metric);
        if (metric.getDataSource() != null) {
            dataSources.put(identifier, metric.getDataSource());
        }
    }

    /**
     * 添加派生指标
     */
    public void addDerivedMetric(String identifier, DerivedMetric metric) {
        derivedMetrics.put(identifier, metric);
    }

    /**
     * 更新基础指标
     */
    public void updateBasicMetric(String identifier, BasicMetric metric) {
        basicMetrics.put(identifier, metric);
        if (metric.getDataSource() != null) {
            dataSources.put(identifier, metric.getDataSource());
        }
        // 同步到计算服务
        metricCalculationService.addMetric(metric);
    }

    /**
     * 更新派生指标
     */
    public void updateDerivedMetric(String identifier, DerivedMetric metric) {
        derivedMetrics.put(identifier, metric);
        // 同步到计算服务
        metricCalculationService.addMetric(metric);
    }

    /**
     * 删除指标
     */
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

    /**
     * 获取指标统计信息
     */
    public Map<String, Object> getMetricStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBasicMetrics", basicMetrics.size());
        stats.put("totalDerivedMetrics", derivedMetrics.size());
        stats.put("totalMetrics", basicMetrics.size() + derivedMetrics.size());
        stats.put("totalDataSources", dataSources.size());
        return stats;
    }

    // ========== 配置类定义 ==========

    /**
     * 基础指标配置类
     */
    public static class BasicMetricConfig {
        private String name;
        private String category;
        private String subCategory;
        private String unit;
        private String description;
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
        public DataSourceConfig getDataSource() { return dataSource; }
        public void setDataSource(DataSourceConfig dataSource) { this.dataSource = dataSource; }
    }

    /**
     * 派生指标配置类
     */
    public static class DerivedMetricConfig {
        private String name;
        private String category;
        private String subCategory;
        private String unit;
        private String description;
        private String formula;
        private String updateStrategy = "REALTIME";
        private Integer calculationInterval = 300;
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
        public String getFormula() { return formula; }
        public void setFormula(String formula) { this.formula = formula; }
        public String getUpdateStrategy() { return updateStrategy; }
        public void setUpdateStrategy(String updateStrategy) { this.updateStrategy = updateStrategy; }
        public Integer getCalculationInterval() { return calculationInterval; }
        public void setCalculationInterval(Integer calculationInterval) { this.calculationInterval = calculationInterval; }
        public List<String> getDependencies() { return dependencies; }
        public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
    }

    /**
     * 数据源配置类
     */
    public static class DataSourceConfig {
        private String sourceType;
        private String sourceAddress;
        private String sourceName;
        private int refreshInterval;
        private boolean enabled;
        private Map<String, Object> config;

        // Getters and Setters
        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }
        public String getSourceAddress() { return sourceAddress; }
        public void setSourceAddress(String sourceAddress) { this.sourceAddress = sourceAddress; }
        public String getSourceName() { return sourceName; }
        public void setSourceName(String sourceName) { this.sourceName = sourceName; }
        public int getRefreshInterval() { return refreshInterval; }
        public void setRefreshInterval(int refreshInterval) { this.refreshInterval = refreshInterval; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public Map<String, Object> getConfig() { return config; }
        public void setConfig(Map<String, Object> config) { this.config = config; }
    }

    /**
     * 基础指标配置容器类
     */
    public static class BasicMetricsConfig {
        private Map<String, BasicMetricConfig> basicMetrics;

        public Map<String, BasicMetricConfig> getBasicMetrics() { return basicMetrics; }
        public void setBasicMetrics(Map<String, BasicMetricConfig> basicMetrics) { this.basicMetrics = basicMetrics; }
    }

    /**
     * 派生指标配置容器类
     */
    public static class DerivedMetricsConfig {
        private Map<String, DerivedMetricConfig> derivedMetrics;

        public Map<String, DerivedMetricConfig> getDerivedMetrics() { return derivedMetrics; }
        public void setDerivedMetrics(Map<String, DerivedMetricConfig> derivedMetrics) { this.derivedMetrics = derivedMetrics; }
    }
}
