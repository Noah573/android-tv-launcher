#!/bin/bash
set -e

echo "=========================================="
echo "  Android TV Launcher 构建脚本"
echo "=========================================="

# 设置环境
export ANDROID_HOME=~/android-sdk
export JAVA_HOME=$ANDROID_HOME/java/jdk-17.0.9+9-jre
export PATH=$JAVA_HOME/bin:$ANDROID_HOME/build-tools/34.0.0:$PATH

# 项目目录
PROJECT_DIR=$(pwd)
BUILD_DIR=$PROJECT_DIR/app/build
OUTPUT_DIR=$BUILD_DIR/outputs/apk

# 清理
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR/{gen,obj,classes,dex,apk}
mkdir -p $OUTPUT_DIR

echo "✓ 环境准备完成"

# 1. 生成R.java
echo "生成资源文件..."
AAPT2=$ANDROID_HOME/build-tools/34.0.0/aapt2
AAPT=$ANDROID_HOME/build-tools/34.0.0/aapt
ANDROID_JAR=$ANDROID_HOME/platforms/android-34/android.jar

$AAPT package -f -m \
    -S $PROJECT_DIR/app/src/main/res \
    -J $BUILD_DIR/gen \
    -M $PROJECT_DIR/app/src/main/AndroidManifest.xml \
    -I $ANDROID_JAR

echo "✓ 资源文件生成完成"

# 2. 编译Java代码
echo "编译Java代码..."
find $PROJECT_DIR/app/src/main/java -name "*.kt" -o -name "*.java" > $BUILD_DIR/sources.txt
find $BUILD_DIR/gen -name "*.java" >> $BUILD_DIR/sources.txt

javac -source 1.8 -target 1.8 \
    -classpath $ANDROID_JAR \
    -d $BUILD_DIR/classes \
    @$BUILD_DIR/sources.txt

echo "✓ Java编译完成"

# 3. 转换为DEX
echo "转换为DEX..."
D8=$ANDROID_HOME/build-tools/34.0.0/d8
$D8 --release --min-api 21 \
    --output $BUILD_DIR/dex \
    $(find $BUILD_DIR/classes -name "*.class")

echo "✓ DEX转换完成"

# 4. 打包APK
echo "打包APK..."
$AAPT package -f \
    -M $PROJECT_DIR/app/src/main/AndroidManifest.xml \
    -S $PROJECT_DIR/app/src/main/res \
    -I $ANDROID_JAR \
    -F $BUILD_DIR/apk/app.unsigned.apk

# 添加DEX到APK
cd $BUILD_DIR/dex
zip -u $BUILD_DIR/apk/app.unsigned.apk classes.dex
cd $PROJECT_DIR

echo "✓ APK打包完成"

# 5. 对齐
echo "对齐APK..."
ZIPALIGN=$ANDROID_HOME/build-tools/34.0.0/zipalign
$ZIPALIGN -f 4 $BUILD_DIR/apk/app.unsigned.apk $BUILD_DIR/apk/app.aligned.apk

echo "✓ APK对齐完成"

# 6. 签名（使用debug签名）
echo "签名APK..."
APKSIGNER=$ANDROID_HOME/build-tools/34.0.0/apksigner

# 创建debug keystore
if [ ! -f ~/.android/debug.keystore ]; then
    mkdir -p ~/.android
    keytool -genkey -v -keystore ~/.android/debug.keystore \
        -storepass android -keypass android \
        -alias androiddebugkey \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=Android Debug,O=Android,C=US"
fi

$APKSIGNER sign --ks ~/.android/debug.keystore \
    --ks-pass pass:android \
    --key-pass pass:android \
    --ks-key-alias androiddebugkey \
    --out $OUTPUT_DIR/app-debug.apk \
    $BUILD_DIR/apk/app.aligned.apk

echo "✓ APK签名完成"

# 7. 完成
echo ""
echo "=========================================="
echo "  ✅ 构建成功！"
echo "=========================================="
echo ""
echo "APK位置: $OUTPUT_DIR/app-debug.apk"
echo "APK大小: $(du -h $OUTPUT_DIR/app-debug.apk | cut -f1)"
echo ""
echo "安装命令:"
echo "  adb install $OUTPUT_DIR/app-debug.apk"
echo ""
