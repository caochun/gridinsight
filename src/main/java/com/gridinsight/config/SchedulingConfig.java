package com.gridinsight.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 调度配置类
 * 启用Spring的异步执行和定时任务功能
 */
@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfig {
    // 配置类，启用调度功能
}
