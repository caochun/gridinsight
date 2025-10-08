package com.gridinsight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * GridInsight 电力行业数字化管控指标系统主应用
 */
@SpringBootApplication
public class GridInsightApplication {

    public static void main(String[] args) {
        SpringApplication.run(GridInsightApplication.class, args);
        System.out.println("=== GridInsight 电力行业数字化管控指标系统启动成功 ===");
        System.out.println("服务地址: http://localhost:8080");
        System.out.println("API文档: http://localhost:8080/api/indicators/health");
    }
}
