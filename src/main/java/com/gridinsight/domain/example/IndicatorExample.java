package com.gridinsight.domain.example;

import com.gridinsight.domain.model.Indicator;
import com.gridinsight.domain.model.DerivedIndicator;
import com.gridinsight.domain.model.IndicatorType;
import java.util.Arrays;
import java.util.List;

/**
 * 电力行业数字化管控指标使用示例
 * 演示如何使用基础指标和派生指标
 */
public class IndicatorExample {
    
    public static void main(String[] args) {
        // 创建基础指标示例
        createBasicIndicators();
        
        // 创建派生指标示例
        createDerivedIndicators();
    }
    
    /**
     * 创建基础指标示例
     */
    private static void createBasicIndicators() {
        System.out.println("=== 基础指标示例 ===");
        
        // 发电量指标
        Indicator powerGeneration = new Indicator(
            "发电量",
            "发电指标",
            "发电量统计",
            IndicatorType.绝对值,
            "万千瓦时",
            "mqtt://grid-monitor.com/power/generation"
        );
        
        // 用电量指标
        Indicator powerConsumption = new Indicator(
            "用电量",
            "用电指标",
            "用电量统计",
            IndicatorType.绝对值,
            "万千瓦时",
            "mqtt://grid-monitor.com/power/consumption"
        );
        
        // 发电效率指标
        Indicator generationEfficiency = new Indicator(
            "发电效率",
            "效率指标",
            "发电效率",
            IndicatorType.比率,
            "%",
            "mqtt://grid-monitor.com/efficiency/generation"
        );
        
        System.out.println("发电量指标: " + powerGeneration);
        System.out.println("用电量指标: " + powerConsumption);
        System.out.println("发电效率指标: " + generationEfficiency);
        System.out.println("发电量完整标识: " + powerGeneration.getFullIdentifier());
        System.out.println("是否为派生指标: " + powerGeneration.isDerived());
    }
    
    /**
     * 创建派生指标示例
     */
    private static void createDerivedIndicators() {
        System.out.println("\n=== 派生指标示例 ===");
        
        // 创建依赖的基础指标
        Indicator powerGeneration = new Indicator(
            "发电量",
            "发电指标",
            "发电量统计",
            IndicatorType.绝对值,
            "万千瓦时",
            "mqtt://grid-monitor.com/power/generation"
        );
        
        Indicator powerConsumption = new Indicator(
            "用电量",
            "用电指标",
            "用电量统计",
            IndicatorType.绝对值,
            "万千瓦时",
            "mqtt://grid-monitor.com/power/consumption"
        );
        
        // 创建派生指标：电力供需平衡率
        DerivedIndicator powerBalanceRate = new DerivedIndicator(
            "电力供需平衡率",
            "平衡指标",
            "供需平衡",
            "%",
            "(发电量 - 用电量) / 发电量 * 100",
            Arrays.asList(powerGeneration, powerConsumption)
        );
        
        System.out.println("派生指标: " + powerBalanceRate);
        System.out.println("公式: " + powerBalanceRate.getFormula());
        System.out.println("依赖指标数量: " + powerBalanceRate.getDependencyCount());
        System.out.println("是否依赖发电量: " + powerBalanceRate.dependsOn(powerGeneration));
        System.out.println("公式是否有效: " + powerBalanceRate.isValidFormula());
        
        // 创建更复杂的派生指标示例
        createComplexDerivedIndicator();
    }
    
    /**
     * 创建复杂的派生指标示例
     */
    private static void createComplexDerivedIndicator() {
        System.out.println("\n=== 复杂派生指标示例 ===");
        
        // 创建多个基础指标
        Indicator totalGeneration = new Indicator("总发电量", "发电指标", "总量统计", IndicatorType.绝对值, "万千瓦时", "mqtt://grid-monitor.com/power/total");
        Indicator renewableGeneration = new Indicator("可再生能源发电量", "发电指标", "清洁能源", IndicatorType.绝对值, "万千瓦时", "mqtt://grid-monitor.com/power/renewable");
        Indicator coalGeneration = new Indicator("煤电发电量", "发电指标", "传统能源", IndicatorType.绝对值, "万千瓦时", "mqtt://grid-monitor.com/power/coal");
        
        // 创建清洁能源占比派生指标
        DerivedIndicator cleanEnergyRatio = new DerivedIndicator();
        cleanEnergyRatio.setName("清洁能源占比");
        cleanEnergyRatio.setCategory("环保指标");
        cleanEnergyRatio.setSubCategory("清洁能源比例");
        cleanEnergyRatio.setUnit("%");
        cleanEnergyRatio.setFormula("可再生能源发电量 / 总发电量 * 100");
        
        // 添加依赖
        cleanEnergyRatio.addDependency(renewableGeneration);
        cleanEnergyRatio.addDependency(totalGeneration);
        
        System.out.println("清洁能源占比指标: " + cleanEnergyRatio);
        System.out.println("依赖指标: " + cleanEnergyRatio.getDependencies());
        System.out.println("公式验证: " + cleanEnergyRatio.isValidFormula());
    }
}
