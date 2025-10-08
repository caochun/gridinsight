package com.gridinsight.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gridinsight.domain.model.Indicator;
import com.gridinsight.domain.model.DerivedIndicator;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 指标查询服务
 * 根据fullIdentifier查询指标定义并计算值
 */
@Service
public class IndicatorQueryService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Indicator> basicIndicators = new ConcurrentHashMap<>();
    private final Map<String, DerivedIndicator> derivedIndicators = new ConcurrentHashMap<>();
    private final MockDataSourceService mockDataSourceService;

    public IndicatorQueryService(MockDataSourceService mockDataSourceService) {
        this.mockDataSourceService = mockDataSourceService;
    }

    @PostConstruct
    public void loadIndicators() {
        try {
            // 加载基础指标
            ClassPathResource basicResource = new ClassPathResource("output/basic_indicators_20251008_181648.json");
            if (basicResource.exists()) {
                Indicator[] basicArray = objectMapper.readValue(basicResource.getInputStream(), Indicator[].class);
                for (Indicator indicator : basicArray) {
                    basicIndicators.put(indicator.getFullIdentifier(), indicator);
                }
            }

            // 加载衍生指标
            ClassPathResource derivedResource = new ClassPathResource("output/derived_indicators_20251008_181648.json");
            if (derivedResource.exists()) {
                DerivedIndicator[] derivedArray = objectMapper.readValue(derivedResource.getInputStream(), DerivedIndicator[].class);
                for (DerivedIndicator indicator : derivedArray) {
                    derivedIndicators.put(indicator.getFullIdentifier(), indicator);
                }
            }

            System.out.println("已加载基础指标: " + basicIndicators.size() + " 个");
            System.out.println("已加载衍生指标: " + derivedIndicators.size() + " 个");
            
            // 打印前几个指标标识符用于调试
            if (!basicIndicators.isEmpty()) {
                System.out.println("基础指标示例:");
                basicIndicators.keySet().stream().limit(3).forEach(System.out::println);
            }
            if (!derivedIndicators.isEmpty()) {
                System.out.println("衍生指标示例:");
                derivedIndicators.keySet().stream().limit(3).forEach(System.out::println);
            }
        } catch (IOException e) {
            System.err.println("加载指标数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据fullIdentifier查询指标值
     */
    public IndicatorValueResponse queryIndicatorValue(String fullIdentifier) {
        try {
            // 首先检查是否为衍生指标
            if (derivedIndicators.containsKey(fullIdentifier)) {
                return calculateDerivedIndicator(fullIdentifier);
            }
            
            // 检查基础指标
            if (basicIndicators.containsKey(fullIdentifier)) {
                return calculateBasicIndicator(fullIdentifier);
            }
            
            return IndicatorValueResponse.error("未找到指标: " + fullIdentifier);
            
        } catch (Exception e) {
            return IndicatorValueResponse.error("计算指标值时发生错误: " + e.getMessage());
        }
    }

    /**
     * 计算基础指标值
     */
    private IndicatorValueResponse calculateBasicIndicator(String fullIdentifier) {
        Indicator indicator = basicIndicators.get(fullIdentifier);
        if (indicator == null) {
            return IndicatorValueResponse.error("基础指标不存在: " + fullIdentifier);
        }

        // 调用数据源获取值
        Double value = mockDataSourceService.getDataFromSource(indicator.getDataSource());
        
        return IndicatorValueResponse.success(
            indicator.getName(),
            indicator.getFullIdentifier(),
            indicator.getUnit(),
            value,
            indicator.getDataSource(),
            "基础指标"
        );
    }

    /**
     * 计算衍生指标值
     */
    private IndicatorValueResponse calculateDerivedIndicator(String fullIdentifier) {
        DerivedIndicator derivedIndicator = derivedIndicators.get(fullIdentifier);
        if (derivedIndicator == null) {
            return IndicatorValueResponse.error("衍生指标不存在: " + fullIdentifier);
        }

        try {
            // 获取依赖指标的值
            Map<String, Double> dependencyValues = new HashMap<>();
            for (Indicator dependency : derivedIndicator.getDependencies()) {
                String depFullIdentifier = dependency.getFullIdentifier();
                Double depValue;
                
                if (basicIndicators.containsKey(depFullIdentifier)) {
                    // 基础指标直接获取数据
                    depValue = mockDataSourceService.getDataFromSource(dependency.getDataSource());
                } else if (derivedIndicators.containsKey(depFullIdentifier)) {
                    // 递归计算衍生指标
                    IndicatorValueResponse depResponse = calculateDerivedIndicator(depFullIdentifier);
                    if (!depResponse.isSuccess()) {
                        return IndicatorValueResponse.error("依赖指标计算失败: " + depFullIdentifier + " - " + depResponse.getErrorMessage());
                    }
                    depValue = depResponse.getValue();
                } else {
                    return IndicatorValueResponse.error("依赖指标不存在: " + depFullIdentifier);
                }
                
                dependencyValues.put(dependency.getName(), depValue);
            }

            // 根据公式计算最终值
            Double result = calculateFormula(derivedIndicator.getFormula(), dependencyValues);
            
            return IndicatorValueResponse.success(
                derivedIndicator.getName(),
                derivedIndicator.getFullIdentifier(),
                derivedIndicator.getUnit(),
                result,
                "衍生指标",
                derivedIndicator.getFormula(),
                dependencyValues
            );
            
        } catch (Exception e) {
            return IndicatorValueResponse.error("计算衍生指标时发生错误: " + e.getMessage());
        }
    }

    /**
     * 根据公式和依赖值计算结果
     */
    private Double calculateFormula(String formula, Map<String, Double> dependencyValues) {
        String expression = formula;
        
        // 替换公式中的指标名称为实际值
        for (Map.Entry<String, Double> entry : dependencyValues.entrySet()) {
            String indicatorName = entry.getKey();
            Double value = entry.getValue();
            expression = expression.replace(indicatorName, value.toString());
        }
        
        // 简单的数学表达式计算（这里使用正则表达式匹配和替换）
        // 实际项目中可能需要使用更复杂的表达式解析器
        return evaluateExpression(expression);
    }

    /**
     * 简单的数学表达式计算
     * 支持基本的四则运算和括号
     */
    private Double evaluateExpression(String expression) {
        try {
            // 移除所有空格
            expression = expression.replaceAll("\\s+", "");
            
            // 处理括号
            while (expression.contains("(")) {
                Pattern pattern = Pattern.compile("\\(([^()]+)\\)");
                Matcher matcher = pattern.matcher(expression);
                if (matcher.find()) {
                    String subExpression = matcher.group(1);
                    Double result = evaluateSimpleExpression(subExpression);
                    expression = expression.replace(matcher.group(0), result.toString());
                } else {
                    break;
                }
            }
            
            return evaluateSimpleExpression(expression);
        } catch (Exception e) {
            throw new RuntimeException("表达式计算错误: " + expression, e);
        }
    }

    /**
     * 计算简单表达式（无括号）
     */
    private Double evaluateSimpleExpression(String expression) {
        // 处理乘除
        Pattern mulDivPattern = Pattern.compile("([\\d.]+)\\s*([*/])\\s*([\\d.]+)");
        Matcher matcher = mulDivPattern.matcher(expression);
        while (matcher.find()) {
            double left = Double.parseDouble(matcher.group(1));
            String operator = matcher.group(2);
            double right = Double.parseDouble(matcher.group(3));
            double result = operator.equals("*") ? left * right : left / right;
            expression = expression.replace(matcher.group(0), String.valueOf(result));
            matcher = mulDivPattern.matcher(expression);
        }
        
        // 处理加减
        Pattern addSubPattern = Pattern.compile("([\\d.]+)\\s*([+-])\\s*([\\d.]+)");
        matcher = addSubPattern.matcher(expression);
        while (matcher.find()) {
            double left = Double.parseDouble(matcher.group(1));
            String operator = matcher.group(2);
            double right = Double.parseDouble(matcher.group(3));
            double result = operator.equals("+") ? left + right : left - right;
            expression = expression.replace(matcher.group(0), String.valueOf(result));
            matcher = addSubPattern.matcher(expression);
        }
        
        return Double.parseDouble(expression);
    }

    /**
     * 获取所有可用的指标标识符
     */
    public List<String> getAllIndicatorIdentifiers() {
        List<String> identifiers = new ArrayList<>();
        identifiers.addAll(basicIndicators.keySet());
        identifiers.addAll(derivedIndicators.keySet());
        return identifiers;
    }

    /**
     * 指标值响应类
     */
    public static class IndicatorValueResponse {
        private boolean success;
        private String indicatorName;
        private String fullIdentifier;
        private String unit;
        private Double value;
        private String dataSource;
        private String indicatorType;
        private String formula;
        private Map<String, Double> dependencyValues;
        private String errorMessage;

        public static IndicatorValueResponse success(String indicatorName, String fullIdentifier, 
                                                   String unit, Double value, String dataSource, String indicatorType) {
            IndicatorValueResponse response = new IndicatorValueResponse();
            response.success = true;
            response.indicatorName = indicatorName;
            response.fullIdentifier = fullIdentifier;
            response.unit = unit;
            response.value = value;
            response.dataSource = dataSource;
            response.indicatorType = indicatorType;
            return response;
        }

        public static IndicatorValueResponse success(String indicatorName, String fullIdentifier, 
                                                   String unit, Double value, String indicatorType, 
                                                   String formula, Map<String, Double> dependencyValues) {
            IndicatorValueResponse response = new IndicatorValueResponse();
            response.success = true;
            response.indicatorName = indicatorName;
            response.fullIdentifier = fullIdentifier;
            response.unit = unit;
            response.value = value;
            response.indicatorType = indicatorType;
            response.formula = formula;
            response.dependencyValues = dependencyValues;
            return response;
        }

        public static IndicatorValueResponse error(String errorMessage) {
            IndicatorValueResponse response = new IndicatorValueResponse();
            response.success = false;
            response.errorMessage = errorMessage;
            return response;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getIndicatorName() { return indicatorName; }
        public String getFullIdentifier() { return fullIdentifier; }
        public String getUnit() { return unit; }
        public Double getValue() { return value; }
        public String getDataSource() { return dataSource; }
        public String getIndicatorType() { return indicatorType; }
        public String getFormula() { return formula; }
        public Map<String, Double> getDependencyValues() { return dependencyValues; }
        public String getErrorMessage() { return errorMessage; }
    }
}
