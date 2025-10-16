package com.gridinsight;

import com.gridinsight.domain.model.*;
import java.util.Arrays;
import java.util.List;

/**
 * 测试数据生成器
 * 用于生成测试用的指标数据
 */
public class TestDataGenerator {
    
    /**
     * 创建基础指标测试数据
     */
    public static List<BasicMetric> createBasicMetrics() {
        return Arrays.asList(
            new BasicMetric(
                "配变总数",
                "中压拓扑",
                "配变统计",
                "个",
                "中压配电网中配变的总数量",
                DataSource.createHttpApi(
                    "https://api.grid-monitor.com/v1/mv-topology/transformer-total",
                    "配变统计API",
                    "获取配变总数数据"
                )
            ),
            new BasicMetric(
                "配变挂接馈线不一致数量",
                "中压拓扑",
                "拓扑不一致",
                "个",
                "配变挂接馈线位置与现场实际运行不一致的数量",
                DataSource.createHttpApi(
                    "https://api.grid-monitor.com/v1/mv-topology/inconsistent-count",
                    "拓扑一致性API",
                    "获取拓扑不一致数据"
                )
            ),
            new BasicMetric(
                "全省低压用户总数",
                "低压用户关系",
                "低压用户统计",
                "户",
                "全省低压用户的总数量",
                DataSource.createHttpApi(
                    "https://api.grid-monitor.com/v1/lv-customer/total-count",
                    "用户统计API",
                    "获取低压用户总数数据"
                )
            ),
            new BasicMetric(
                "变户关系不正确的低压用户数",
                "低压用户关系",
                "变户关系错误",
                "户",
                "变户关系不正确的低压用户数量",
                DataSource.createHttpApi(
                    "https://api.grid-monitor.com/v1/lv-customer/transformer-relationship/incorrect-count",
                    "变户关系API",
                    "获取变户关系错误数据"
                )
            )
        );
    }
    
    /**
     * 创建派生指标测试数据
     */
    public static List<DerivedMetric> createDerivedMetrics(List<BasicMetric> basicMetrics) {
        return Arrays.asList(
            new DerivedMetric(
                "中压拓扑关系准确率",
                "拓扑质量",
                "中压拓扑准确性",
                "%",
                "中压拓扑关系的准确率",
                "(1 - 中压拓扑.拓扑不一致.配变挂接馈线不一致数量 / 中压拓扑.配变统计.配变总数) * 100",
                Arrays.asList(basicMetrics.get(0), basicMetrics.get(1))
            ),
            new DerivedMetric(
                "低压变户关系准确率",
                "用户关系质量",
                "变户关系准确性",
                "%",
                "低压变户关系的准确率",
                "(1 - 低压用户关系.变户关系错误.变户关系不正确的低压用户数 / 低压用户关系.低压用户统计.全省低压用户总数) * 100",
                Arrays.asList(basicMetrics.get(2), basicMetrics.get(3))
            ),
            new DerivedMetric(
                "综合质量指标",
                "质量评估",
                "综合评估",
                "%",
                "综合质量评估指标",
                "sqrt(拓扑质量.中压拓扑准确性.中压拓扑关系准确率 * 用户关系质量.变户关系准确性.低压变户关系准确率)",
                Arrays.asList() // 这个会在运行时添加依赖
            )
        );
    }
    
    /**
     * 创建MQTT数据源测试数据
     */
    public static List<BasicMetric> createMqttMetrics() {
        return Arrays.asList(
            new BasicMetric(
                "实时功率",
                "发电指标",
                "实时功率",
                "MW",
                "实时发电功率",
                DataSource.createMqtt(
                    "grid/power/realtime",
                    "实时功率MQTT",
                    "实时功率数据订阅"
                )
            ),
            new BasicMetric(
                "电压等级",
                "电压指标",
                "电压监测",
                "kV",
                "电网电压等级",
                DataSource.createMqtt(
                    "grid/voltage/level",
                    "电压等级MQTT",
                    "电压等级数据订阅"
                )
            )
        );
    }
    
    /**
     * 创建数据库数据源测试数据
     */
    public static List<BasicMetric> createDatabaseMetrics() {
        return Arrays.asList(
            new BasicMetric(
                "历史发电量",
                "发电指标",
                "历史统计",
                "MWh",
                "历史发电量统计",
                new DataSource(
                    DataSource.SourceType.DATABASE,
                    "jdbc:mysql://localhost:3306/grid_data",
                    "历史数据数据库",
                    "历史发电量数据查询",
                    3600 // 1小时刷新一次
                )
            )
        );
    }
    
    /**
     * 创建文件数据源测试数据
     */
    public static List<BasicMetric> createFileMetrics() {
        return Arrays.asList(
            new BasicMetric(
                "配置文件参数",
                "系统配置",
                "配置参数",
                "个",
                "系统配置参数",
                new DataSource(
                    DataSource.SourceType.FILE,
                    "/data/config/parameters.json",
                    "配置文件",
                    "系统配置参数文件",
                    86400 // 24小时刷新一次
                )
            )
        );
    }
    
    /**
     * 创建复杂的派生指标测试数据
     */
    public static List<DerivedMetric> createComplexDerivedMetrics() {
        return Arrays.asList(
            new DerivedMetric(
                "加权平均准确率",
                "质量评估",
                "加权评估",
                "%",
                "基于权重的平均准确率",
                "(拓扑质量.中压拓扑准确性.中压拓扑关系准确率 * 0.6 + 用户关系质量.变户关系准确性.低压变户关系准确率 * 0.4)",
                Arrays.asList()
            ),
            new DerivedMetric(
                "质量趋势指标",
                "趋势分析",
                "质量趋势",
                "%",
                "质量变化趋势指标",
                "max(拓扑质量.中压拓扑准确性.中压拓扑关系准确率, 用户关系质量.变户关系准确性.低压变户关系准确率) - min(拓扑质量.中压拓扑准确性.中压拓扑关系准确率, 用户关系质量.变户关系准确性.低压变户关系准确率)",
                Arrays.asList()
            )
        );
    }
    
    /**
     * 创建所有类型的测试数据
     */
    public static TestDataSet createCompleteTestDataSet() {
        List<BasicMetric> basicMetrics = createBasicMetrics();
        List<BasicMetric> mqttMetrics = createMqttMetrics();
        List<BasicMetric> dbMetrics = createDatabaseMetrics();
        List<BasicMetric> fileMetrics = createFileMetrics();
        
        List<DerivedMetric> derivedMetrics = createDerivedMetrics(basicMetrics);
        List<DerivedMetric> complexDerivedMetrics = createComplexDerivedMetrics();
        
        return new TestDataSet(
            basicMetrics,
            mqttMetrics,
            dbMetrics,
            fileMetrics,
            derivedMetrics,
            complexDerivedMetrics
        );
    }
    
    /**
     * 测试数据集
     */
    public static class TestDataSet {
        private final List<BasicMetric> basicMetrics;
        private final List<BasicMetric> mqttMetrics;
        private final List<BasicMetric> dbMetrics;
        private final List<BasicMetric> fileMetrics;
        private final List<DerivedMetric> derivedMetrics;
        private final List<DerivedMetric> complexDerivedMetrics;
        
        public TestDataSet(List<BasicMetric> basicMetrics,
                          List<BasicMetric> mqttMetrics,
                          List<BasicMetric> dbMetrics,
                          List<BasicMetric> fileMetrics,
                          List<DerivedMetric> derivedMetrics,
                          List<DerivedMetric> complexDerivedMetrics) {
            this.basicMetrics = basicMetrics;
            this.mqttMetrics = mqttMetrics;
            this.dbMetrics = dbMetrics;
            this.fileMetrics = fileMetrics;
            this.derivedMetrics = derivedMetrics;
            this.complexDerivedMetrics = complexDerivedMetrics;
        }
        
        // Getters
        public List<BasicMetric> getBasicMetrics() { return basicMetrics; }
        public List<BasicMetric> getMqttMetrics() { return mqttMetrics; }
        public List<BasicMetric> getDbMetrics() { return dbMetrics; }
        public List<BasicMetric> getFileMetrics() { return fileMetrics; }
        public List<DerivedMetric> getDerivedMetrics() { return derivedMetrics; }
        public List<DerivedMetric> getComplexDerivedMetrics() { return complexDerivedMetrics; }
        
        public List<BasicMetric> getAllBasicMetrics() {
            return Arrays.asList(
                basicMetrics, mqttMetrics, dbMetrics, fileMetrics
            ).stream().flatMap(List::stream).collect(java.util.stream.Collectors.toList());
        }
        
        public List<DerivedMetric> getAllDerivedMetrics() {
            return Arrays.asList(derivedMetrics, complexDerivedMetrics)
                .stream().flatMap(List::stream).collect(java.util.stream.Collectors.toList());
        }
    }
}
