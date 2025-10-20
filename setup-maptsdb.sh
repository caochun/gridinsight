#!/bin/bash

# MapTSDB设置脚本
# 用于下载和设置MapTSDB依赖

echo "开始设置MapTSDB..."

# 创建libs目录
mkdir -p libs

# 检查是否已经存在JAR文件
if [ -f "libs/maptsdb-1.3.0.jar" ]; then
    echo "MapTSDB JAR文件已存在"
    exit 0
fi

# 下载MapTSDB JAR文件（如果GitHub Releases中有的话）
echo "尝试从GitHub下载MapTSDB JAR文件..."

# 方法1：尝试从GitHub Releases下载
wget -O libs/maptsdb-1.3.0.jar "https://github.com/caochun/maptsdb/releases/download/v1.3.0/maptsdb-1.3.0.jar" 2>/dev/null

if [ $? -eq 0 ] && [ -f "libs/maptsdb-1.3.0.jar" ]; then
    echo "✅ 成功从GitHub Releases下载MapTSDB JAR文件"
else
    echo "❌ 无法从GitHub Releases下载，请手动构建"
    echo ""
    echo "请执行以下步骤手动构建MapTSDB："
    echo "1. git clone https://github.com/caochun/maptsdb.git"
    echo "2. cd mportsdb"
    echo "3. mvn clean package"
    echo "4. cp target/maptsdb-1.3.0.jar $(pwd)/libs/"
    echo ""
    echo "或者，你可以："
    echo "1. 在IDE中打开MapTSDB项目"
    echo "2.1 运行Maven构建"
    echo "3. 将生成的JAR文件复制到libs目录"
fi

echo "MapTSDB设置完成"
