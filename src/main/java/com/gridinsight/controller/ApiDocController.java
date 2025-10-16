package com.gridinsight.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ApiDocController {

    @GetMapping("/api/metrics")
    public String apiDocs() {
        return "api-doc"; // 渲染 templates/api-doc.html
    }
}


