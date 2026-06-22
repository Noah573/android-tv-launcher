#!/bin/bash

# Android TV Launcher 构建脚本

set -e

echo "=========================================="
echo "  Android TV Launcher 构建脚本"
echo "=========================================="

# 检查Android SDK
if [ -z "$ANDROID_HOME" ]; then
    echo "错误: 未设置ANDROID_HOME环境变量"
    echo "请先安装Android SDK并设置环境变量"
    exit 1
fi

# 检查Java
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java"
    echo "请先安装JDK 17或更高版本"
    exit 1
fi

echo "✓ Android SDK: $ANDROID_HOME"
echo "✓ Java版本: $(java -version 2>&1 | head -1)"
echo ""

# 清理
echo "清理旧文件..."
./gradlew clean

# 构建Debug APK
echo ""
echo "构建Debug APK..."
./gradlew assembleDebug

# 检查结果
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
    echo ""
    echo "=========================================="
    echo "  构建成功！"
    echo "=========================================="
    echo ""
    echo "APK位置: $APK_PATH"
    echo "APK大小: $(du -h "$APK_PATH" | cut -f1)"
    echo ""
    echo "安装到设备:"
    echo "  adb install $APK_PATH"
    echo ""
else
    echo "构建失败，请检查错误信息"
    exit 1
fi
