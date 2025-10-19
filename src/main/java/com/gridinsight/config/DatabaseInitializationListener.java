package com.gridinsight.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


/**
 * 数据库初始化监听器
 * 在应用启动完成后验证数据库初始化状态
 */
@Component
public class DatabaseInitializationListener {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            // 验证数据库表是否创建成功
            verifyDatabaseInitialization();
            System.out.println("✅ 数据库初始化验证完成");
        } catch (Exception e) {
            System.err.println("❌ 数据库初始化验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证数据库初始化状态
     */
    private void verifyDatabaseInitialization() {
        try {
            // 检查表是否存在
            String[] tables = {"transformers", "users", "historical_load"};
            
            for (String table : tables) {
                String sql = "SELECT COUNT(*) FROM " + table;
                Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
                System.out.println("📊 表 " + table + " 记录数: " + count);
            }
            
            // 显示数据统计
            showDataStatistics();
            
        } catch (Exception e) {
            System.err.println("数据库验证失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 显示数据统计信息
     */
    private void showDataStatistics() {
        try {
            // 配变统计
            String transformerSql = "SELECT COUNT(*) FROM transformers WHERE status = 'active'";
            Integer activeTransformers = jdbcTemplate.queryForObject(transformerSql, Integer.class);
            System.out.println("🏭 活跃配变数量: " + activeTransformers);
            
            // 用户统计
            String userSql = "SELECT COUNT(*) FROM users WHERE active = 1";
            Integer activeUsers = jdbcTemplate.queryForObject(userSql, Integer.class);
            System.out.println("👥 活跃用户数量: " + activeUsers);
            
            // 历史负荷统计
            String loadSql = "SELECT ROUND(AVG(load_value), 2) FROM historical_load WHERE date >= datetime('now', '-1 day')";
            Double avgLoad = jdbcTemplate.queryForObject(loadSql, Double.class);
            System.out.println("⚡ 近24小时平均负荷: " + avgLoad + " MW");
            
        } catch (Exception e) {
            System.err.println("获取数据统计失败: " + e.getMessage());
        }
    }
}
