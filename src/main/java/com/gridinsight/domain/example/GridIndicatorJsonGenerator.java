package com.gridinsight.domain.example;

import com.gridinsight.domain.model.Indicator;
import com.gridinsight.domain.model.DerivedIndicator;
import com.gridinsight.domain.model.IndicatorType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 电力行业数字化管控指标JSON生成器
 * 创建基础指标和衍生指标对象，并序列化为JSON输出
 */
public class GridIndicatorJsonGenerator {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        // 配置JSON输出格式
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("=== 电力行业数字化管控指标JSON生成 ===");
            
            // 创建所有基础指标
            List<Indicator> basicIndicators = createBasicIndicators();
            
            // 创建所有衍生指标
            List<DerivedIndicator> derivedIndicators = createDerivedIndicators(basicIndicators);
            
            // 生成JSON输出
            generateJsonOutput(basicIndicators, derivedIndicators);
            
        } catch (Exception e) {
            System.err.println("生成JSON时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建所有基础指标
     */
    private static List<Indicator> createBasicIndicators() {
        List<Indicator> indicators = new ArrayList<>();
        
        // 指标1：中压拓扑关系准确率 - 基础指标
        indicators.add(new Indicator(
            "配变挂接馈线、具体分段位置与现场实际运行不一致数量",
            "中压拓扑",
            "拓扑不一致",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/mv-topology/inconsistent-count"
        ));
        
        indicators.add(new Indicator(
            "配变总数",
            "中压拓扑",
            "配变统计",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/mv-topology/transformer-total"
        ));
        
        // 指标2：低压-"配变-用户"关系准确率 - 基础指标
        indicators.add(new Indicator(
            "变户关系不正确的低压用户数",
            "低压用户关系",
            "变户关系错误",
            IndicatorType.绝对值,
            "户",
            "https://api.grid-monitor.com/v1/lv-customer/transformer-relationship/incorrect-count"
        ));
        
        indicators.add(new Indicator(
            "全省低压用户总数",
            "低压用户关系",
            "低压用户统计",
            IndicatorType.绝对值,
            "户",
            "https://api.grid-monitor.com/v1/lv-customer/total-count"
        ));
        
        // 指标3：低压"分路-用户"关系校核通过率 - 基础指标
        indicators.add(new Indicator(
            "台区单线图用户挂接分路档案正常的低压用户数",
            "低压分路关系",
            "分路档案正常",
            IndicatorType.绝对值,
            "户",
            "https://api.grid-monitor.com/v1/lv-branch/relationship/normal-count"
        ));
        
        // 指标4：低压"分相-用户"关系识别率 - 基础指标
        indicators.add(new Indicator(
            "能识别出具体相别的低压用户数",
            "低压分相关系",
            "相别识别",
            IndicatorType.绝对值,
            "户",
            "https://api.grid-monitor.com/v1/lv-phase/relationship/identified-count"
        ));
        
        // 指标5：中压配网杆塔、环网柜、站房坐标抽检通过率 - 基础指标
        indicators.add(new Indicator(
            "抽查中压配网核心设备的坐标正确数量",
            "中压坐标抽检",
            "坐标正确",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/mv-coordinate/inspection/correct-count"
        ));
        
        indicators.add(new Indicator(
            "抽查中压配网核心设备坐标总数",
            "中压坐标抽检",
            "坐标总数",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/mv-coordinate/inspection/total-count"
        ));
        
        // 指标6："配变-小区"关系匹配率 - 基础指标
        indicators.add(new Indicator(
            "匹配上小区的配变数",
            "配变小区关系",
            "小区匹配",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/transformer-community/matched-count"
        ));
        
        indicators.add(new Indicator(
            "配变总数",
            "配变小区关系",
            "配变统计",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/transformer-community/total-count"
        ));
        
        // 指标7：量测中心与配电自动化系统开关变位一致率 - 基础指标
        indicators.add(new Indicator(
            "实时量测中心开关变位匹配上源端开关变位的数量",
            "开关变位一致性",
            "变位匹配",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/switch-position/consistency/matched-count"
        ));
        
        indicators.add(new Indicator(
            "源端开关变位数",
            "开关变位一致性",
            "源端变位统计",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/switch-position/consistency/total-count"
        ));
        
        // 指标8：电缆通道上图率 - 基础指标
        indicators.add(new Indicator(
            "电缆通道上图规模数",
            "电缆通道上图",
            "通道上图",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/cable-channel/mapping/mapped-count"
        ));
        
        indicators.add(new Indicator(
            "电缆通道规模总数",
            "电缆通道上图",
            "通道总数",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/cable-channel/mapping/total-count"
        ));
        
        return indicators;
    }
    
    /**
     * 创建所有衍生指标
     */
    private static List<DerivedIndicator> createDerivedIndicators(List<Indicator> basicIndicators) {
        List<DerivedIndicator> derivedIndicators = new ArrayList<>();
        
        // 指标1：中压拓扑关系准确率
        derivedIndicators.add(new DerivedIndicator(
            "中压拓扑关系准确率",
            "拓扑质量",
            "中压拓扑准确性",
            "%",
            "(1 - 配变挂接馈线、具体分段位置与现场实际运行不一致数量 / 配变总数) * 100",
            Arrays.asList(basicIndicators.get(0), basicIndicators.get(1))
        ));
        
        // 指标2：低压-"配变-用户"关系准确率
        derivedIndicators.add(new DerivedIndicator(
            "低压-\"配变-用户\"关系准确率",
            "用户关系质量",
            "变户关系准确性",
            "%",
            "(1 - 变户关系不正确的低压用户数 / 全省低压用户总数) * 100",
            Arrays.asList(basicIndicators.get(2), basicIndicators.get(3))
        ));
        
        // 指标3：低压"分路-用户"关系校核通过率
        derivedIndicators.add(new DerivedIndicator(
            "低压\"分路-用户\"关系校核通过率",
            "分路关系质量",
            "分路关系校核",
            "%",
            "(1 - 台区单线图用户挂接分路档案正常的低压用户数 / 全省低压用户总数) * 100",
            Arrays.asList(basicIndicators.get(4), basicIndicators.get(3))
        ));
        
        // 指标4：低压"分相-用户"关系识别率
        derivedIndicators.add(new DerivedIndicator(
            "低压\"分相-用户\"关系识别率",
            "分相关系质量",
            "相别识别率",
            "%",
            "能识别出具体相别的低压用户数 / 全省低压用户总数 * 100",
            Arrays.asList(basicIndicators.get(5), basicIndicators.get(3))
        ));
        
        // 指标5：中压配网杆塔、环网柜、站房坐标抽检通过率
        derivedIndicators.add(new DerivedIndicator(
            "中压配网杆塔、环网柜、站房坐标抽检通过率",
            "坐标质量",
            "中压坐标抽检通过率",
            "%",
            "抽查中压配网核心设备的坐标正确数量 / 抽查中压配网核心设备坐标总数 * 100",
            Arrays.asList(basicIndicators.get(6), basicIndicators.get(7))
        ));
        
        // 指标6："配变-小区"关系匹配率
        derivedIndicators.add(new DerivedIndicator(
            "\"配变-小区\"关系匹配率",
            "配变小区关系质量",
            "配变小区匹配率",
            "%",
            "匹配上小区的配变数 / 配变总数 * 100",
            Arrays.asList(basicIndicators.get(8), basicIndicators.get(9))
        ));
        
        // 指标7：量测中心与配电自动化系统开关变位一致率
        derivedIndicators.add(new DerivedIndicator(
            "量测中心与配电自动化系统开关变位一致率",
            "开关变位一致性质量",
            "开关变位一致率",
            "%",
            "实时量测中心开关变位匹配上源端开关变位的数量 / 源端开关变位数 * 100",
            Arrays.asList(basicIndicators.get(10), basicIndicators.get(11))
        ));
        
        // 指标8：电缆通道上图率
        derivedIndicators.add(new DerivedIndicator(
            "电缆通道上图率",
            "电缆通道上图质量",
            "电缆通道上图率",
            "%",
            "电缆通道上图规模数 / 电缆通道规模总数 * 100",
            Arrays.asList(basicIndicators.get(12), basicIndicators.get(13))
        ));
        
        return derivedIndicators;
    }
    
    /**
     * 生成JSON输出
     */
    private static void generateJsonOutput(List<Indicator> basicIndicators, List<DerivedIndicator> derivedIndicators) {
        try {
            // 生成时间戳
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            
            System.out.println("\n=== 基础指标JSON输出 ===");
            String basicIndicatorsJson = objectMapper.writeValueAsString(basicIndicators);
            System.out.println(basicIndicatorsJson);
            
            // 保存基础指标JSON到文件
            saveJsonToFile(basicIndicatorsJson, "basic_indicators_" + timestamp + ".json");
            
            System.out.println("\n=== 衍生指标JSON输出 ===");
            String derivedIndicatorsJson = objectMapper.writeValueAsString(derivedIndicators);
            System.out.println(derivedIndicatorsJson);
            
            // 保存衍生指标JSON到文件
            saveJsonToFile(derivedIndicatorsJson, "derived_indicators_" + timestamp + ".json");
            
            System.out.println("\n=== 完整指标体系JSON输出 ===");
            IndicatorSystem indicatorSystem = new IndicatorSystem(basicIndicators, derivedIndicators);
            String fullSystemJson = objectMapper.writeValueAsString(indicatorSystem);
            System.out.println(fullSystemJson);
            
            // 保存完整指标体系JSON到文件
            saveJsonToFile(fullSystemJson, "complete_indicator_system_" + timestamp + ".json");
            
            System.out.println("\n=== JSON文件已保存 ===");
            System.out.println("基础指标文件: basic_indicators_" + timestamp + ".json");
            System.out.println("衍生指标文件: derived_indicators_" + timestamp + ".json");
            System.out.println("完整系统文件: complete_indicator_system_" + timestamp + ".json");
            
        } catch (Exception e) {
            System.err.println("序列化JSON时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 保存JSON到文件
     */
    private static void saveJsonToFile(String jsonContent, String fileName) {
        try {
            String filePath = "output/" + fileName;
            // 确保输出目录存在
            java.io.File outputDir = new java.io.File("output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(jsonContent);
                System.out.println("JSON文件已保存: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("保存JSON文件时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 指标体系包装类
     */
    public static class IndicatorSystem {
        private List<Indicator> basicIndicators;
        private List<DerivedIndicator> derivedIndicators;
        private String systemName;
        private String version;
        private String description;
        
        public IndicatorSystem(List<Indicator> basicIndicators, List<DerivedIndicator> derivedIndicators) {
            this.basicIndicators = basicIndicators;
            this.derivedIndicators = derivedIndicators;
            this.systemName = "电力行业数字化管控指标体系";
            this.version = "1.0.0";
            this.description = "基于Web服务接口的电力行业数字化管控指标模型";
        }
        
        // Getters
        public List<Indicator> getBasicIndicators() { return basicIndicators; }
        public List<DerivedIndicator> getDerivedIndicators() { return derivedIndicators; }
        public String getSystemName() { return systemName; }
        public String getVersion() { return version; }
        public String getDescription() { return description; }
        
        // Setters
        public void setBasicIndicators(List<Indicator> basicIndicators) { this.basicIndicators = basicIndicators; }
        public void setDerivedIndicators(List<DerivedIndicator> derivedIndicators) { this.derivedIndicators = derivedIndicators; }
        public void setSystemName(String systemName) { this.systemName = systemName; }
        public void setVersion(String version) { this.version = version; }
        public void setDescription(String description) { this.description = description; }
    }
}
