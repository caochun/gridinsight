package com.gridinsight.domain.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * 指标类型枚举测试
 */
public class IndicatorTypeTest {
    
    @Test
    void testIndicatorTypeValues() {
        assertThat(IndicatorType.绝对值.getDisplayName()).isEqualTo("绝对值");
        assertThat(IndicatorType.比率.getDisplayName()).isEqualTo("比率");
        assertThat(IndicatorType.同比.getDisplayName()).isEqualTo("同比");
        assertThat(IndicatorType.环比.getDisplayName()).isEqualTo("环比");
        assertThat(IndicatorType.派生指标.getDisplayName()).isEqualTo("派生指标");
    }
    
    @Test
    void testToString() {
        assertThat(IndicatorType.绝对值.toString()).isEqualTo("绝对值");
        assertThat(IndicatorType.比率.toString()).isEqualTo("比率");
        assertThat(IndicatorType.同比.toString()).isEqualTo("同比");
        assertThat(IndicatorType.环比.toString()).isEqualTo("环比");
        assertThat(IndicatorType.派生指标.toString()).isEqualTo("派生指标");
    }
    
    @Test
    void testAllTypesExist() {
        IndicatorType[] types = IndicatorType.values();
        assertThat(types).hasSize(5);
        assertThat(types).containsExactly(
            IndicatorType.绝对值,
            IndicatorType.比率,
            IndicatorType.同比,
            IndicatorType.环比,
            IndicatorType.派生指标
        );
    }
}
