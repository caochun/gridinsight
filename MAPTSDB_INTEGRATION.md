# MapTSDB集成指南

## 概述

本指南说明如何将MapTSDB时序数据库集成到GridInsight项目中，以替换当前的JSON文件存储方式。

## MapTSDB简介

MapTSDB是一个基于MapDB构建的高性能时序数据存储系统，专为物联网和边缘计算场景设计。它提供了：

- 高效的数据写入和查询
- 时间范围查询功能
- 数据压缩功能
- 多种数据类型支持
- 批量写入API
- 高性能并发处理

## 集成步骤

### 1. 获取MapTSDB JAR文件

#### 方式1：从GitHub仓库构建

```bash
# 克隆MapTSDB仓库
git clone https://github.com/caochun/maptsdb.git
cd mportsdb

# 构建JAR文件
mvn clean package

# 复制JAR文件到GridInsight项目的libs目录
cp target/maptsdb-1.3.0.jar /path/to/gridinsight/libs/
```

#### 方式2：使用提供的脚本

```bash
# 运行设置脚本
./setup-maptsdb.sh
```

### 2. 启用MapTSDB依赖

在 `pom.xml` 中取消注释MapTSDB依赖：

```xml
<!-- MapTSDB - 基于MapDB的时序数据存储系统 (本地JAR) -->
<dependency>
    <groupId>com.maptsdb</groupId>
    <artifactId>maptsdb</artifactId>
    <version>1.3.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/maptsdb-1.3.0.jar</systemPath>
</dependency>
```

### 3. 创建MapTSDB服务实现

创建 `MapTsdbTimeSeriesDataService.java` 文件，实现 `TimeSeriesDataService` 接口。

### 4. 配置MapTSDB

在 `config/application.properties` 中配置MapTSDB：

```properties
# 时序数据库配置
gridinsight.timeseries.type=maptsdb
gridinsight.timeseries.data-path=data/timeseries

# MapTSDB配置
gridinsight.maptsdb.enable-memory-mapping=true
gridinsight.maptsdb.enable-transactions=true
gridinsight.maptsdb.concurrency-scale=16
gridinsight.maptsdb.cleanup-on-shutdown=true
```

### 5. 创建配置类

创建 `TimeSeriesDataConfig.java` 配置类，根据配置自动选择使用JSON文件存储或MapTSDB存储。

## 配置选项

### 时序数据库类型

- `json`: 使用JSON文件存储（默认）
- `maptsdb`: 使用MapTSDB存储

### MapTSDB配置选项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `gridinsight.maptsdb.enable-memory-mapping` | `true` | 启用内存映射文件 |
| `gridinsight.maptsdb.enable-transactions` | `true` | 启用事务支持 |
| `gridinsight.maptsdb.concurrency-scale` | `16` | 并发级别 |
| `gridinsight.maptsdb.cleanup-on-shutdown` | `true` | 关闭时清理资源 |

## 性能优势

根据MapTSDB的性能测试数据：

- **数值类型写入**: 377,929条/秒
- **对象类型写入**: 78,388条/秒
- **批量写入优化**: 大数据量时性能提升2.89倍
- **并发性能**: 8线程处理80万数据点，零数据丢失

## 使用示例

### 基本使用

```java
// 自动配置，根据gridinsight.timeseries.type选择实现
@Autowired
private TimeSeriesDataService timeSeriesDataService;

// 存储指标值
timeSeriesDataService.storeMetricValue(identifier, value, timestamp);

// 获取最新值
MetricValue latestValue = timeSeriesDataService.getLatestMetricValue(identifier);

// 获取历史数据
List<MetricValue> history = timeSeriesDataService.getMetricHistory(
    identifier, startTime, endTime);
```

### 批量操作

```java
// 批量获取最新值
Map<String, MetricValue> values = timeSeriesDataService.getLatestMetricValues(identifiers);
```

## 迁移指南

### 从JSON存储迁移到MapTSDB

1. 备份现有的JSON时序数据文件
2. 配置使用MapTSDB存储
3. 重启应用
4. MapTSDB会自动创建新的存储结构
5. 验证数据读写功能正常

### 回退到JSON存储

如果遇到问题，可以通过修改配置回退到JSON存储：

```properties
gridinsight.timeseries.type=json
```

## 故障排除

### 常见问题

1. **JAR文件找不到**
   - 确保 `libs/maptsdb-1.3.0.jar` 文件存在
   - 检查文件路径是否正确

2. **编译错误**
   - 确保已取消注释MapTSDB依赖
   - 检查import语句是否正确

3. **运行时错误**
   - 检查MapTSDB配置是否正确
   - 查看应用日志获取详细错误信息

### 日志配置

```properties
# 启用MapTSDB调试日志
logging.level.com.maptsdb=DEBUG
logging.level.com.gridinsight.service.MapTsdbTimeSeriesDataService=DEBUG
```

## 开发计划

- [ ] 完善MapTSDB服务实现
- [ ] 添加性能监控和统计
- [ ] 实现数据迁移工具
- [ ] 添加单元测试
- [ ] 性能基准测试

## 参考资料

- [MapTSDB GitHub仓库](https://github.com/caochun/maptsdb)
- [MapDB官方文档](https://mapdb.org/)
- [Spring Boot配置指南](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)
