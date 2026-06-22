#!/bin/bash

echo "=========================================="
echo "  Android TV Launcher 在线构建指南"
echo "=========================================="
echo ""

# 检查是否有浏览器
if command -v xdg-open &> /dev/null; then
    BROWSER="xdg-open"
elif command -v open &> /dev/null; then
    BROWSER="open"
else
    BROWSER="echo"
fi

echo "由于本地环境缺少Android SDK，推荐使用在线构建服务："
echo ""
echo "1. GitHub Actions（推荐）"
echo "   - 访问: https://github.com/new"
echo "   - 创建仓库: AndroidTVLauncher"
echo "   - 上传项目文件"
echo "   - GitHub Actions会自动构建APK"
echo ""
echo "2. Appetize.io"
echo "   - 访问: https://appetize.io"
echo "   - 上传项目文件"
echo "   - 在线构建和测试"
echo ""
echo "3. Replit"
echo "   - 访问: https://replit.com"
echo "   - 创建Android项目"
echo "   - 上传代码并构建"
echo ""

# 打开GitHub
echo "正在打开GitHub..."
$BROWSER "https://github.com/new" 2>/dev/null || echo "请手动访问: https://github.com/new"
