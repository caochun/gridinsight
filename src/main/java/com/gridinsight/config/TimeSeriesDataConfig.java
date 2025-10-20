package com.gridinsight.config;

import com.gridinsight.service.TimeSeriesDataService;
import com.gridinsight.service.JsonTimeSeriesDataService;
import com.gridinsight.service.MapTsdbTimeSeriesDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 时序数据服务配置类
 * 根据配置自动选择使用JSON文件存储或MapTSDB存储
 */
@Configuration
public class TimeSeriesDataConfig {

    @Value("${gridinsight.timeseries.type:json}")
    private String timeseriesType;

    /**
     * 配置时序数据服务
     * 根据gridinsight.timeseries.type配置选择实现
     */
    @Bean
    @Primary
    public TimeSeriesDataService timeSeriesDataService() {
        if ("maptsdb".equalsIgnoreCase(timeseriesType)) {
            return new MapTsdbTimeSeriesDataService();
        } else {
            return new JsonTimeSeriesDataService();
        }
    }

    /**
     * MapTSDB时序数据服务（条件化Bean）
     */
    @Bean
    @ConditionalOnProperty(name = "gridinsight.timeseries.type", havingValue = "maptsdb")
    public MapTsdbTimeSeriesDataService mapTsdbTimeSeriesDataService() {
        return new MapTsdbTimeSeriesDataService();
    }

    /**
     * JSON文件时序数据服务（条件化Bean）
     */
    @Bean
    @ConditionalOnProperty(name = "gridinsight.timeseries.type", havingValue = "json")
    public JsonTimeSeriesDataService jsonTimeSeriesDataService() {
        return new JsonTimeSeriesDataService();
    }
}
