package com.gridinsight.domain.service;

import com.gridinsight.domain.model.Metric;
import com.gridinsight.domain.model.DerivedMetric;
import com.gridinsight.domain.model.MetricType;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 公式解析器
 * 负责解析公式中的指标依赖关系和验证公式的完整性
 */
public class FormulaParser {
    
    // 指标标识符模式：分类.子分类.指标名称
    private static final Pattern METRIC_IDENTIFIER_PATTERN = 
        Pattern.compile("([\\w\\u4e00-\\u9fa5]+\\.[\\w\\u4e00-\\u9fa5]+\\.[\\w\\u4e00-\\u9fa5]+)");
    
    /**
     * 提取公式中的指标标识符
     * @param formula 计算公式
     * @return 指标标识符列表
     */
    public static List<String> extractMetricIdentifiers(String formula) {
        if (formula == null || formula.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        Set<String> identifiers = new HashSet<>();
        Matcher matcher = METRIC_IDENTIFIER_PATTERN.matcher(formula);
        
        while (matcher.find()) {
            identifiers.add(matcher.group(1));
        }
        
        return new ArrayList<>(identifiers);
    }
    
    /**
     * 解析公式中的依赖指标
     * @param formula 计算公式
     * @return 依赖的指标标识符列表
     */
    public static List<String> parseDependencies(String formula) {
        if (formula == null || formula.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        Set<String> dependencies = new HashSet<>();
        Matcher matcher = METRIC_IDENTIFIER_PATTERN.matcher(formula);
        
        while (matcher.find()) {
            String identifier = matcher.group(1);
            dependencies.add(identifier);
        }
        
        return new ArrayList<>(dependencies);
    }
    
    /**
     * 验证派生指标的公式和依赖关系
     * @param derivedMetric 派生指标
     * @param allMetrics 所有可用的指标映射
     * @return 验证结果
     */
    public static FormulaValidationResult validateDerivedMetric(DerivedMetric derivedMetric, 
                                                               Map<String, Metric> allMetrics) {
        FormulaValidationResult result = new FormulaValidationResult();
        
        // 1. 检查公式语法
        if (!FormulaEngine.validateFormulaSyntax(derivedMetric.getFormula())) {
            result.addError("公式语法错误: " + derivedMetric.getFormula());
            return result;
        }
        
        // 2. 解析公式中的依赖
        List<String> formulaDependencies = parseDependencies(derivedMetric.getFormula());
        
        // 3. 检查依赖指标是否存在
        for (String dependencyId : formulaDependencies) {
            if (!allMetrics.containsKey(dependencyId)) {
                result.addError("依赖指标不存在: " + dependencyId);
            }
        }
        
        // 4. 检查依赖指标是否在依赖列表中
        List<String> declaredDependencies = new ArrayList<>();
        for (Metric dependency : derivedMetric.getDependencies()) {
            declaredDependencies.add(dependency.getIdentifier());
        }
        
        for (String formulaDep : formulaDependencies) {
            if (!declaredDependencies.contains(formulaDep)) {
                result.addWarning("公式中引用的指标未在依赖列表中声明: " + formulaDep);
            }
        }
        
        // 5. 检查是否有未使用的依赖
        for (String declaredDep : declaredDependencies) {
            if (!formulaDependencies.contains(declaredDep)) {
                result.addWarning("依赖列表中包含未在公式中使用的指标: " + declaredDep);
            }
        }
        
        // 6. 检查循环依赖
        if (hasCircularDependency(derivedMetric, allMetrics)) {
            result.addError("检测到循环依赖");
        }
        
        return result;
    }
    
    /**
     * 检查循环依赖
     * @param derivedMetric 派生指标
     * @param allMetrics 所有指标映射
     * @return true 如果存在循环依赖
     */
    private static boolean hasCircularDependency(DerivedMetric derivedMetric, Map<String, Metric> allMetrics) {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        
        return hasCircularDependencyRecursive(derivedMetric.getIdentifier(), 
                                            derivedMetric, allMetrics, visited, recursionStack);
    }
    
    /**
     * 递归检查循环依赖
     */
    private static boolean hasCircularDependencyRecursive(String currentMetricId, 
                                                         DerivedMetric derivedMetric,
                                                         Map<String, Metric> allMetrics,
                                                         Set<String> visited, 
                                                         Set<String> recursionStack) {
        
        if (recursionStack.contains(currentMetricId)) {
            return true; // 发现循环依赖
        }
        
        if (visited.contains(currentMetricId)) {
            return false; // 已经访问过，无循环依赖
        }
        
        visited.add(currentMetricId);
        recursionStack.add(currentMetricId);
        
        // 检查当前指标的依赖
        for (Metric dependency : derivedMetric.getDependencies()) {
            if (dependency instanceof DerivedMetric) {
                DerivedMetric derivedDep = (DerivedMetric) dependency;
                if (hasCircularDependencyRecursive(dependency.getIdentifier(), 
                                                 derivedDep, allMetrics, visited, recursionStack)) {
                    return true;
                }
            }
        }
        
        recursionStack.remove(currentMetricId);
        return false;
    }
    
    /**
     * 生成公式的依赖图
     * @param derivedMetric 派生指标
     * @param allMetrics 所有指标映射
     * @return 依赖图
     */
    public static DependencyGraph generateDependencyGraph(DerivedMetric derivedMetric, 
                                                         Map<String, Metric> allMetrics) {
        DependencyGraph graph = new DependencyGraph();
        Set<String> processed = new HashSet<>();
        
        buildDependencyGraph(derivedMetric, allMetrics, graph, processed);
        
        return graph;
    }
    
    /**
     * 构建依赖图
     */
    private static void buildDependencyGraph(DerivedMetric derivedMetric, 
                                           Map<String, Metric> allMetrics,
                                           DependencyGraph graph, 
                                           Set<String> processed) {
        
        String metricId = derivedMetric.getIdentifier();
        
        if (processed.contains(metricId)) {
            return;
        }
        
        processed.add(metricId);
        graph.addNode(metricId, derivedMetric.getMetricType());
        
        for (Metric dependency : derivedMetric.getDependencies()) {
            graph.addEdge(metricId, dependency.getIdentifier());
            
            if (dependency instanceof DerivedMetric) {
                buildDependencyGraph((DerivedMetric) dependency, allMetrics, graph, processed);
            } else {
                graph.addNode(dependency.getIdentifier(), dependency.getMetricType());
            }
        }
    }
    
    /**
     * 公式验证结果
     */
    public static class FormulaValidationResult {
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        public String getSummary() {
            StringBuilder summary = new StringBuilder();
            if (!errors.isEmpty()) {
                summary.append("错误: ").append(String.join("; ", errors));
            }
            if (!warnings.isEmpty()) {
                if (summary.length() > 0) summary.append("; ");
                summary.append("警告: ").append(String.join("; ", warnings));
            }
            return summary.toString();
        }
    }
    
    /**
     * 依赖图
     */
    public static class DependencyGraph {
        private Map<String, MetricType> nodes = new HashMap<>();
        private Map<String, Set<String>> edges = new HashMap<>();
        
        public void addNode(String metricId, MetricType metricType) {
            nodes.put(metricId, metricType);
            edges.putIfAbsent(metricId, new HashSet<>());
        }
        
        public void addEdge(String from, String to) {
            edges.get(from).add(to);
        }
        
        public Set<String> getNodes() {
            return nodes.keySet();
        }
        
        public Set<String> getDependencies(String metricId) {
            return edges.getOrDefault(metricId, new HashSet<>());
        }
        
        public MetricType getNodeType(String metricId) {
            return nodes.get(metricId);
        }
        
        /**
         * 获取计算顺序（拓扑排序）
         * @return 按依赖顺序排列的指标列表
         */
        public List<String> getCalculationOrder() {
            List<String> result = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            Set<String> tempMark = new HashSet<>();
            
            for (String node : nodes.keySet()) {
                if (!visited.contains(node)) {
                    visit(node, result, visited, tempMark);
                }
            }
            
            Collections.reverse(result);
            return result;
        }
        
        private void visit(String node, List<String> result, Set<String> visited, Set<String> tempMark) {
            if (tempMark.contains(node)) {
                throw new IllegalArgumentException("检测到循环依赖: " + node);
            }
            
            if (visited.contains(node)) {
                return;
            }
            
            tempMark.add(node);
            
            for (String dependency : edges.get(node)) {
                visit(dependency, result, visited, tempMark);
            }
            
            tempMark.remove(node);
            visited.add(node);
            result.add(node);
        }
    }
}
