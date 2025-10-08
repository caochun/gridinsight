package com.gridinsight.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 模拟数据源服务
 * 模拟从各种数据源获取数据
 */
@Service
public class MockDataSourceService {

    private final Random random = new Random();
    private final Map<String, Double> mockDataCache = new HashMap<>();

    /**
     * 根据数据源URL获取模拟数据
     */
    public Double getDataFromSource(String dataSource) {
        if (dataSource == null || dataSource.isEmpty()) {
            return generateRandomValue(0, 100);
        }

        // 检查缓存
        if (mockDataCache.containsKey(dataSource)) {
            return mockDataCache.get(dataSource);
        }

        // 根据数据源类型生成不同的模拟数据
        Double value = generateMockDataByDataSource(dataSource);
        
        // 缓存数据（模拟数据在一定时间内保持稳定）
        mockDataCache.put(dataSource, value);
        
        return value;
    }

    /**
     * 根据数据源URL生成相应的模拟数据
     */
    private Double generateMockDataByDataSource(String dataSource) {
        if (dataSource.contains("inconsistent-count")) {
            // 拓扑不一致数量：0-50个
            return generateRandomValue(0, 50);
        } else if (dataSource.contains("transformer-total")) {
            // 配变总数：1000-5000个
            return generateRandomValue(1000, 5000);
        } else if (dataSource.contains("incorrect-count")) {
            // 变户关系错误数量：0-200户
            return generateRandomValue(0, 200);
        } else if (dataSource.contains("total-count")) {
            // 低压用户总数：10000-100000户
            return generateRandomValue(10000, 100000);
        } else if (dataSource.contains("normal-count")) {
            // 分路档案正常数量：5000-50000户
            return generateRandomValue(5000, 50000);
        } else if (dataSource.contains("phase-identified")) {
            // 相别识别数量：3000-30000户
            return generateRandomValue(3000, 30000);
        } else if (dataSource.contains("phase-total")) {
            // 相别总数：8000-80000户
            return generateRandomValue(8000, 80000);
        } else if (dataSource.contains("branch-normal")) {
            // 分路档案正常数量：4000-40000户
            return generateRandomValue(4000, 40000);
        } else if (dataSource.contains("branch-total")) {
            // 分路总数：6000-60000户
            return generateRandomValue(6000, 60000);
        } else if (dataSource.contains("phase-relationship-normal")) {
            // 相别关系正常数量：3500-35000户
            return generateRandomValue(3500, 35000);
        } else if (dataSource.contains("phase-relationship-total")) {
            // 相别关系总数：7000-70000户
            return generateRandomValue(7000, 70000);
        } else if (dataSource.contains("customer-relationship-normal")) {
            // 用户关系正常数量：4500-45000户
            return generateRandomValue(4500, 45000);
        } else if (dataSource.contains("customer-relationship-total")) {
            // 用户关系总数：9000-90000户
            return generateRandomValue(9000, 90000);
        } else if (dataSource.contains("data-quality-normal")) {
            // 数据质量正常数量：5000-50000户
            return generateRandomValue(5000, 50000);
        } else if (dataSource.contains("data-quality-total")) {
            // 数据质量总数：10000-100000户
            return generateRandomValue(10000, 100000);
        } else {
            // 默认生成0-1000的随机值
            return generateRandomValue(0, 1000);
        }
    }

    /**
     * 生成指定范围内的随机值
     */
    private Double generateRandomValue(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    /**
     * 清除缓存（用于测试）
     */
    public void clearCache() {
        mockDataCache.clear();
    }

    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        return mockDataCache.size();
    }
}
