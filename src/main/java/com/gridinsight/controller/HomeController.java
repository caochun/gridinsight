package com.gridinsight.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 首页控制器
 */
@Controller
public class HomeController {

    /**
     * 根路径重定向到管理界面
     */
    @GetMapping("/")
    public String redirectToAdmin() {
        return "redirect:/admin/metrics";
    }
}
