#!/bin/bash

# GridInsight 外部配置文件启动脚本
# 支持从外部配置文件加载指标定义，无需重新编译

echo "启动 GridInsight 指标管理系统..."
echo "使用外部配置文件模式"

# 检查配置文件是否存在
if [ ! -f "config/metrics/basic-metrics.yaml" ]; then
    echo "警告: 基础指标配置文件不存在: config/metrics/basic-metrics.yaml"
    echo "将使用默认配置"
fi

if [ ! -f "config/metrics/derived-metrics.yaml" ]; then
    echo "警告: 派生指标配置文件不存在: config/metrics/derived-metrics.yaml"
    echo "将使用默认配置"
fi

# 设置外部配置文件路径
export GRIDINSIGHT_METRICS_BASIC_CONFIG_FILE=config/metrics/basic-metrics.yaml
export GRIDINSIGHT_METRICS_DERIVED_CONFIG_FILE=config/metrics/derived-metrics.yaml

# 启动应用
echo "启动应用..."
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.config.location=config/application.properties"
