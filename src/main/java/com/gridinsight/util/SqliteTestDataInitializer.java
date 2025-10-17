package com.gridinsight.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQLite测试数据库初始化工具
 */
public class SqliteTestDataInitializer {
    
    private static final String DB_PATH = "data/test_metrics.db";
    private static final String INIT_SQL_PATH = "data/init_test_db.sql";
    
    /**
     * 初始化SQLite测试数据库
     */
    public static void initializeTestDatabase() {
        try {
            // 加载SQLite JDBC驱动
            Class.forName("org.sqlite.JDBC");
            
            // 创建数据库连接
            String url = "jdbc:sqlite:" + DB_PATH;
            try (Connection conn = DriverManager.getConnection(url)) {
                System.out.println("=== SQLite测试数据库初始化开始 ===");
                
                // 读取并执行SQL脚本
                executeSqlScript(conn, INIT_SQL_PATH);
                
                System.out.println("=== SQLite测试数据库初始化完成 ===");
                System.out.println("数据库路径: " + DB_PATH);
                
                // 显示数据统计
                showDataStatistics(conn);
                
            } catch (SQLException e) {
                System.err.println("数据库操作失败: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC驱动未找到: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 执行SQL脚本文件
     */
    private static void executeSqlScript(Connection conn, String scriptPath) throws SQLException {
        try (BufferedReader reader = new BufferedReader(new FileReader(scriptPath))) {
            StringBuilder sql = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                // 跳过注释和空行
                if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                    continue;
                }
                
                sql.append(line).append("\n");
                
                // 如果遇到分号，执行SQL语句
                if (line.trim().endsWith(";")) {
                    String sqlStatement = sql.toString().trim();
                    if (!sqlStatement.isEmpty()) {
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute(sqlStatement);
                            System.out.println("执行SQL: " + sqlStatement.substring(0, Math.min(50, sqlStatement.length())) + "...");
                        }
                    }
                    sql = new StringBuilder();
                }
            }
            
        } catch (IOException e) {
            System.err.println("读取SQL脚本失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 显示数据统计信息
     */
    private static void showDataStatistics(Connection conn) throws SQLException {
        System.out.println("\n=== 数据统计 ===");
        
        String[] queries = {
            "SELECT COUNT(*) as count FROM transformers WHERE status = 'active'",
            "SELECT COUNT(*) as count FROM users WHERE active = 1",
            "SELECT ROUND(AVG(load_value), 2) as avg_load FROM historical_load WHERE date >= datetime('now', '-1 day')"
        };
        
        String[] labels = {"活跃配变数量", "活跃用户数量", "最近24小时平均负荷(MW)"};
        
        try (Statement stmt = conn.createStatement()) {
            for (int i = 0; i < queries.length; i++) {
                try (var rs = stmt.executeQuery(queries[i])) {
                    if (rs.next()) {
                        System.out.println(labels[i] + ": " + rs.getObject(1));
                    }
                }
            }
        }
    }
    
    /**
     * 主方法，用于独立运行数据库初始化
     */
    public static void main(String[] args) {
        initializeTestDatabase();
    }
}
