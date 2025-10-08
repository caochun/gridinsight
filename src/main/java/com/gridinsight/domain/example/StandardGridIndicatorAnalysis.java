package com.gridinsight.domain.example;

import com.gridinsight.domain.model.Indicator;
import com.gridinsight.domain.model.DerivedIndicator;
import com.gridinsight.domain.model.IndicatorType;
import java.util.Arrays;
import java.util.List;

/**
 * 电力行业数字化管控标准指标分析
 * 基于标准指标名称进行指标分解
 */
public class StandardGridIndicatorAnalysis {
    
    public static void main(String[] args) {
        System.out.println("=== 电力行业数字化管控标准指标分析 ===");
        
        // 分析标准指标1：中压拓扑关系准确率
        analyzeMediumVoltageTopologyAccuracy();
        
        // 分析标准指标2：低压-"配变-用户"关系准确率
        analyzeLowVoltageTransformerCustomerRelationship();
        
        // 分析标准指标3：低压"分路-用户"关系校核通过率
        analyzeLowVoltageBranchCustomerRelationship();
        
        // 分析标准指标4：低压"分相-用户"关系识别率
        analyzeLowVoltagePhaseCustomerRelationship();
        
        // 分析标准指标5：中压配网杆塔、环网柜、站房坐标抽检通过率
        analyzeMediumVoltageCoordinateInspection();
        
        // 分析标准指标6："配变-小区"关系匹配率
        analyzeTransformerCommunityMatching();
        
        // 分析标准指标7：量测中心与配电自动化系统开关变位一致率
        analyzeSwitchPositionConsistency();
        
        // 分析标准指标8：电缆通道上图率
        analyzeCableChannelMapping();
    }
    
    /**
     * 标准指标1：中压拓扑关系准确率
     * 公式：=（1-配变挂接馈线、具体分段位置与现场实际运行不一致数量/配变总数）*100%
     */
    private static void analyzeMediumVoltageTopologyAccuracy() {
        System.out.println("\n=== 标准指标1：中压拓扑关系准确率 ===");
        
        // 基础指标
        Indicator inconsistentTopologyCount = new Indicator(
            "配变挂接馈线、具体分段位置与现场实际运行不一致数量",
            "中压拓扑",
            "拓扑不一致",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/mv-topology/inconsistent-count"
        );
        
        Indicator totalTransformerCount = new Indicator(
            "配变总数",
            "中压拓扑",
            "配变统计",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/mv-topology/transformer-total"
        );
        
        // 衍生指标
        DerivedIndicator mediumVoltageTopologyAccuracy = new DerivedIndicator(
            "中压拓扑关系准确率",
            "拓扑质量",
            "中压拓扑准确性",
            "%",
            "(1 - 配变挂接馈线、具体分段位置与现场实际运行不一致数量 / 配变总数) * 100",
            Arrays.asList(inconsistentTopologyCount, totalTransformerCount)
        );
        
        System.out.println("基础指标:");
        System.out.println("- " + inconsistentTopologyCount);
        System.out.println("- " + totalTransformerCount);
        System.out.println("衍生指标:");
        System.out.println("- " + mediumVoltageTopologyAccuracy);
        System.out.println("公式: " + mediumVoltageTopologyAccuracy.getFormula());
    }
    
    /**
     * 标准指标2：低压-"配变-用户"关系准确率
     * 公式：=（1-变户关系不正确的低压用户数/全省低压用户总数）*100%
     */
    private static void analyzeLowVoltageTransformerCustomerRelationship() {
        System.out.println("\n=== 标准指标2：低压-\"配变-用户\"关系准确率 ===");
        
        // 基础指标
        Indicator incorrectCustomerRelationshipCount = new Indicator(
            "变户关系不正确的低压用户数",
            "低压用户关系",
            "变户关系错误",
            IndicatorType.绝对值,
            "户",
            "https://api.grid-monitor.com/v1/lv-customer/transformer-relationship/incorrect-count"
        );
        
        Indicator totalLowVoltageCustomerCount = new Indicator(
            "全省低压用户总数",
            "低压用户关系",
            "低压用户统计",
            IndicatorType.绝对值,
            "户",
            "https://api.grid-monitor.com/v1/lv-customer/total-count"
        );
        
        // 衍生指标
        DerivedIndicator lowVoltageTransformerCustomerAccuracy = new DerivedIndicator(
            "低压-\"配变-用户\"关系准确率",
            "用户关系质量",
            "变户关系准确性",
            "%",
            "(1 - 变户关系不正确的低压用户数 / 全省低压用户总数) * 100",
            Arrays.asList(incorrectCustomerRelationshipCount, totalLowVoltageCustomerCount)
        );
        
        System.out.println("基础指标:");
        System.out.println("- " + incorrectCustomerRelationshipCount);
        System.out.println("- " + totalLowVoltageCustomerCount);
        System.out.println("衍生指标:");
        System.out.println("- " + lowVoltageTransformerCustomerAccuracy);
        System.out.println("公式: " + lowVoltageTransformerCustomerAccuracy.getFormula());
    }
    
    /**
     * 标准指标3：低压"分路-用户"关系校核通过率
     * 公式：=（1-台区单线图用户挂接分路档案正常的低压用户数/全省低压用户总数）*100%
     */
    private static void analyzeLowVoltageBranchCustomerRelationship() {
        System.out.println("\n=== 标准指标3：低压\"分路-用户\"关系校核通过率 ===");
        
        // 基础指标
        Indicator normalBranchCustomerCount = new Indicator(
            "台区单线图用户挂接分路档案正常的低压用户数",
            "低压分路关系",
            "分路档案正常",
            IndicatorType.绝对值,
            "户",
            "https://api.grid-monitor.com/v1/lv-branch/relationship/normal-count"
        );
        
        Indicator totalLowVoltageCustomerCount = new Indicator(
            "全省低压用户总数",
            "低压分路关系",
            "低压用户统计",
            IndicatorType.绝对值,
            "户",
            "https://api.grid-monitor.com/v1/lv-customer/total-count"
        );
        
        // 衍生指标
        DerivedIndicator lowVoltageBranchCustomerVerificationRate = new DerivedIndicator(
            "低压\"分路-用户\"关系校核通过率",
            "分路关系质量",
            "分路关系校核",
            "%",
            "(1 - 台区单线图用户挂接分路档案正常的低压用户数 / 全省低压用户总数) * 100",
            Arrays.asList(normalBranchCustomerCount, totalLowVoltageCustomerCount)
        );
        
        System.out.println("基础指标:");
        System.out.println("- " + normalBranchCustomerCount);
        System.out.println("- " + totalLowVoltageCustomerCount);
        System.out.println("衍生指标:");
        System.out.println("- " + lowVoltageBranchCustomerVerificationRate);
        System.out.println("公式: " + lowVoltageBranchCustomerVerificationRate.getFormula());
    }
    
    /**
     * 标准指标4：低压"分相-用户"关系识别率
     * 公式：=能识别出具体相别的低压用户数/全省低压用户总数*100%
     */
    private static void analyzeLowVoltagePhaseCustomerRelationship() {
        System.out.println("\n=== 标准指标4：低压\"分相-用户\"关系识别率 ===");
        
        // 基础指标
        Indicator identifiedPhaseCustomerCount = new Indicator(
            "能识别出具体相别的低压用户数",
            "低压分相关系",
            "相别识别",
            IndicatorType.绝对值,
            "户",
            "https://api.grid-monitor.com/v1/lv-phase/relationship/identified-count"
        );
        
        Indicator totalLowVoltageCustomerCount = new Indicator(
            "全省低压用户总数",
            "低压分相关系",
            "低压用户统计",
            IndicatorType.绝对值,
            "户",
            "https://api.grid-monitor.com/v1/lv-customer/total-count"
        );
        
        // 衍生指标
        DerivedIndicator lowVoltagePhaseCustomerIdentificationRate = new DerivedIndicator(
            "低压\"分相-用户\"关系识别率",
            "分相关系质量",
            "相别识别率",
            "%",
            "能识别出具体相别的低压用户数 / 全省低压用户总数 * 100",
            Arrays.asList(identifiedPhaseCustomerCount, totalLowVoltageCustomerCount)
        );
        
        System.out.println("基础指标:");
        System.out.println("- " + identifiedPhaseCustomerCount);
        System.out.println("- " + totalLowVoltageCustomerCount);
        System.out.println("衍生指标:");
        System.out.println("- " + lowVoltagePhaseCustomerIdentificationRate);
        System.out.println("公式: " + lowVoltagePhaseCustomerIdentificationRate.getFormula());
    }
    
    /**
     * 标准指标5：中压配网杆塔、环网柜、站房坐标抽检通过率
     * 公式：=抽查中压配网核心设备的坐标正确数量/抽查中压配网核心设备坐标总数*100%
     */
    private static void analyzeMediumVoltageCoordinateInspection() {
        System.out.println("\n=== 标准指标5：中压配网杆塔、环网柜、站房坐标抽检通过率 ===");
        
        // 基础指标
        Indicator correctCoordinateCount = new Indicator(
            "抽查中压配网核心设备的坐标正确数量",
            "中压坐标抽检",
            "坐标正确",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/mv-coordinate/inspection/correct-count"
        );
        
        Indicator totalCoordinateCount = new Indicator(
            "抽查中压配网核心设备坐标总数",
            "中压坐标抽检",
            "坐标总数",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/mv-coordinate/inspection/total-count"
        );
        
        // 衍生指标
        DerivedIndicator mediumVoltageCoordinateInspectionPassRate = new DerivedIndicator(
            "中压配网杆塔、环网柜、站房坐标抽检通过率",
            "坐标质量",
            "中压坐标抽检通过率",
            "%",
            "抽查中压配网核心设备的坐标正确数量 / 抽查中压配网核心设备坐标总数 * 100",
            Arrays.asList(correctCoordinateCount, totalCoordinateCount)
        );
        
        System.out.println("基础指标:");
        System.out.println("- " + correctCoordinateCount);
        System.out.println("- " + totalCoordinateCount);
        System.out.println("衍生指标:");
        System.out.println("- " + mediumVoltageCoordinateInspectionPassRate);
        System.out.println("公式: " + mediumVoltageCoordinateInspectionPassRate.getFormula());
    }
    
    /**
     * 标准指标6："配变-小区"关系匹配率
     * 公式：=匹配上小区的配变数/配变总数
     */
    private static void analyzeTransformerCommunityMatching() {
        System.out.println("\n=== 标准指标6：\"配变-小区\"关系匹配率 ===");
        
        // 基础指标
        Indicator matchedTransformerCount = new Indicator(
            "匹配上小区的配变数",
            "配变小区关系",
            "小区匹配",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/transformer-community/matched-count"
        );
        
        Indicator totalTransformerCount = new Indicator(
            "配变总数",
            "配变小区关系",
            "配变统计",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/transformer-community/total-count"
        );
        
        // 衍生指标
        DerivedIndicator transformerCommunityMatchingRate = new DerivedIndicator(
            "\"配变-小区\"关系匹配率",
            "配变小区关系质量",
            "配变小区匹配率",
            "%",
            "匹配上小区的配变数 / 配变总数 * 100",
            Arrays.asList(matchedTransformerCount, totalTransformerCount)
        );
        
        System.out.println("基础指标:");
        System.out.println("- " + matchedTransformerCount);
        System.out.println("- " + totalTransformerCount);
        System.out.println("衍生指标:");
        System.out.println("- " + transformerCommunityMatchingRate);
        System.out.println("公式: " + transformerCommunityMatchingRate.getFormula());
    }
    
    /**
     * 标准指标7：量测中心与配电自动化系统开关变位一致率
     * 公式：=实时量测中心开关变位匹配上源端开关变位的数量/源端开关变位数*100%
     */
    private static void analyzeSwitchPositionConsistency() {
        System.out.println("\n=== 标准指标7：量测中心与配电自动化系统开关变位一致率 ===");
        
        // 基础指标
        Indicator matchedSwitchPositionCount = new Indicator(
            "实时量测中心开关变位匹配上源端开关变位的数量",
            "开关变位一致性",
            "变位匹配",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/switch-position/consistency/matched-count"
        );
        
        Indicator totalSwitchPositionCount = new Indicator(
            "源端开关变位数",
            "开关变位一致性",
            "源端变位统计",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/switch-position/consistency/total-count"
        );
        
        // 衍生指标
        DerivedIndicator switchPositionConsistencyRate = new DerivedIndicator(
            "量测中心与配电自动化系统开关变位一致率",
            "开关变位一致性质量",
            "开关变位一致率",
            "%",
            "实时量测中心开关变位匹配上源端开关变位的数量 / 源端开关变位数 * 100",
            Arrays.asList(matchedSwitchPositionCount, totalSwitchPositionCount)
        );
        
        System.out.println("基础指标:");
        System.out.println("- " + matchedSwitchPositionCount);
        System.out.println("- " + totalSwitchPositionCount);
        System.out.println("衍生指标:");
        System.out.println("- " + switchPositionConsistencyRate);
        System.out.println("公式: " + switchPositionConsistencyRate.getFormula());
    }
    
    /**
     * 标准指标8：电缆通道上图率
     * 公式：=电缆通道上图规模数/电缆通道规模总数*100%
     */
    private static void analyzeCableChannelMapping() {
        System.out.println("\n=== 标准指标8：电缆通道上图率 ===");
        
        // 基础指标
        Indicator mappedCableChannelCount = new Indicator(
            "电缆通道上图规模数",
            "电缆通道上图",
            "通道上图",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/cable-channel/mapping/mapped-count"
        );
        
        Indicator totalCableChannelCount = new Indicator(
            "电缆通道规模总数",
            "电缆通道上图",
            "通道总数",
            IndicatorType.绝对值,
            "个",
            "https://api.grid-monitor.com/v1/cable-channel/mapping/total-count"
        );
        
        // 衍生指标
        DerivedIndicator cableChannelMappingRate = new DerivedIndicator(
            "电缆通道上图率",
            "电缆通道上图质量",
            "电缆通道上图率",
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
