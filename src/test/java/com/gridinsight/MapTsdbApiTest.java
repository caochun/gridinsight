package com.gridinsight;

import com.maptsdb.TimeSeriesDatabase;
import com.maptsdb.TimeSeriesDatabaseBuilder;

/**
 * MapTSDB API测试类
 * 用于了解MapTSDB的实际API结构
 */
public class MapTsdbApiTest {
    
    public static void main(String[] args) {
        try {
            // 测试MapTSDB的基本API
            System.out.println("测试MapTSDB API...");
            
            // 尝试创建TimeSeriesDatabaseBuilder
            TimeSeriesDatabaseBuilder builder = TimeSeriesDatabaseBuilder.builder();
            System.out.println("TimeSeriesDatabaseBuilder创建成功");
            
            // 尝试创建TimeSeriesDatabase
            TimeSeriesDatabase db = builder.build();
            System.out.println("TimeSeriesDatabase创建成功");
            
            // 测试基本操作
            long timestamp = System.currentTimeMillis();
            db.putDouble("test-metric", timestamp, 123.45);
            System.out.println("数据写入成功");
            
            // 测试读取
            Double value = db.getDouble("test-metric", timestamp);
            System.out.println("读取到的值: " + value);
            
            // 测试基本操作
            System.out.println("MapTSDB API测试完成");
            
        } catch (Exception e) {
            System.err.println("MapTSDB API测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
