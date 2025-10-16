package com.gridinsight.domain.service;

import com.gridinsight.domain.model.MetricValue;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;

/**
 * FormulaEngine测试
 */
public class FormulaEngineTest {
    
    @Test
    void testBasicArithmetic() {
        Map<String, MetricValue> values = new HashMap<>();
        values.put("测试.基础.A", MetricValue.good("测试.基础.A", 10.0, "个"));
        values.put("测试.基础.B", MetricValue.good("测试.基础.B", 5.0, "个"));
        
        // 测试加法
        MetricValue result = FormulaEngine.calculate("测试.基础.A + 测试.基础.B", values);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValue()).isCloseTo(15.0, within(0.001));
        
        // 测试减法
        result = FormulaEngine.calculate("测试.基础.A - 测试.基础.B", values);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValue()).isCloseTo(5.0, within(0.001));
        
        // 测试乘法
        result = FormulaEngine.calculate("测试.基础.A * 测试.基础.B", values);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValue()).isCloseTo(50.0, within(0.001));
        
        // 测试除法
        result = FormulaEngine.calculate("测试.基础.A / 测试.基础.B", values);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValue()).isCloseTo(2.0, within(0.001));
    }
    
    @Test
    void testComplexExpression() {
        Map<String, MetricValue> values = new HashMap<>();
        values.put("测试.复杂.A", MetricValue.good("测试.复杂.A", 100.0, "个"));
        values.put("测试.复杂.B", MetricValue.good("测试.复杂.B", 20.0, "个"));
        
        // 测试复杂表达式：(1 - B / A) * 100
        MetricValue result = FormulaEngine.calculate("(1 - 测试.复杂.B / 测试.复杂.A) * 100", values);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValue()).isCloseTo(80.0, within(0.001));
    }
    
    @Test
    void testMathematicalFunctions() {
        Map<String, MetricValue> values = new HashMap<>();
        values.put("测试.函数.A", MetricValue.good("测试.函数.A", 16.0, "个"));
        values.put("测试.函数.B", MetricValue.good("测试.函数.B", 4.0, "个"));
        
        // 测试平方根
        MetricValue result = FormulaEngine.calculate("sqrt(测试.函数.A)", values);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValue()).isCloseTo(4.0, within(0.001));
        
        // 测试绝对值
        result = FormulaEngine.calculate("abs(-5)", values);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValue()).isCloseTo(5.0, within(0.001));
        
        // 测试最大值
        result = FormulaEngine.calculate("max(测试.函数.A, 测试.函数.B)", values);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValue()).isCloseTo(16.0, within(0.001));
        
        // 测试最小值
        result = FormulaEngine.calculate("min(测试.函数.A, 测试.函数.B)", values);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValue()).isCloseTo(4.0, within(0.001));
    }
    
    @Test
    void testPowerOperation() {
        Map<String, MetricValue> values = new HashMap<>();
        values.put("测试.幂运算.A", MetricValue.good("测试.幂运算.A", 2.0, "个"));
        values.put("测试.幂运算.B", MetricValue.good("测试.幂运算.B", 3.0, "个"));
        
        // 测试幂运算
        MetricValue result = FormulaEngine.calculate("测试.幂运算.A ^ 测试.幂运算.B", values);
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValue()).isCloseTo(8.0, within(0.001));
    }
    
    @Test
    void testMetricIdentifierReplacement() {
        Map<String, MetricValue> values = new HashMap<>();
        values.put("中压拓扑.配变统计.配变总数", MetricValue.good("配变总数", 1000.0, "个"));
        values.put("中压拓扑.拓扑不一致.不一致数量", MetricValue.good("不一致数量", 50.0, "个"));
        
        // 测试指标标识符替换
        MetricValue result = FormulaEngine.calculate(
            "(1 - 中压拓扑.拓扑不一致.不一致数量 / 中压拓扑.配变统计.配变总数) * 100", 
            values
        );
        assertThat(result.isValid()).isTrue();
        assertThat(result.getValue()).isCloseTo(95.0, within(0.001));
    }
    
    @Test
    void testErrorHandling() {
        Map<String, MetricValue> values = new HashMap<>();
        
        // 测试除零错误
        values.put("测试.错误.A", MetricValue.good("测试.错误.A", 10.0, "个"));
        values.put("测试.错误.B", MetricValue.good("测试.错误.B", 0.0, "个"));
        
        MetricValue result = FormulaEngine.calculate("测试.错误.A / 测试.错误.B", values);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getDataSource()).contains("公式计算错误");
        
        // 测试缺少指标
        result = FormulaEngine.calculate("不存在的.指标.名称 * 2", values);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getDataSource()).contains("无法找到指标值");
    }
    
    @Test
    void testFormulaSyntaxValidation() {
        // 测试有效公式
        assertThat(FormulaEngine.validateFormulaSyntax("测试.语法.A + 测试.语法.B")).isTrue();
        assertThat(FormulaEngine.validateFormulaSyntax("(测试.语法.A + 测试.语法.B) * 测试.语法.C")).isTrue();
        assertThat(FormulaEngine.validateFormulaSyntax("sqrt(测试.语法.A) + abs(测试.语法.B)")).isTrue();
        
        // 测试无效公式
        assertThat(FormulaEngine.validateFormulaSyntax("")).isFalse();
        assertThat(FormulaEngine.validateFormulaSyntax(null)).isFalse();
        assertThat(FormulaEngine.validateFormulaSyntax("(测试.语法.A + 测试.语法.B")).isFalse(); // 括号不匹配
        assertThat(FormulaEngine.validateFormulaSyntax("测试.语法.A ++ 测试.语法.B")).isFalse(); // 连续运算符
        assertThat(FormulaEngine.validateFormulaSyntax("unknownFunc(测试.语法.A)")).isFalse(); // 未知函数
    }
    
    @Test
    void testEmptyFormula() {
        Map<String, MetricValue> values = new HashMap<>();
        
        MetricValue result = FormulaEngine.calculate("", values);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getDataSource()).contains("公式为空");
        
        result = FormulaEngine.calculate(null, values);
        assertThat(result.isValid()).isFalse();
        assertThat(result.getDataSource()).contains("公式为空");
    }
}
