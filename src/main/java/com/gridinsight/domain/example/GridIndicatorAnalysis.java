package com.gridinsight.domain.example;

import com.gridinsight.domain.model.Indicator;
import com.gridinsight.domain.model.DerivedIndicator;
import com.gridinsight.domain.model.IndicatorType;
import java.util.Arrays;
import java.util.List;

/**
 * 电力行业数字化管控指标分析示例
 * 将复杂算术表达式拆分为基础指标和衍生指标
 */
public class GridIndicatorAnalysis {
    
    public static void main(String[] args) {
        System.out.println("=== 电力行业数字化管控指标分析 ===");
        
        // 分析指标1：配变挂接准确性
        analyzeTransformerConnectionAccuracy();
        
        // 分析指标2：变户关系准确性
        analyzeCustomerRelationshipAccuracy();
        
        // 分析指标3：台区单线图准确性
        analyzeSingleLineDiagramAccuracy();
        
        // 分析指标4：低压用户相别识别率
        analyzePhaseIdentificationRate();
        
        // 分析指标5：中压设备坐标准确性
        analyzeCoordinateAccuracy();
        
        // 分析指标6：配变小区匹配率
        analyzeCommunityMatchingRate();
        
        // 分析指标7：开关变位匹配率
        analyzeSwitchPositionMatchingRate();
        
        // 分析指标8：电缆通道上图率
        analyzeCableChannelMappingRate();
    }
    
    /**
     * 指标1：配变挂接准确性分析
     * 公式：(1-配变挂接馈线、具体分段位置与现场实际运行不一致数量/配变总数)*100%
     */
    private static void analyzeTransformerConnectionAccuracy() {
        System.out.println("\n=== 指标1：配变挂接准确性 ===");
        
        // 基础指标
        Indicator inconsistentTransformerCount = new Indicator(
            "配变挂接不一致数量",
            "配网拓扑",
            "配变挂接",
            IndicatorType.绝对值,
            "个",
            "mqtt://grid-monitor.com/topology/transformer/inconsistent"
        );
        
        Indicator totalTransformerCount = new Indicator(
            "配变总数",
            "配网拓扑",
            "配变统计",
            IndicatorType.绝对值,
            "个",
            "mqtt://grid-monitor.com/topology/transformer/total"
        );
        
        // 衍生指标
        DerivedIndicator transformerConnectionAccuracy = new DerivedIndicator(
            "配变挂接准确性",
            "数据质量",
            "拓扑准确性",
            "%",
            "(1 - 配变挂接不一致数量 / 配变总数) * 100",
            Arrays.asList(inconsistentTransformerCount, totalTransformerCount)
        );
        
        System.out.println("基础指标:");
        System.out.println("- " + inconsistentTransformerCount);
        System.out.println("- " + totalTransformerCount);
        System.out.println("衍生指标:");
        System.out.println("- " + transformerConnectionAccuracy);
        System.out.println("公式: " + transformerConnectionAccuracy.getFormula());
    }
    
    /**
     * 指标2：变户关系准确性分析
     * 公式：(1-变户关系不正确的低压用户数/全省低压用户总数)*100%
     */
    private static void analyzeCustomerRelationshipAccuracy() {
        System.out.println("\n=== 指标2：变户关系准确性 ===");
        
        // 基础指标
        Indicator incorrectCustomerCount = new Indicator(
            "变户关系不正确的低压用户数",
            "用户档案",
            "变户关系",
            IndicatorType.绝对值,
            "户",
            "mqtt://grid-monitor.com/customer/relationship/incorrect"
        );
        
        Indicator totalCustomerCount = new Indicator(
            "全省低压用户总数",
            "用户档案",
            "用户统计",
            IndicatorType.绝对值,
            "户",
            "mqtt://grid-monitor.com/customer/total"
        );
        
        // 衍生指标
        DerivedIndicator customerRelationshipAccuracy = new DerivedIndicator(
            "变户关系准确性",
            "数据质量",
            "用户档案准确性",
            "%",
            "(1 - 变户关系不正确的低压用户数 / 全省低压用户总数) * 100",
            Arrays.asList(incorrectCustomerCount, totalCustomerCount)
        );
        
        System.out.println("基础指标:");
        System.out.println("- " + incorrectCustomerCount);
        System.out.println("- " + totalCustomerCount);
        System.out.println("衍生指标:");
        System.out.println("- " + customerRelationshipAccuracy);
        System.out.println("公式: " + customerRelationshipAccuracy.getFormula());
    }
    
    /**
     * 指标3：台区单线图准确性分析
     * 公式：(1-台区单线图用户挂接分路档案正常的低压用户数/全省低压用户总数)*100%
     */
    private static void analyzeSingleLineDiagramAccuracy() {
        System.out.println("\n=== 指标3：台区单线图准确性 ===");
        
        // 基础指标
        Indicator normalCustomerCount = new Indicator(
            "台区单线图用户挂接分路档案正常的低压用户数",
            "图形数据",
            "单线图",
            IndicatorType.绝对值,
            "户",
            "mqtt://grid-monitor.com/diagram/single-line/normal"
        );
        
        Indicator totalCustomerCount = new Indicator(
            "全省低压用户总数",
            "用户档案",
            "用户统计",
            IndicatorType.绝对值,
            "户",
            "mqtt://grid-monitor.com/customer/total"
        );
        
        // 衍生指标
        DerivedIndicator singleLineDiagramAccuracy = new DerivedIndicator(
            "台区单线图准确性",
            "数据质量",
            "图形数据准确性",
            "%",
            "(1 - 台区单线图用户挂接分路档案正常的低压用户数 / 全省低压用户总数) * 100",
            Arrays.asList(normalCustomerCount, totalCustomerCount)
        );
        
        System.out.println("基础指标:");
        System.out.println("- " + normalCustomerCount);
        System.out.println("- " + totalCustomerCount);
        System.out.println("衍生指标:");
        System.out.println("- " + singleLineDiagramAccuracy);
        System.out.println("公式: " + singleLineDiagramAccuracy.getFormula());
    }
    
    /**
     * 指标4：低压用户相别识别率分析
     * 公式：能识别出具体相别的低压用户数/全省低压用户总数*100%
     */
    private static void analyzePhaseIdentificationRate() {
        System.out.println("\n=== 指标4：低压用户相别识别率 ===");
        
        // 基础指标
        Indicator identifiedCustomerCount = new Indicator(
            "能识别出具体相别的低压用户数",
            "用户档案",
            "相别识别",
            IndicatorType.绝对值,
            "户",
            "mqtt://grid-monitor.com/customer/phase/identified"
        );
        
        Indicator totalCustomerCount = new Indicator(
            "全省低压用户总数",
            "用户档案",
            "用户统计",
            IndicatorType.绝对值,
            "户",
            "mqtt://grid-monitor.com/customer/total"
        );
        
        // 衍生指标
        DerivedIndicator phaseIdentificationRate = new DerivedIndicator(
            "低压用户相别识别率",
            "数据完整性",
            "相别识别",
            "%",
            "能识别出具体相别的低压用户数 / 全省低压用户总数 * 100",
            Arrays.asList(identifiedCustomerCount, totalCustomerCount)
        );
        
        System.out.println("基础指标:");
        System.out.println("- " + identifiedCustomerCount);
        System.out.println("- " + totalCustomerCount);
        System.out.println("衍生指标:");
        System.out.println("- " + phaseIdentificationRate);
        System.out.println("公式: " + phaseIdentificationRate.getFormula());
    }
    
    /**
     * 指标5：中压设备坐标准确性分析
     * 公式：抽查中压配网核心设备的坐标正确数量/抽查中压配网核心设备坐标总数*100%
     */
    private static void analyzeCoordinateAccuracy() {
        System.out.println("\n=== 指标5：中压设备坐标准确性 ===");
        
        // 基础指标
        Indicator correctCoordinateCount = new Indicator(
            "抽查中压配网核心设备的坐标正确数量",
            "地理信息",
            "坐标准确性",
            IndicatorType.绝对值,
            "个",
            "mqtt://grid-monitor.com/geographic/coordinates/correct"
        );
        
        Indicator totalCoordinateCount = new Indicator(
            "抽查中压配网核心设备坐标总数",
            "地理信息",
            "坐标统计",
            IndicatorType.绝对值,
            "个",
            "mqtt://grid-monitor.com/geographic/coordinates/total"
        );
        
        // 衍生指标
        DerivedIndicator coordinateAccuracy = new DerivedIndicator(
            "中压设备坐标准确性",
            "数据质量",
            "地理信息准确性",
            "%",
            "抽查中压配网核心设备的坐标正确数量 / 抽查中压配网核心设备坐标总数 * 100",
            Arrays.asList(correctCoordinateCount, totalCoordinateCount)
        );
        
        System.out.println("基础指标:");
        System.out.println("- " + correctCoordinateCount);
        System.out.println("- " + totalCoordinateCount);
        System.out.println("衍生指标:");
        System.out.println("- " + coordinateAccuracy);
        System.out.println("公式: " + coordinateAccuracy.getFormula());
    }
    
    /**
     * 指标6：配变小区匹配率分析
     * 公式：匹配上小区的配变数/配变总数
     */
    private static void analyzeCommunityMatchingRate() {
        System.out.println("\n=== 指标6：配变小区匹配率 ===");
        
        // 基础指标
        Indicator matchedTransformerCount = new Indicator(
            "匹配上小区的配变数",
            "配网拓扑",
            "小区匹配",
            IndicatorType.绝对值,
            "个",
            "mqtt://grid-monitor.com/topology/community/matched"
        );
        
        Indicator totalTransformerCount = new Indicator(
            "配变总数",
            "配网拓扑",
            "配变统计",
            IndicatorType.绝对值,
            "个",
            "mqtt://grid-monitor.com/topology/transformer/total"
        );
        
        // 衍生指标
        DerivedIndicator communityMatchingRate = new DerivedIndicator(
            "配变小区匹配率",
            "数据完整性",
            "拓扑完整性",
            "%",
            "匹配上小区的配变数 / 配变总数 * 100",
            Arrays.asList(matchedTransformerCount, totalTransformerCount)
        );
        
        System.out.println("基础指标:");
        System.out.println("- " + matchedTransformerCount);
        System.out.println("- " + totalTransformerCount);
        System.out.println("衍生指标:");
        System.out.println("- " + communityMatchingRate);
        System.out.println("公式: " + communityMatchingRate.getFormula());
    }
    
    /**
     * 指标7：开关变位匹配率分析
     * 公式：实时量测中心开关变位匹配上源端开关变位的数量/源端开关变位数*100%
     */
    private static void analyzeSwitchPositionMatchingRate() {
        System.out.println("\n=== 指标7：开关变位匹配率 ===");
        
        // 基础指标
        Indicator matchedSwitchCount = new Indicator(
            "实时量测中心开关变位匹配上源端开关变位的数量",
            "实时数据",
            "开关变位",
            IndicatorType.绝对值,
            "个",
            "mqtt://grid-monitor.com/realtime/switch/matched"
        );
        
        Indicator totalSwitchCount = new Indicator(
            "源端开关变位数",
            "实时数据",
            "开关统计",
            IndicatorType.绝对值,
            "个",
            "mqtt://grid-monitor.com/realtime/switch/total"
        );
        
        // 衍生指标
        DerivedIndicator switchPositionMatchingRate = new DerivedIndicator(
            "开关变位匹配率",
            "数据同步",
            "实时数据同步",
            "%",
            "实时量测中心开关变位匹配上源端开关变位的数量 / 源端开关变位数 * 100",
            Arrays.asList(matchedSwitchCount, totalSwitchCount)
        );
        
        System.out.println("基础指标:");
        System.out.println("- " + matchedSwitchCount);
        System.out.println("- " + totalSwitchCount);
        System.out.println("衍生指标:");
        System.out.println("- " + switchPositionMatchingRate);
        System.out.println("公式: " + switchPositionMatchingRate.getFormula());
    }
    
    /**
     * 指标8：电缆通道上图率分析
     * 公式：电缆通道上图规模数/电缆通道规模总数*100%
     */
    private static void analyzeCableChannelMappingRate() {
        System.out.println("\n=== 指标8：电缆通道上图率 ===");
        
        // 基础指标
        Indicator mappedCableChannelCount = new Indicator(
            "电缆通道上图规模数",
            "基础设施",
            "电缆通道",
            IndicatorType.绝对值,
            "个",
            "mqtt://grid-monitor.com/infrastructure/cable/mapped"
        );
        
        Indicator totalCableChannelCount = new Indicator(
            "电缆通道规模总数",
            "基础设施",
            "电缆统计",
            IndicatorType.绝对值,
            "个",
            "mqtt://grid-monitor.com/infrastructure/cable/total"
        );
        
        // 衍生指标
        DerivedIndicator cableChannelMappingRate = new DerivedIndicator(
            "电缆通道上图率",
            "数据完整性",
            "基础设施数字化",
            "%",
            "电缆通道上图规模数 / 电缆通道规模总数 * 100",
            Arrays.asList(mappedCableChannelCount, totalCableChannelCount)
        );
        
        System.out.println("基础指标:");
        System.out.println("- " + mappedCableChannelCount);
        System.out.println("- " + totalCableChannelCount);
        System.out.println("衍生指标:");
        System.out.println("- " + cableChannelMappingRate);
        System.out.println("公式: " + cableChannelMappingRate.getFormula());
    }
}
