#!/bin/bash

# 快速构建脚本 - 无需Android Studio

echo "=========================================="
echo "  Android TV Launcher 快速构建"
echo "=========================================="
echo ""

# 检查环境
check_env() {
    if [ -z "$ANDROID_HOME" ]; then
        echo "未检测到Android SDK"
        echo ""
        echo "请按以下步骤安装："
        echo "1. 下载Android SDK Command Line Tools:"
        echo "   https://developer.android.com/studio#command-line-tools-only"
        echo ""
        echo "2. 解压到目录，例如: ~/android-sdk"
        echo ""
        echo "3. 设置环境变量:"
        echo "   export ANDROID_HOME=~/android-sdk"
        echo "   export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin"
        echo ""
        echo "4. 安装SDK:"
        echo "   sdkmanager 'platforms;android-34' 'build-tools;34.0.0'"
        echo ""
        return 1
    fi
    return 0
}

# 检查Java
check_java() {
    if ! command -v java &> /dev/null; then
        echo "未检测到Java"
        echo ""
        echo "请安装JDK 17:"
        echo "  Ubuntu/Debian: sudo apt install openjdk-17-jdk"
        echo "  macOS: brew install openjdk@17"
        echo ""
        return 1
    fi
    return 0
}

# 主流程
main() {
    check_java || exit 1
    check_env || exit 1
    
    echo "✓ 环境检查通过"
    echo ""
    
    # 给gradlew执行权限
    chmod +x gradlew 2>/dev/null || true
    
    # 清理并构建
    echo "开始构建..."
    ./gradlew assembleDebug --no-daemon
    
    # 检查结果
    APK="app/build/outputs/apk/debug/app-debug.apk"
    if [ -f "$APK" ]; then
        SIZE=$(du -h "$APK" | cut -f1)
        echo ""
        echo "=========================================="
        echo "  ✅ 构建成功！"
        echo "=========================================="
        echo ""
        echo "APK文件: $APK"
        echo "文件大小: $SIZE"
        echo ""
        echo "安装方法:"
        echo "  1. 连接TV设备或模拟器"
        echo "  2. 运行: adb install $APK"
        echo "  3. 按HOME键选择TV桌面"
        echo ""
    else
        echo "❌ 构建失败"
        exit 1
    fi
}

main "$@"
