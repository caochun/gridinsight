package com.gridinsight.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;

/**
 * 基础指标类测试
 */
public class IndicatorTest {
    
    private Indicator indicator;
    
    @BeforeEach
    void setUp() {
        indicator = new Indicator(
            "发电量",
            "发电指标",
            "发电量统计",
            IndicatorType.绝对值,
            "万千瓦时",
            "mqtt://grid-monitor.com/power/generation"
        );
    }
    
    @Test
    void testIndicatorCreation() {
        assertThat(indicator.getName()).isEqualTo("发电量");
        assertThat(indicator.getCategory()).isEqualTo("发电指标");
        assertThat(indicator.getSubCategory()).isEqualTo("发电量统计");
        assertThat(indicator.getIndicatorType()).isEqualTo(IndicatorType.绝对值);
        assertThat(indicator.getUnit()).isEqualTo("万千瓦时");
        assertThat(indicator.getDataSource()).isEqualTo("mqtt://grid-monitor.com/power/generation");
    }
    
    @Test
    void testGetFullIdentifier() {
        String identifier = indicator.getFullIdentifier();
        assertThat(identifier).isEqualTo("发电指标.发电量统计.发电量");
    }
    
    @Test
    void testIsDerived() {
        assertThat(indicator.isDerived()).isFalse();
        
        // 测试派生指标类型
        indicator.setIndicatorType(IndicatorType.派生指标);
        assertThat(indicator.isDerived()).isTrue();
    }
    
    @Test
    void testEqualsAndHashCode() {
        Indicator sameIndicator = new Indicator(
            "发电量",
            "发电指标", 
            "发电量统计",
            IndicatorType.绝对值,
            "万千瓦时",
            "mqtt://grid-monitor.com/power/generation"
        );
        
        assertThat(indicator).isEqualTo(sameIndicator);
        assertThat(indicator.hashCode()).isEqualTo(sameIndicator.hashCode());
    }
    
    @Test
    void testToString() {
        String toString = indicator.toString();
        assertThat(toString).contains("发电量");
        assertThat(toString).contains("发电指标");
        assertThat(toString).contains("绝对值");
    }
}
