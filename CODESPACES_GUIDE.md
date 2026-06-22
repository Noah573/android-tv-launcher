# 使用GitHub Codespaces构建APK

## 步骤1: 创建GitHub仓库

1. 访问 https://github.com/new
2. 仓库名称: `AndroidTVLauncher`
3. 选择 "Public"
4. 点击 "Create repository"

## 步骤2: 上传项目文件

在仓库页面，点击 "uploading an existing file"，上传以下文件：

```
AndroidTVLauncher/
├── .github/workflows/build.yml
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/tvlauncher/
│       │   ├── MainActivity.kt
│       │   ├── AppInfo.kt
│       │   ├── AppListAdapter.kt
│       │   ├── FavoriteAppAdapter.kt
│       │   ├── WifiStateReceiver.kt
│       │   └── TVLauncherApp.kt
│       └── res/
│           ├── anim/
│           ├── drawable/
│           ├── layout/
│           └── values/
├── build.gradle.kts
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── settings.gradle.kts
└── README.md
```

## 步骤3: 启动Codespace

1. 在仓库页面，点击绿色的 "Code" 按钮
2. 选择 "Codespaces" 标签
3. 点击 "Create codespace on main"

## 步骤4: 构建APK

在Codespace终端中运行：

```bash
# 给gradlew执行权限
chmod +x gradlew

# 构建APK
./gradlew assembleDebug

# APK位置
ls -la app/build/outputs/apk/debug/
```

## 步骤5: 下载APK

1. 在文件浏览器中找到 `app/build/outputs/apk/debug/app-debug.apk`
2. 右键点击文件，选择 "Download"

## 安装到TV

```bash
adb install app-debug.apk
```

## 注意事项

- Codespaces有免费额度限制
- 构建完成后记得删除Codespace节省额度
- APK可以直接在Android TV上安装使用

## 功能特性

- ✅ Apple TV风格UI
- ✅ 横屏适配
- ✅ 遥控器支持
- ✅ WiFi状态显示
- ✅ 流畅动效
- ✅ 性能优化
