package com.gridinsight.util;

/**
 * 统一异常处理工具类
 */
public class ExceptionHandler {
    
    /**
     * 静默处理异常，不打印堆栈跟踪
     * @param e 异常
     * @param context 上下文信息
     */
    public static void handleSilently(Exception e, String context) {
        // 在生产环境中，这里应该使用日志框架记录异常
        // 当前为了简化，只做静默处理
    }
    
    /**
     * 处理异常并返回默认值
     * @param e 异常
     * @param defaultValue 默认值
     * @param <T> 返回值类型
     * @return 默认值
     */
    public static <T> T handleWithDefault(Exception e, T defaultValue) {
        handleSilently(e, "操作失败，使用默认值");
        return defaultValue;
    }
}
