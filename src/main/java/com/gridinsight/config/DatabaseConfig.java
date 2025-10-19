package com.gridinsight.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 数据库配置类
 * 负责配置SQLite数据源和数据库初始化
 */
@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    /**
     * 配置数据源
     */
    @Bean
    public DataSource dataSource() {
        // 确保数据库目录存在
        ensureDatabaseDirectory();
        
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(databaseUrl);
        
        return dataSource;
    }

    /**
     * 配置JdbcTemplate
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 确保数据库目录存在
     */
    private void ensureDatabaseDirectory() {
        try {
            // 从URL中提取数据库文件路径
            String dbPath = databaseUrl.replace("jdbc:sqlite:", "");
            File dbFile = new File(dbPath);
            File dbDir = dbFile.getParentFile();
            
            if (dbDir != null && !dbDir.exists()) {
                Files.createDirectories(Paths.get(dbDir.getAbsolutePath()));
                System.out.println("创建数据库目录: " + dbDir.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("创建数据库目录失败: " + e.getMessage());
        }
    }
}
