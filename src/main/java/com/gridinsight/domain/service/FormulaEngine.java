package com.gridinsight.domain.service;

import com.gridinsight.domain.model.MetricValue;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 公式计算引擎
 * 负责解析和计算派生指标的数学表达式
 */
public class FormulaEngine {
    
    /**
     * 公式语法说明：
     * 1. 指标标识符：分类.子分类.指标名称
     * 2. 数学运算符：+ (加), - (减), * (乘), / (除), ^ (幂)
     * 3. 括号：() 用于改变运算优先级
     * 4. 函数：abs(), sqrt(), log(), exp(), min(), max()
     * 
     * 示例公式：
     * - (1 - 中压拓扑.拓扑不一致.配变挂接馈线不一致数量 / 中压拓扑.配变统计.配变总数) * 100
     * - sqrt(中压拓扑.配变统计.配变总数 * 低压用户关系.低压用户统计.全省低压用户总数)
     * - max(中压拓扑.配变统计.配变总数, 低压用户关系.低压用户统计.全省低压用户总数)
     */
    
    // 正则表达式模式
    private static final Pattern METRIC_PATTERN = Pattern.compile("([\\w\\u4e00-\\u9fa5]+\\.[\\w\\u4e00-\\u9fa5]+\\.[\\w\\u4e00-\\u9fa5]+)");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("(\\w+)\\s*\\(([^)]+)\\)");
    private static final Pattern PARENTHESES_PATTERN = Pattern.compile("\\(([^()]+)\\)");
    
    /**
     * 计算派生指标值
     * @param formula 计算公式
     * @param metricValues 指标值映射表
     * @return 计算结果
     */
    public static MetricValue calculate(String formula, Map<String, MetricValue> metricValues) {
        if (formula == null || formula.trim().isEmpty()) {
            return MetricValue.error("", "公式为空");
        }
        
        try {
            // 1. 替换指标标识符为实际数值
            String expression = replaceMetricIdentifiers(formula, metricValues);
            
            // 2. 处理函数
            expression = processFunctions(expression);
            
            // 3. 计算数学表达式
            BigDecimal result = evaluateExpression(expression);
            
            // 4. 创建结果值
            MetricValue metricValue = new MetricValue("", result.doubleValue(), "", 
                                 java.time.LocalDateTime.now(), MetricValue.DataQuality.GOOD);
            return metricValue;
            
        } catch (Exception e) {
            return MetricValue.error("", "公式计算错误: " + e.getMessage());
        }
    }
    
    /**
     * 替换公式中的指标标识符为实际数值
     */
    private static String replaceMetricIdentifiers(String formula, Map<String, MetricValue> metricValues) {
        String result = formula;
        Matcher matcher = METRIC_PATTERN.matcher(formula);
        
        while (matcher.find()) {
            String identifier = matcher.group(1);
            MetricValue metricValue = metricValues.get(identifier);
            
            if (metricValue != null && metricValue.isValid()) {
                // 替换为数值
                result = result.replace(identifier, metricValue.getValue().toString());
            } else {
                throw new IllegalArgumentException("无法找到指标值: " + identifier);
            }
        }
        
        return result;
    }
    
    /**
     * 处理数学函数
     */
    private static String processFunctions(String expression) {
        String result = expression;
        Matcher matcher = FUNCTION_PATTERN.matcher(result);
        
        while (matcher.find()) {
            String functionName = matcher.group(1).toLowerCase();
            String functionArgs = matcher.group(2);
            String functionCall = matcher.group(0);
            
            // 先计算函数参数中的表达式
            String evaluatedArgs = evaluateFunctionArgs(functionArgs);
            
            BigDecimal functionResult = calculateFunction(functionName, evaluatedArgs);
            result = result.replace(functionCall, functionResult.toString());
            
            // 重新匹配，因为字符串已经改变
            matcher = FUNCTION_PATTERN.matcher(result);
        }
        
        return result;
    }
    
    /**
     * 计算函数参数
     */
    private static String evaluateFunctionArgs(String args) {
        // 按逗号分割参数，分别计算每个参数
        String[] argArray = args.split(",");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < argArray.length; i++) {
            if (i > 0) {
                result.append(",");
            }
            // 计算每个参数
            BigDecimal evaluatedArg = evaluateExpression(argArray[i].trim());
            result.append(evaluatedArg.toString());
        }
        
        return result.toString();
    }
    
    /**
     * 计算数学函数
     */
    private static BigDecimal calculateFunction(String functionName, String args) {
        String[] argArray = args.split(",");
        
        switch (functionName) {
            case "abs":
                if (argArray.length != 1) throw new IllegalArgumentException("abs函数需要1个参数");
                return new BigDecimal(argArray[0].trim()).abs();
                
            case "sqrt":
                if (argArray.length != 1) throw new IllegalArgumentException("sqrt函数需要1个参数");
                double sqrtValue = Math.sqrt(Double.parseDouble(argArray[0].trim()));
                return new BigDecimal(sqrtValue);
                
            case "log":
                if (argArray.length != 1) throw new IllegalArgumentException("log函数需要1个参数");
                double logValue = Math.log(Double.parseDouble(argArray[0].trim()));
                return new BigDecimal(logValue);
                
            case "exp":
                if (argArray.length != 1) throw new IllegalArgumentException("exp函数需要1个参数");
                double expValue = Math.exp(Double.parseDouble(argArray[0].trim()));
                return new BigDecimal(expValue);
                
            case "min":
                if (argArray.length < 2) throw new IllegalArgumentException("min函数需要至少2个参数");
                BigDecimal minValue = new BigDecimal(argArray[0].trim());
                for (int i = 1; i < argArray.length; i++) {
                    BigDecimal current = new BigDecimal(argArray[i].trim());
                    if (current.compareTo(minValue) < 0) {
                        minValue = current;
                    }
                }
                return minValue;
                
            case "max":
                if (argArray.length < 2) throw new IllegalArgumentException("max函数需要至少2个参数");
                BigDecimal maxValue = new BigDecimal(argArray[0].trim());
                for (int i = 1; i < argArray.length; i++) {
                    BigDecimal current = new BigDecimal(argArray[i].trim());
                    if (current.compareTo(maxValue) > 0) {
                        maxValue = current;
                    }
                }
                return maxValue;
                
            default:
                throw new IllegalArgumentException("不支持的函数: " + functionName);
        }
    }
    
    /**
     * 计算数学表达式
     */
    private static BigDecimal evaluateExpression(String expression) {
        // 移除空格
        expression = expression.replaceAll("\\s+", "");
        
        // 处理括号
        while (expression.contains("(")) {
            Matcher matcher = PARENTHESES_PATTERN.matcher(expression);
            if (matcher.find()) {
                String subExpression = matcher.group(1);
                BigDecimal result = evaluateSimpleExpression(subExpression);
                expression = expression.replace(matcher.group(0), result.toString());
            } else {
                break;
            }
        }
        
        return evaluateSimpleExpression(expression);
    }
    
    /**
     * 计算简单表达式（无括号）
     */
    private static BigDecimal evaluateSimpleExpression(String expression) {
        // 处理幂运算 (^)
        expression = processPower(expression);
        
        // 处理乘除运算 (*, /)
        expression = processMultiplyDivide(expression);
        
        // 处理加减运算 (+, -)
        expression = processAddSubtract(expression);
        
        return new BigDecimal(expression);
    }
    
    /**
     * 处理幂运算
     */
    private static String processPower(String expression) {
        Pattern pattern = Pattern.compile("([\\d.]+)\\^([\\d.]+)");
        Matcher matcher = pattern.matcher(expression);
        
        while (matcher.find()) {
            BigDecimal base = new BigDecimal(matcher.group(1));
            BigDecimal exponent = new BigDecimal(matcher.group(2));
            BigDecimal result = base.pow(exponent.intValue());
            expression = expression.replace(matcher.group(0), result.toString());
            matcher = pattern.matcher(expression);
        }
        
        return expression;
    }
    
    /**
     * 处理乘除运算
     */
    private static String processMultiplyDivide(String expression) {
        Pattern pattern = Pattern.compile("([\\d.]+)\\s*([*/])\\s*([\\d.]+)");
        Matcher matcher = pattern.matcher(expression);
        
        while (matcher.find()) {
            try {
                String leftStr = matcher.group(1).trim();
                String operator = matcher.group(2).trim();
                String rightStr = matcher.group(3).trim();
                
                BigDecimal left = new BigDecimal(leftStr);
                BigDecimal right = new BigDecimal(rightStr);
                
                BigDecimal result;
                if (operator.equals("*")) {
                    result = left.multiply(right);
                } else {
                    if (right.compareTo(BigDecimal.ZERO) == 0) {
                        throw new ArithmeticException("除零错误");
                    }
                    result = left.divide(right, 10, RoundingMode.HALF_UP);
                }
                
                expression = expression.replace(matcher.group(0), result.toString());
                matcher = pattern.matcher(expression);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("无法解析数字: " + matcher.group(0) + " - " + e.getMessage());
            }
        }
        
        return expression;
    }
    
    /**
     * 处理加减运算
     */
    private static String processAddSubtract(String expression) {
        Pattern pattern = Pattern.compile("([\\d.]+)\\s*([+-])\\s*([\\d.]+)");
        Matcher matcher = pattern.matcher(expression);
        
        while (matcher.find()) {
            BigDecimal left = new BigDecimal(matcher.group(1));
            String operator = matcher.group(2);
            BigDecimal right = new BigDecimal(matcher.group(3));
            
            BigDecimal result;
            if (operator.equals("+")) {
                result = left.add(right);
            } else {
                result = left.subtract(right);
            }
            
            expression = expression.replace(matcher.group(0), result.toString());
            matcher = pattern.matcher(expression);
        }
        
        return expression;
    }
    
    /**
     * 验证公式语法
     * @param formula 公式字符串
     * @return true 如果公式语法正确
     */
    public static boolean validateFormulaSyntax(String formula) {
        if (formula == null || formula.trim().isEmpty()) {
            return false;
        }
        
        try {
            // 检查括号匹配
            if (!checkParenthesesMatch(formula)) {
                return false;
            }
            
            // 检查运算符语法
            if (!checkOperatorSyntax(formula)) {
                return false;
            }
            
            // 检查函数语法
            if (!checkFunctionSyntax(formula)) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查括号匹配
     */
    private static boolean checkParenthesesMatch(String formula) {
        int count = 0;
        for (char c : formula.toCharArray()) {
            if (c == '(') count++;
            else if (c == ')') count--;
            if (count < 0) return false;
        }
        return count == 0;
    }
    
    /**
     * 检查运算符语法
     */
    private static boolean checkOperatorSyntax(String formula) {
        // 简化检查：不能有连续的运算符
        return !formula.matches(".*[+\\-*/^]{2,}.*");
    }
    
    /**
     * 检查函数语法
     */
    private static boolean checkFunctionSyntax(String formula) {
        Pattern functionPattern = Pattern.compile("(\\w+)\\s*\\(([^)]+)\\)");
        Matcher matcher = functionPattern.matcher(formula);
        
        while (matcher.find()) {
            String functionName = matcher.group(1).toLowerCase();
            // 检查是否为支持的函数
            if (!isSupportedFunction(functionName)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 检查是否为支持的函数
     */
    private static boolean isSupportedFunction(String functionName) {
        return functionName.equals("abs") || functionName.equals("sqrt") || 
               functionName.equals("log") || functionName.equals("exp") ||
               functionName.equals("min") || functionName.equals("max");
    }
}
