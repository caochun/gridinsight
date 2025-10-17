package com.gridinsight.domain.model;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

/**
 * 数据源配置类
 * 定义指标数据来源的配置信息，支持多种数据源类型
 */
public class DataSource {
    
    /**
     * 数据源类型
     */
    public enum SourceType {
        HTTP_API("HTTP API"),
        MQTT("MQTT"),
        DATABASE("数据库"),
        FILE("文件");
        
        private final String displayName;
        
        SourceType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    /**
     * 数据源类型
     */
    private SourceType sourceType;
    
    /**
     * 数据源名称
     */
    private String sourceName;
    
    /**
     * 数据源描述
     */
    private String description;
    
    /**
     * 刷新频率（秒）
     */
    private Integer refreshInterval;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 数据源配置参数
     * 根据不同的数据源类型，包含不同的配置项
     */
    private Map<String, Object> config = new HashMap<>();
    
    /**
     * 默认构造函数
     */
    public DataSource() {
        this.enabled = true;
    }
    
    /**
     * 构造函数
     * @param sourceType 数据源类型
     * @param sourceName 数据源名称
     * @param description 数据源描述
     * @param refreshInterval 刷新频率（秒）
     * @param enabled 是否启用
     */
    public DataSource(SourceType sourceType, String sourceName, String description, 
                      Integer refreshInterval, Boolean enabled) {
        this();
        this.sourceType = sourceType;
        this.sourceName = sourceName;
        this.description = description;
        this.refreshInterval = refreshInterval;
        this.enabled = enabled;
    }
    
    /**
     * 创建HTTP API数据源
     * @param url API地址
     * @param method HTTP方法 (GET/POST/PUT/DELETE)
     * @param headers 请求头
     * @param sourceName 数据源名称
     * @param description 数据源描述
     * @param refreshInterval 刷新频率（秒）
     * @return HTTP API数据源
     */
    public static DataSource createHttpApi(String url, String method, Map<String, String> headers,
                                          String sourceName, String description, Integer refreshInterval) {
        DataSource dataSource = new DataSource(SourceType.HTTP_API, sourceName, description, refreshInterval, true);
        dataSource.setConfig("url", url);
        dataSource.setConfig("method", method != null ? method : "GET");
        if (headers != null) {
            dataSource.setConfig("headers", headers);
        }
        return dataSource;
    }
    
    /**
     * 创建MQTT数据源
     * @param broker MQTT Broker地址
     * @param port Broker端口
     * @param topic MQTT主题
     * @param qos QoS级别
     * @param sourceName 数据源名称
     * @param description 数据源描述
     * @param refreshInterval 刷新频率（秒）
     * @return MQTT数据源
     */
    public static DataSource createMqtt(String broker, Integer port, String topic, Integer qos,
                                       String sourceName, String description, Integer refreshInterval) {
        DataSource dataSource = new DataSource(SourceType.MQTT, sourceName, description, refreshInterval, true);
        dataSource.setConfig("broker", broker);
        dataSource.setConfig("port", port != null ? port : 1883);
        dataSource.setConfig("topic", topic);
        dataSource.setConfig("qos", qos != null ? qos : 0);
        return dataSource;
    }
    
    /**
     * 创建数据库数据源
     * @param connectionString 数据库连接字符串
     * @param username 用户名
     * @param password 密码
     * @param query SQL查询语句
     * @param driver 数据库驱动
     * @param sourceName 数据源名称
     * @param description 数据源描述
     * @param refreshInterval 刷新频率（秒）
     * @return 数据库数据源
     */
    public static DataSource createDatabase(String connectionString, String username, String password,
                                           String query, String driver, String sourceName, 
                                           String description, Integer refreshInterval) {
        DataSource dataSource = new DataSource(SourceType.DATABASE, sourceName, description, refreshInterval, true);
        dataSource.setConfig("connectionString", connectionString);
        dataSource.setConfig("username", username);
        dataSource.setConfig("password", password);
        dataSource.setConfig("query", query);
        dataSource.setConfig("driver", driver);
        return dataSource;
    }
    
    /**
     * 创建文件数据源
     * @param filePath 文件路径
     * @param encoding 文件编码
     * @param format 文件格式 (CSV/JSON/XML)
     * @param delimiter 分隔符（CSV格式）
     * @param sourceName 数据源名称
     * @param description 数据源描述
     * @param refreshInterval 刷新频率（秒）
     * @return 文件数据源
     */
    public static DataSource createFile(String filePath, String encoding, String format, String delimiter,
                                       String sourceName, String description, Integer refreshInterval) {
        DataSource dataSource = new DataSource(SourceType.FILE, sourceName, description, refreshInterval, true);
        dataSource.setConfig("filePath", filePath);
        dataSource.setConfig("encoding", encoding != null ? encoding : "UTF-8");
        dataSource.setConfig("format", format != null ? format : "CSV");
        if (delimiter != null) {
            dataSource.setConfig("delimiter", delimiter);
        }
        return dataSource;
    }
    
    /**
     * 设置配置参数
     * @param key 配置键
     * @param value 配置值
     */
    public void setConfig(String key, Object value) {
        this.config.put(key, value);
    }
    
    /**
     * 获取配置参数
     * @param key 配置键
     * @return 配置值
     */
    public Object getConfig(String key) {
        return this.config.get(key);
    }
    
    /**
     * 获取配置参数（指定类型）
     * @param key 配置键
     * @param type 期望的类型
     * @return 配置值
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key, Class<T> type) {
        Object value = this.config.get(key);
        if (value == null) {
            return null;
        }
        
        // 直接类型匹配
        if (type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        
        // 尝试字符串转换
        if (type == String.class && value instanceof String) {
            return (T) value;
        }
        
        // 尝试数字转换
        if (type == Integer.class) {
            if (value instanceof Integer) {
                return (T) value;
            } else if (value instanceof Number) {
                return (T) Integer.valueOf(((Number) value).intValue());
            } else if (value instanceof String) {
                try {
                    return (T) Integer.valueOf((String) value);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 获取所有配置
     * @return 配置Map
     */
    public Map<String, Object> getConfig() {
        return new HashMap<>(this.config);
    }
    
    /**
     * 设置所有配置
     * @param config 配置Map
     */
    public void setConfig(Map<String, Object> config) {
        this.config = config != null ? new HashMap<>(config) : new HashMap<>();
    }
    
    // Getter 和 Setter 方法
    
    public SourceType getSourceType() {
        return sourceType;
    }
    
    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }
    
    public String getSourceName() {
        return sourceName;
    }
    
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getRefreshInterval() {
        return refreshInterval;
    }
    
    public void setRefreshInterval(Integer refreshInterval) {
        this.refreshInterval = refreshInterval;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 获取数据源地址（兼容性方法）
     * 根据数据源类型返回相应的地址信息
     * @return 数据源地址
     */
    public String getSourceAddress() {
        if (sourceType == null) {
            return null;
        }
        
        switch (sourceType) {
            case HTTP_API:
                return getConfig("url", String.class);
            case MQTT:
                String broker = getConfig("broker", String.class);
                Integer port = getConfig("port", Integer.class);
                return broker + ":" + (port != null ? port : 1883);
            case DATABASE:
                return getConfig("connectionString", String.class);
            case FILE:
                return getConfig("filePath", String.class);
            default:
                return null;
        }
    }
    
    /**
     * 设置数据源地址（兼容性方法）
     * 根据数据源类型设置相应的地址信息
     * @param sourceAddress 数据源地址
     */
    public void setSourceAddress(String sourceAddress) {
        if (sourceType == null || sourceAddress == null) {
            return;
        }
        
        switch (sourceType) {
            case HTTP_API:
                setConfig("url", sourceAddress);
                break;
            case MQTT:
                // MQTT地址格式：broker:port
                if (sourceAddress.contains(":")) {
                    String[] parts = sourceAddress.split(":", 2);
                    setConfig("broker", parts[0]);
                    try {
                        setConfig("port", Integer.parseInt(parts[1]));
                    } catch (NumberFormatException e) {
                        setConfig("port", 1883);
                    }
                } else {
                    setConfig("broker", sourceAddress);
                    setConfig("port", 1883);
                }
                break;
            case DATABASE:
                setConfig("connectionString", sourceAddress);
                break;
            case FILE:
                setConfig("filePath", sourceAddress);
                break;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataSource that = (DataSource) o;
        return sourceType == that.sourceType &&
               Objects.equals(sourceName, that.sourceName) &&
               Objects.equals(config, that.config);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sourceType, sourceName, config);
    }
    
    @Override
    public String toString() {
        return "DataSource{" +
               "sourceType=" + sourceType +
               ", sourceName='" + sourceName + '\'' +
               ", description='" + description + '\'' +
               ", refreshInterval=" + refreshInterval +
               ", enabled=" + enabled +
               ", config=" + config +
               '}';
    }
}
