# GridInsight 指标平台（Metric-based）

面向电力行业的指标（Metric）管理与计算平台。支持基础指标从外部数据源采集，派生指标通过公式计算生成，采用事件驱动更新机制，内置 Web 管理界面与 REST API，并集成基于JSON文件的高性能本地时序数据存储。

- Java 11+
- Spring Boot 2.7.18
- 端口：9000（server.port=9000）
- 时序存储：基于JSON文件的高性能本地存储

## 核心概念

### Metric（指标）
- **定义**：电力系统中可观测、可测量的业务指标，如电压、电流、功率、故障率等
- **唯一标识**：采用"分类.子分类.名称"三级层次结构，如"中压拓扑.配变统计.配变总数"
- **UUID标识**：每个指标都有唯一的UUID，用于API查询和URL安全编码
- **基本属性**：包含名称、分类、子分类、单位、描述等元数据
- **数据质量**：每个指标值都带有质量标识（GOOD/STALE/ERROR），用于数据可信度评估

### BasicMetric（基础指标）
- **定义**：直接从外部数据源采集的原始指标，是派生指标计算的基础
- **数据源**：支持多种采集方式
  - HTTP_API：通过REST API获取数据
  - MQTT：订阅消息队列主题
  - DATABASE：从关系型数据库查询（支持SQLite等）
  - FILE：从文件系统读取
- **更新机制**：
  - HTTP_API/DATABASE/FILE：根据 `refreshInterval`（刷新间隔）定时从数据源拉取最新值
  - MQTT：根据 `samplingInterval`（采样间隔）对订阅数据进行采样存储
- **存储**：采集到的值存储到JSON文件时序数据库中，支持历史查询

### DerivedMetric（派生指标）
- **定义**：通过数学公式计算得出的指标，依赖一个或多个基础指标或其他派生指标
- **计算公式**：支持复杂数学表达式，包括：
  - 基本运算：+、-、*、/
  - 数学函数：sqrt()、abs()、max()、min()
  - 括号优先级：()
  - 指标引用：通过标识符引用其他指标值
- **依赖关系**：自动解析公式中的指标依赖，构建依赖图
- **更新策略**：
  - **事件驱动**：当依赖的基础指标值发生变化时，自动触发派生指标重新计算并存储
  - **实时计算**：每次查询时实时计算，适合计算成本低的指标
- **循环依赖检测**：系统自动检测并阻止循环依赖，确保计算逻辑的正确性

### MetricValue（指标值）
- **结构**：包含指标标识符、数值、单位、时间戳、数据质量等信息
- **时间戳**：记录指标值的采集或计算时间
- **数据质量**：标识数据的可信度状态
- **单位**：保持与指标定义的一致性

### DataSource（数据源）
- **配置**：定义外部数据源的连接参数和采集策略
- **类型**：支持HTTP、MQTT、数据库、文件等多种数据源
- **配置方式**：使用Map<String, Object>存储灵活配置，支持连接字符串、查询语句等
- **启用状态**：可动态启用/禁用数据源，便于运维管理

## 架构与模块

- domain/model：`Metric`、`BasicMetric`、`DerivedMetric`、`MetricValue` 等
- domain/service：
  - `MetricCalculationService`：计算中枢；管理注册表；分派基础/派生计算；结果缓存（5 分钟有效）；依赖图与统计
  - `FormulaEngine` / `FormulaParser`：公式解析与求值（支持中文标识符、()、+-*/、sqrt/abs/max/min）
- service：
  - `MetricConfigService`：启动加载 `resources/metrics/*.yaml` 并同步注册到计算服务
  - `DataSourceService`：模拟 HTTP/MQTT/DB/FILE 取数
  - `MetricSchedulerService`：定时任务（基础：`refreshInterval`；派生仅 SCHEDULED）
  - `EventDrivenMetricUpdateService` / `MetricUpdateEventListener`：事件驱动依赖更新（发布/监听 `MetricUpdateEvent`）
  - `TimeSeriesDataService`：时序占位实现（内存），可替换 InfluxDB 等
- controller：
  - `MetricController`：REST API（查询/列表/批量/健康/清缓存）
  - `MetricManagementController`：Web UI（定义管理、查询定义 API）
  - `ApiDocController`：API 文档页（`/api/metrics`）

## 启动与访问

```bash
mvn clean compile
mvn spring-boot:run
```

- 管理入口：`http://localhost:9000/admin/metrics`
  - 基础指标：`/admin/metrics/basic`
  - 派生指标：`/admin/metrics/derived`
- API 文档页：`http://localhost:9000/api/metrics`（在线试调；友好展示"实时值/指标定义"）

## 指标配置（YAML）

- 基础：`src/main/resources/metrics/basic-metrics.yaml`
  - key 为完整标识符；`dataSource` 包含 `sourceType/sourceAddress/sourceName/refreshInterval/enabled`
- 派生：`src/main/resources/metrics/derived-metrics.yaml`
  - `formula` 引用其他指标标识符
  - `dependencies` 依赖列表
  - `updateStrategy`：`REALTIME/DEPENDENCY_DRIVEN/SCHEDULED`
  - `calculationInterval`：仅 SCHEDULED 生效

启动时由 `MetricConfigService` 自动加载并注册到 `MetricCalculationService`；控制台打印加载数量。

## 事件驱动依赖更新

1) 基础指标按 `refreshInterval` 定时更新，写入JSON文件时序存储并发布 `MetricValueChangedEvent`  
2) 监听器 `MetricValueChangeListener` 查找依赖此指标的派生指标  
3) 对所有派生指标触发异步计算（含冷却期防抖）  
4) 派生指标计算结果存储到JSON文件时序存储中
循环依赖由 `FormulaParser` 定义/解析阶段校验。

## REST API

- 健康：GET `/api/metrics/health`
- 列表：GET `/api/metrics/list`
- 单查：GET `/api/metrics/query?identifier=分类.子分类.名称`
- UUID查询：GET `/api/metrics/query-by-uuid?uuid=指标UUID`
- 批量：POST `/api/metrics/batch-query`（Body: `[]` 标识符数组）
- 清缓存：POST `/api/metrics/clear-cache`

错误响应：`{"success": false, "error": "..."}`  
成功实时值包含：`identifier/value/unit/timestamp/quality`

## Web UI

- 管理页：查看/新增/编辑/删除基础与派生指标
- API 文档页：
  - 实时值：表格友好展示（标识符、数值+单位、时间、质量）
  - 历史值：Chart.js图表展示历史数据趋势
  - 指标定义：表格友好展示（名称、分类/子分类、单位、类型、依赖、公式、描述）
  - 指标列表：弹出对话框显示所有指标（包含UUID），支持点击链接快速查询

## 测试

```bash
mvn clean test
```

覆盖：
- 单元：公式引擎/解析、模型
- 集成：计算服务与数据源模拟
- 事件驱动：依赖触发、级联与多场景校验
- SCHEDULED 策略：间隔/注册与计算验证

## 时序数据库

**基于 JSON 文件的高性能本地时序存储**：
- `JsonTimeSeriesDataService` 实现高性能本地时序数据存储
- 支持指标值的持久化存储和历史查询
- 每个指标使用独立的 JSON 文件，支持并发读写
- 内置最新值缓存，提供毫秒级查询响应
- 支持时间范围查询和统计计算

**存储特性**：
- 轻量级：基于 JSON 文件存储，无外部依赖
- 高性能：支持高并发读写操作
- 持久化：数据存储在本地文件系统
- 易于调试：JSON 格式便于查看和调试
- 故障恢复：支持应用重启后数据恢复

**API接口**：
- 历史查询：`GET /api/timeseries/history?metric=xxx&start=xxx&end=xxx`
- 最新值：`GET /api/timeseries/latest?metric=xxx`
- 批量查询：`POST /api/timeseries/latest-batch`
- 统计信息：`GET /api/timeseries/statistics?metric=xxx&range=1h`
- 存储统计：`GET /api/timeseries/storage-stats`

## 配置与调试

- `src/main/resources/application.properties`
  - `server.port=9000`
  - `spring.thymeleaf.cache=false`（开发期禁用模板缓存）
  - `gridinsight.timeseries.data-path=data/timeseries`（时序数据存储路径）
  - `gridinsight.timeseries.type=json`（使用JSON文件存储）
  - `gridinsight.timeseries.enable-cache=true`（启用最新值缓存）

## 目录结构

```
src/main/java/com/gridinsight/
  ├─ domain/
  │   ├─ model/ (Metric, BasicMetric, DerivedMetric, MetricValue ...)
  │   └─ service/ (MetricCalculationService, FormulaEngine, FormulaParser)
  ├─ service/ (MetricConfigService, MetricSchedulerService, EventDriven*, DataSourceService, TimeSeriesDataService)
  ├─ controller/ (MetricController, MetricManagementController, ApiDocController)
  └─ util/ (ExceptionHandler)
src/main/resources/
  ├─ metrics/
  │   ├─ basic-metrics.yaml
  │   └─ derived-metrics.yaml
  └─ templates/ (Web UI 页面，含 api-doc.html)
data/
  └─ test_metrics.db (SQLite测试数据库)
```

## 已知限制与规划

- 公式函数有限（sqrt/abs/max/min），可扩展
- ~~时序库为内存占位~~ ✅ **已实现基于JSON文件的高性能本地存储**
- Web UI 规划：调度/事件监控、时序曲线可视化、定义导入导出、审计与权限
- 时序数据可视化：可集成Grafana等可视化工具

## 最新更新

- **UUID支持**：所有指标现在都有唯一UUID，支持安全的API查询和URL编码
- **事件驱动架构**：派生指标采用事件驱动机制，当依赖的基础指标变化时自动重新计算
- **JSON文件存储**：实现基于JSON文件的高性能本地时序数据存储，每个指标使用UUID作为文件名
- **时序数据API**：新增完整的时序数据查询和统计API接口，支持UUID查询
- **Web UI增强**：
  - API文档页新增历史值图表展示（Chart.js）
  - 指标列表弹出对话框，支持点击链接快速查询
  - 实时值/历史值双Tab展示
- **数据源优化**：
  - HTTP_API/DATABASE/FILE使用refreshInterval（刷新间隔）
  - MQTT使用samplingInterval（采样间隔）
- **代码清理**：移除所有调试输出，优化异常处理
- **架构简化**：移除缓存机制，直接使用时序数据库存储和查询

---
快速验证：
1) `http://localhost:9000/api/metrics/health`  
2) `http://localhost:9000/api/metrics/list`  
3) `http://localhost:9000/api/metrics`