# GridInsight 指标平台

面向电力行业的指标管理与计算平台。支持基础指标从外部数据源采集，派生指标通过公式计算生成，采用事件驱动更新机制，内置 Web 管理界面与 REST API。

## 技术栈

- **Java 11+** | **Spring Boot 2.7.18** | **端口：9000**
- **时序存储**：MapTSDB高性能时序数据库 + JSON文件存储
- **数据库**：SQLite（支持外部配置）
- **前端**：Bootstrap + Chart.js

## 核心概念

### 📊 Metric（指标）
- **定义**：电力系统中可观测、可测量的业务指标
- **标识**：采用"分类.子分类.名称"三级层次结构 + UUID
- **质量**：每个指标值都带有质量标识（GOOD/STALE/ERROR）

### 🔗 BasicMetric（基础指标）
- **数据源**：HTTP_API、MQTT、DATABASE、FILE
- **更新机制**：定时刷新（refreshInterval）或采样存储（samplingInterval）
- **存储**：MapTSDB时序数据库（高性能）或JSON文件存储

### 🧮 DerivedMetric（派生指标）
- **计算公式**：支持 +、-、*、/、sqrt()、abs()、max()、min()
- **事件驱动**：依赖指标变化时自动重新计算
- **循环检测**：自动检测并阻止循环依赖

### 💾 MetricValue（指标值）
- **结构**：标识符、数值、单位、时间戳、数据质量
- **存储**：MapTSDB时序数据库（高性能）或JSON文件存储

## 🏗️ 架构与模块

### 核心服务
- **ExternalMetricConfigService**：外部配置文件管理
- **MetricCalculationService**：指标计算中枢
- **EventDrivenMetricUpdateService**：事件驱动更新
- **TimeSeriesDataService**：时序数据存储（支持MapTSDB和JSON）

### 控制器
- **MetricController**：REST API接口
- **MetricManagementController**：Web管理界面
- **TimeSeriesController**：时序数据API

## 🚀 快速开始

### 启动应用
```bash
# 使用外部配置启动
./start-with-external-config.sh

# 或直接启动
mvn spring-boot:run
```

### 访问地址
- **管理界面**：http://localhost:9000/admin/metrics
- **API文档**：http://localhost:9000/api/metrics
- **健康检查**：http://localhost:9000/api/metrics/health

## 📝 指标配置

### 外部配置文件
- **基础指标**：`config/metrics/basic-metrics.yaml`
- **派生指标**：`config/metrics/derived-metrics.yaml`
- **应用配置**：`config/application.properties`

### 配置特性
- ✅ **外部配置**：修改后无需重新编译
- ✅ **热加载**：支持动态配置更新
- ✅ **多数据源**：HTTP、MQTT、数据库、文件
- ✅ **存储切换**：支持MapTSDB/JSON存储模式切换

### 时序存储配置
```properties
# 选择时序存储类型：maptsdb（推荐）或 json
gridinsight.timeseries.type=maptsdb

# MapTSDB配置
gridinsight.maptsdb.enable-memory-mapping=true
gridinsight.maptsdb.enable-transactions=true
gridinsight.maptsdb.batch-size=10
```

## 🔄 事件驱动更新

1. **基础指标更新** → 发布 `MetricValueChangedEvent`
2. **事件监听** → 查找依赖的派生指标
3. **异步计算** → 触发派生指标重新计算
4. **结果存储** → 保存到MapTSDB时序数据库或JSON文件

## 🌐 REST API

### 基础接口
- `GET /api/metrics/health` - 健康检查
- `GET /api/metrics/list` - 指标列表
- `GET /api/metrics/query-by-uuid?uuid=xxx` - UUID查询
- `POST /api/metrics/clear-cache` - 清空缓存

### 时序数据接口
- `GET /api/timeseries/history` - 历史数据查询
- `GET /api/timeseries/latest` - 最新值查询

## 🖥️ Web界面

### 管理界面
- **指标管理**：增删改查基础/派生指标
- **实时监控**：指标状态和统计信息

### API文档页
- **实时值**：表格展示当前指标值
- **历史值**：Chart.js图表展示趋势
- **指标列表**：支持UUID快速查询

## 🧪 测试

```bash
mvn clean test
```

**测试覆盖**：
- 单元测试：公式引擎、模型验证
- 集成测试：计算服务、数据源模拟
- 事件驱动：依赖触发、级联计算
- 时序存储：数据持久化、查询性能

## 💾 时序存储

### MapTSDB时序数据库（推荐）
- **高性能**：基于MapDB构建，支持40万+写入/秒
- **事务支持**：批量提交策略，确保数据一致性
- **动态数据源**：运行时自动添加指标数据源
- **内存映射**：高效的内存使用和磁盘I/O
- **数据恢复**：支持现有数据库文件打开和恢复

### JSON文件存储（备用）
- **轻量级**：无外部依赖，基于JSON文件
- **易调试**：JSON格式便于查看和调试
- **兼容性**：完全向后兼容

### 存储特性
- **双模式支持**：可通过配置切换MapTSDB/JSON存储
- **自动数据源管理**：动态添加指标数据源
- **批量提交优化**：提升写入性能
- **内置缓存**：最新值缓存，毫秒级查询响应
- **时间范围查询**：支持历史数据查询和统计计算

## 📁 项目结构

```
gridinsight/
├── config/                    # 外部配置文件
│   ├── application.properties
│   ├── metrics/
│   └── data/
├── src/main/java/
│   └── com/gridinsight/
│       ├── domain/           # 领域模型
│       ├── service/          # 业务服务
│       ├── controller/       # 控制器
│       └── config/          # 配置类
├── data/                     # 数据目录
│   └── timeseries/          # 时序数据存储（MapTSDB/JSON）
└── README.md
```

## ✨ 特性亮点

- ✅ **外部配置**：修改配置无需重新编译
- ✅ **事件驱动**：派生指标自动更新
- ✅ **多数据源**：HTTP、MQTT、数据库、文件
- ✅ **时序存储**：MapTSDB高性能时序数据库 + JSON文件存储
- ✅ **Web界面**：完整的管理和监控界面
- ✅ **REST API**：完整的API接口
- ✅ **UUID支持**：安全的指标标识和查询

## 🔧 快速验证

1. **健康检查**：http://localhost:9000/api/metrics/health
2. **指标列表**：http://localhost:9000/api/metrics/list  
3. **管理界面**：http://localhost:9000/admin/metrics
4. **API文档**：http://localhost:9000/api/metrics