#!/bin/bash

# GridInsight 测试运行脚本

echo "=== GridInsight 测试运行脚本 ==="
echo "开始运行测试..."

# 设置颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: Maven未安装或不在PATH中${NC}"
    exit 1
fi

# 清理并编译项目
echo -e "${YELLOW}1. 清理并编译项目...${NC}"
mvn clean compile test-compile
if [ $? -ne 0 ]; then
    echo -e "${RED}编译失败${NC}"
    exit 1
fi
echo -e "${GREEN}编译成功${NC}"

# 运行单元测试
echo -e "${YELLOW}2. 运行单元测试...${NC}"
mvn test -Dtest="*Test" -DfailIfNoTests=false
if [ $? -ne 0 ]; then
    echo -e "${RED}单元测试失败${NC}"
    exit 1
fi
echo -e "${GREEN}单元测试通过${NC}"

# 运行集成测试
echo -e "${YELLOW}3. 运行集成测试...${NC}"
mvn test -Dtest="*IntegrationTest" -DfailIfNoTests=false
if [ $? -ne 0 ]; then
    echo -e "${RED}集成测试失败${NC}"
    exit 1
fi
echo -e "${GREEN}集成测试通过${NC}"

# 运行端到端测试
echo -e "${YELLOW}4. 运行端到端测试...${NC}"
mvn test -Dtest="*EndToEndTest" -DfailIfNoTests=false
if [ $? -ne 0 ]; then
    echo -e "${RED}端到端测试失败${NC}"
    exit 1
fi
echo -e "${GREEN}端到端测试通过${NC}"

# 生成测试报告
echo -e "${YELLOW}5. 生成测试报告...${NC}"
mvn surefire-report:report
if [ $? -eq 0 ]; then
    echo -e "${GREEN}测试报告已生成: target/site/surefire-report.html${NC}"
else
    echo -e "${YELLOW}测试报告生成失败，但测试已通过${NC}"
fi

# 运行所有测试
echo -e "${YELLOW}6. 运行所有测试...${NC}"
mvn test
if [ $? -ne 0 ]; then
    echo -e "${RED}部分测试失败${NC}"
    exit 1
fi

echo -e "${GREEN}=== 所有测试通过！ ===${NC}"
echo "测试报告: target/site/surefire-report.html"
echo "测试覆盖率报告: target/site/jacoco/index.html"
