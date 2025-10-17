# DataSource 重构设计

## 问题分析

原有的 `DataSource` 类使用单一的 `sourceAddress` 字符串来存储所有类型数据源的地址信息，这种方式存在以下问题：

### 1. HTTP API 数据源
- **问题**：仅存储 URL 不够，还需要 HTTP 方法、请求头、认证信息等
- **需求**：支持 GET/POST/PUT/DELETE、自定义请求头、Basic Auth/Token 认证

### 2. MQTT 数据源
- **问题**：仅存储 Topic 不够，还需要 Broker 地址、端口、QoS 等
- **需求**：支持 Broker 连接、端口配置、QoS 级别、认证信息

### 3. DATABASE 数据源
- **问题**：仅存储连接字符串不够，还需要 SQL 查询语句、用户名密码等
- **需求**：支持连接字符串、用户名密码、SQL 查询、数据库驱动

### 4. FILE 数据源
- **问题**：仅存储文件路径不够，还需要编码、格式、分隔符等
- **需求**：支持文件路径、编码格式、文件格式（CSV/JSON/XML）、分隔符

## 解决方案

### 核心设计
使用 `Map<String, Object> config` 来存储不同类型数据源的配置参数，通过工厂方法创建特定类型的数据源。

### 1. HTTP API 数据源配置
```java
DataSource httpSource = DataSource.createHttpApi(
    "https://api.example.com/metrics",  // URL
    "GET",                             // HTTP 方法
    Map.of("Authorization", "Bearer token"), // 请求头
    "API数据源",                        // 名称
    "从HTTP API获取指标数据",           // 描述
    300                                // 刷新间隔
);
```

**配置参数**：
- `url`: API 地址
- `method`: HTTP 方法 (GET/POST/PUT/DELETE)
- `headers`: 请求头 Map
- `timeout`: 请求超时时间
- `auth`: 认证信息

### 2. MQTT 数据源配置
```java
DataSource mqttSource = DataSource.createMqtt(
    "mqtt://broker.example.com",       // Broker 地址
    1883,                             // 端口
    "metrics/power",                  // Topic
    1,                                // QoS
    "MQTT数据源",                      // 名称
    "从MQTT Broker订阅指标数据",        // 描述
    60                                // 刷新间隔
);
```

**配置参数**：
- `broker`: MQTT Broker 地址
- `port`: Broker 端口
- `topic`: MQTT 主题
- `qos`: QoS 级别 (0/1/2)
- `clientId`: 客户端 ID
- `username`: 用户名
- `password`: 密码

### 3. DATABASE 数据源配置
```java
DataSource dbSource = DataSource.createDatabase(
    "jdbc:mysql://localhost:3306/metrics", // 连接字符串
    "username",                           // 用户名
    "password",                           // 密码
    "SELECT value FROM metrics WHERE id = ?", // SQL 查询
    "com.mysql.cj.jdbc.Driver",          // 驱动
    "数据库数据源",                        // 名称
    "从数据库查询指标数据",                // 描述
    600                                  // 刷新间隔
);
```

**配置参数**：
- `connectionString`: 数据库连接字符串
- `username`: 用户名
- `password`: 密码
- `query`: SQL 查询语句
- `driver`: 数据库驱动类名
- `poolSize`: 连接池大小
- `timeout`: 查询超时时间

### 4. FILE 数据源配置
```java
DataSource fileSource = DataSource.createFile(
    "/data/metrics.csv",               // 文件路径
    "UTF-8",                          // 编码
    "CSV",                            // 格式
    ",",                              // 分隔符
    "文件数据源",                       // 名称
    "从CSV文件读取指标数据",            // 描述
    300                               // 刷新间隔
);
```

**配置参数**：
- `filePath`: 文件路径
- `encoding`: 文件编码
- `format`: 文件格式 (CSV/JSON/XML)
- `delimiter`: 分隔符（CSV格式）
- `skipLines`: 跳过的行数
- `columnMapping`: 列映射

## 兼容性设计

为了保持向后兼容，保留了 `getSourceAddress()` 和 `setSourceAddress()` 方法：

- `getSourceAddress()`: 根据数据源类型返回相应的地址信息
- `setSourceAddress()`: 根据数据源类型设置相应的地址信息

## 使用示例

### 创建不同类型的数据源
```java
// HTTP API 数据源
DataSource httpSource = DataSource.createHttpApi(
    "https://api.example.com/metrics",
    "GET",
    Map.of("Authorization", "Bearer token"),
    "API数据源",
    "从HTTP API获取指标数据",
    300
);

// MQTT 数据源
DataSource mqttSource = DataSource.createMqtt(
    "mqtt://broker.example.com",
    1883,
    "metrics/power",
    1,
    "MQTT数据源",
    "从MQTT Broker订阅指标数据",
    60
);

// 数据库数据源
DataSource dbSource = DataSource.createDatabase(
    "jdbc:mysql://localhost:3306/metrics",
    "username",
    "password",
    "SELECT value FROM metrics WHERE id = ?",
    "com.mysql.cj.jdbc.Driver",
    "数据库数据源",
    "从数据库查询指标数据",
    600
);

// 文件数据源
DataSource fileSource = DataSource.createFile(
    "/data/metrics.csv",
    "UTF-8",
    "CSV",
    ",",
    "文件数据源",
    "从CSV文件读取指标数据",
    300
);
```

### 访问配置参数
```java
// 获取 HTTP URL
String url = httpSource.getConfig("url", String.class);

// 获取 MQTT Broker 地址
String broker = mqttSource.getConfig("broker", String.class);

// 获取数据库查询语句
String query = dbSource.getConfig("query", String.class);

// 获取文件路径
String filePath = fileSource.getConfig("filePath", String.class);
```

## 优势

1. **灵活性**：支持各种数据源类型的复杂配置
2. **扩展性**：易于添加新的数据源类型和配置参数
3. **类型安全**：通过泛型方法提供类型安全的配置访问
4. **向后兼容**：保持现有 API 的兼容性
5. **可维护性**：清晰的工厂方法和配置管理

## 后续工作

1. 更新 `DataSourceService` 以支持新的配置结构
2. 更新 YAML 配置文件格式
3. 更新 Web UI 表单以支持新的配置字段
4. 添加数据源配置验证逻辑
5. 实现数据源连接测试功能
