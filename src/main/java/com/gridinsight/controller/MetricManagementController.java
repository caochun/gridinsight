package com.gridinsight.controller;

import com.gridinsight.domain.model.*;
import com.gridinsight.service.MetricConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 指标管理Web UI控制器
 * 提供指标定义的Web界面管理功能
 */
@Controller
@RequestMapping("/admin/metrics")
public class MetricManagementController {

    @Autowired
    private MetricConfigService metricConfigService;

    /**
     * 指标管理首页
     */
    @GetMapping
    public String index(Model model) {
        Map<String, Object> stats = metricConfigService.getMetricStats();
        model.addAttribute("stats", stats);
        return "metric-management";
    }

    /**
     * 基础指标管理页面
     */
    @GetMapping("/basic")
    public String basicMetrics(Model model) {
        Map<String, BasicMetric> basicMetrics = metricConfigService.getAllBasicMetrics();
        model.addAttribute("basicMetrics", basicMetrics);
        return "basic-metrics";
    }

    /**
     * 派生指标管理页面
     */
    @GetMapping("/derived")
    public String derivedMetrics(Model model) {
        Map<String, DerivedMetric> derivedMetrics = metricConfigService.getAllDerivedMetrics();
        model.addAttribute("derivedMetrics", derivedMetrics);
        return "derived-metrics";
    }

    /**
     * 添加基础指标页面
     */
    @GetMapping("/basic/add")
    public String addBasicMetricForm(Model model) {
        model.addAttribute("metric", new BasicMetricForm());
        model.addAttribute("dataSourceTypes", DataSource.SourceType.values());
        return "add-basic-metric";
    }

    /**
     * 添加派生指标页面
     */
    @GetMapping("/derived/add")
    public String addDerivedMetricForm(Model model) {
        model.addAttribute("metric", new DerivedMetricForm());
        
        // 创建简化的指标列表用于模板
        List<Map<String, String>> availableMetrics = metricConfigService.getAllMetrics().entrySet().stream()
                .map(entry -> {
                    Map<String, String> metricInfo = new HashMap<>();
                    metricInfo.put("identifier", entry.getKey());
                    metricInfo.put("name", entry.getValue().getName());
                    return metricInfo;
                })
                .collect(Collectors.toList());
        model.addAttribute("availableMetrics", availableMetrics);
        
        return "add-derived-metric";
    }

    /**
     * 编辑基础指标页面
     */
    @GetMapping("/basic/edit/{identifier}")
    public String editBasicMetricForm(@PathVariable String identifier, Model model) {
        BasicMetric metric = metricConfigService.getBasicMetric(identifier);
        if (metric == null) {
            return "redirect:/admin/metrics/basic?error=指标不存在";
        }
        
        BasicMetricForm form = new BasicMetricForm();
        form.setIdentifier(identifier);
        form.setName(metric.getName());
        form.setCategory(metric.getCategory());
        form.setSubCategory(metric.getSubCategory());
        form.setUnit(metric.getUnit());
        form.setDescription(metric.getDescription());
        
        if (metric.getDataSource() != null) {
            form.setSourceType(metric.getDataSource().getSourceType().name());
            form.setSourceAddress(metric.getDataSource().getSourceAddress());
            form.setSourceName(metric.getDataSource().getSourceName());
            form.setRefreshInterval(metric.getDataSource().getRefreshInterval());
            form.setEnabled(metric.getDataSource().getEnabled());
        }
        
        model.addAttribute("metric", form);
        model.addAttribute("dataSourceTypes", DataSource.SourceType.values());
        return "edit-basic-metric";
    }

    /**
     * 编辑派生指标页面
     */
    @GetMapping("/derived/edit/{identifier}")
    public String editDerivedMetricForm(@PathVariable String identifier, Model model) {
        DerivedMetric metric = metricConfigService.getDerivedMetric(identifier);
        if (metric == null) {
            return "redirect:/admin/metrics/derived?error=指标不存在";
        }
        
        DerivedMetricForm form = new DerivedMetricForm();
        form.setIdentifier(identifier);
        form.setName(metric.getName());
        form.setCategory(metric.getCategory());
        form.setSubCategory(metric.getSubCategory());
        form.setUnit(metric.getUnit());
        form.setDescription(metric.getDescription());
        form.setFormula(metric.getFormula());
        form.setUpdateStrategy(metric.getUpdateStrategy().name());
        form.setCalculationInterval(metric.getCalculationInterval());
        
        List<String> dependencies = metric.getDependencies().stream()
                .map(Metric::getIdentifier)
                .collect(Collectors.toList());
        form.setDependencies(dependencies);
        
        model.addAttribute("metric", form);
        
        // 创建简化的指标列表用于模板
        List<Map<String, String>> availableMetrics = metricConfigService.getAllMetrics().entrySet().stream()
                .map(entry -> {
                    Map<String, String> metricInfo = new HashMap<>();
                    metricInfo.put("identifier", entry.getKey());
                    metricInfo.put("name", entry.getValue().getName());
                    return metricInfo;
                })
                .collect(Collectors.toList());
        model.addAttribute("availableMetrics", availableMetrics);
        
        return "edit-derived-metric";
    }

    /**
     * 保存基础指标
     */
    @PostMapping("/basic/save")
    public String saveBasicMetric(@ModelAttribute BasicMetricForm form) {
        try {
            // 创建数据源
            DataSource dataSource = new DataSource(
                DataSource.SourceType.valueOf(form.getSourceType()),
                form.getSourceAddress(),
                form.getSourceName(),
                Integer.valueOf(form.getRefreshInterval()),
                form.isEnabled()
            );

            // 创建基础指标
            BasicMetric metric = new BasicMetric(
                form.getName(),
                form.getCategory(),
                form.getSubCategory(),
                form.getUnit(),
                form.getDescription(),
                dataSource
            );

            // 保存指标
            metricConfigService.addBasicMetric(form.getIdentifier(), metric);
            
            return "redirect:/admin/metrics/basic?success=基础指标保存成功";
        } catch (Exception e) {
            return "redirect:/admin/metrics/basic/add?error=" + e.getMessage();
        }
    }

    /**
     * 保存派生指标
     */
    @PostMapping("/derived/save")
    public String saveDerivedMetric(@ModelAttribute DerivedMetricForm form) {
        try {
            // 创建依赖指标列表
            List<Metric> dependencies = form.getDependencies().stream()
                    .map(metricConfigService::getMetric)
                    .filter(metric -> metric != null)
                    .collect(Collectors.toList());

            // 解析更新策略
            DerivedMetricUpdateStrategy updateStrategy;
            try {
                updateStrategy = DerivedMetricUpdateStrategy.valueOf(form.getUpdateStrategy());
            } catch (Exception e) {
                updateStrategy = DerivedMetricUpdateStrategy.REALTIME;
            }

            // 创建派生指标
            DerivedMetric metric = new DerivedMetric(
                form.getName(),
                form.getCategory(),
                form.getSubCategory(),
                form.getUnit(),
                form.getDescription(),
                form.getFormula(),
                dependencies,
                updateStrategy,
                form.getCalculationInterval()
            );

            // 保存指标
            metricConfigService.addDerivedMetric(form.getIdentifier(), metric);
            
            return "redirect:/admin/metrics/derived?success=派生指标保存成功";
        } catch (Exception e) {
            return "redirect:/admin/metrics/derived/add?error=" + e.getMessage();
        }
    }

    /**
     * 更新基础指标
     */
    @PostMapping("/basic/update")
    public String updateBasicMetric(@ModelAttribute BasicMetricForm form) {
        try {
            // 创建数据源
            DataSource dataSource = new DataSource(
                DataSource.SourceType.valueOf(form.getSourceType()),
                form.getSourceAddress(),
                form.getSourceName(),
                Integer.valueOf(form.getRefreshInterval()),
                form.isEnabled()
            );

            // 创建基础指标
            BasicMetric metric = new BasicMetric(
                form.getName(),
                form.getCategory(),
                form.getSubCategory(),
                form.getUnit(),
                form.getDescription(),
                dataSource
            );

            // 更新指标
            metricConfigService.updateBasicMetric(form.getIdentifier(), metric);
            
            return "redirect:/admin/metrics/basic?success=基础指标更新成功";
        } catch (Exception e) {
            return "redirect:/admin/metrics/basic/edit/" + form.getIdentifier() + "?error=" + e.getMessage();
        }
    }

    /**
     * 更新派生指标
     */
    @PostMapping("/derived/update")
    public String updateDerivedMetric(@ModelAttribute DerivedMetricForm form) {
        try {
            // 创建依赖指标列表
            List<Metric> dependencies = form.getDependencies().stream()
                    .map(metricConfigService::getMetric)
                    .filter(metric -> metric != null)
                    .collect(Collectors.toList());

            // 解析更新策略
            DerivedMetricUpdateStrategy updateStrategy;
            try {
                updateStrategy = DerivedMetricUpdateStrategy.valueOf(form.getUpdateStrategy());
            } catch (Exception e) {
                updateStrategy = DerivedMetricUpdateStrategy.REALTIME;
            }

            // 创建派生指标
            DerivedMetric metric = new DerivedMetric(
                form.getName(),
                form.getCategory(),
                form.getSubCategory(),
                form.getUnit(),
                form.getDescription(),
                form.getFormula(),
                dependencies,
                updateStrategy,
                form.getCalculationInterval()
            );

            // 更新指标
            metricConfigService.updateDerivedMetric(form.getIdentifier(), metric);
            
            return "redirect:/admin/metrics/derived?success=派生指标更新成功";
        } catch (Exception e) {
            return "redirect:/admin/metrics/derived/edit/" + form.getIdentifier() + "?error=" + e.getMessage();
        }
    }

    /**
     * 删除指标
     */
    @PostMapping("/delete/{identifier}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMetric(@PathVariable String identifier) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = metricConfigService.removeMetric(identifier);
            if (deleted) {
                response.put("success", true);
                response.put("message", "指标删除成功");
            } else {
                response.put("success", false);
                response.put("message", "指标不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 获取指标详情API
     */
    @GetMapping("/api/{identifier}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMetricDetails(@PathVariable String identifier) {
        Map<String, Object> response = new HashMap<>();
        Metric metric = metricConfigService.getMetric(identifier);
        
        if (metric != null) {
            response.put("success", true);
            response.put("metric", metric);
        } else {
            response.put("success", false);
            response.put("message", "指标不存在");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取指标详情API（支持查询参数形式，避免路径变量中包含点/中文导致的问题）
     */
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMetricDetailsByParam(@RequestParam("identifier") String identifier) {
        Map<String, Object> response = new HashMap<>();
        Metric metric = metricConfigService.getMetric(identifier);

        if (metric != null) {
            response.put("success", true);
            response.put("metric", metric);
        } else {
            response.put("success", false);
            response.put("message", "指标不存在");
        }

        return ResponseEntity.ok(response);
    }

    // ========== 表单类定义 ==========

    /**
     * 基础指标表单类
     */
    public static class BasicMetricForm {
        private String identifier;
        private String name;
        private String category;
        private String subCategory;
        private String unit;
        private String description;
        private String sourceType;
        private String sourceAddress;
        private String sourceName;
        private int refreshInterval = 300;
        private boolean enabled = true;

        // Getters and Setters
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
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
    }

    /**
     * 派生指标表单类
     */
    public static class DerivedMetricForm {
        private String identifier;
        private String name;
        private String category;
        private String subCategory;
        private String unit;
        private String description;
        private String formula;
        private List<String> dependencies;
        private String updateStrategy = "REALTIME";
        private Integer calculationInterval = 300;

        // Getters and Setters
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
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
        public List<String> getDependencies() { return dependencies; }
        public void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }
        public String getUpdateStrategy() { return updateStrategy; }
        public void setUpdateStrategy(String updateStrategy) { this.updateStrategy = updateStrategy; }
        public Integer getCalculationInterval() { return calculationInterval; }
        public void setCalculationInterval(Integer calculationInterval) { this.calculationInterval = calculationInterval; }
    }
}
