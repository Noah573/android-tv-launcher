# Android TV Launcher

一个高性能、美观的Android TV桌面应用，仿照Apple TV UI设计。

## ✨ 特性

- 🎨 **Apple TV风格UI** - 简洁美观的深色主题
- 📱 **完全适配横屏** - 支持任意比例和尺寸
- ⚡ **极致性能优化** - 对低端芯片也能流畅运行
- 🎮 **遥控器/手柄完美适配** - 支持所有TV遥控操作
- 📊 **顶部状态栏** - 显示WiFi、时间、日期等信息
- ✨ **流畅动效** - 不影响性能的轻量级动画

## 📦 安装

### 方法1: 直接安装APK
```bash
# 下载预编译APK
adb install tvlauncher.apk
```

### 方法2: 从源码构建
```bash
# 1. 克隆项目
cd AndroidTVLauncher

# 2. 运行构建脚本
chmod +x build.sh
./build.sh

# 3. 安装APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 🎯 设置为默认桌面

安装后，按HOME键会提示选择桌面应用，选择"TV桌面"并设为默认即可。

## 🎮 遥控器操作

| 按键 | 功能 |
|------|------|
| ↑↓←→ | 导航选择 |
| 确认/OK | 打开应用 |
| HOME | 返回桌面 |
| 返回 | 留在桌面 |

## 🔧 技术特性

### 性能优化
- 使用ViewBinding避免findViewById
- RecyclerView复用和缓存优化
- 轻量级动画，避免过度绘制
- ProGuard代码压缩
- 禁用不必要的日志

### 兼容性
- 最低支持Android 5.0 (API 21)
- 适配所有TV分辨率
- 支持Leanback特性

## 📁 项目结构

```
app/src/main/
├── java/com/tvlauncher/
│   ├── MainActivity.kt          # 主Activity
│   ├── AppInfo.kt               # 应用信息数据类
│   ├── AppListAdapter.kt        # 应用列表适配器
│   ├── FavoriteAppAdapter.kt    # 收藏应用适配器
│   ├── WifiStateReceiver.kt     # WiFi状态监听
│   └── TVLauncherApp.kt         # Application
├── res/
│   ├── layout/                  # 布局文件
│   ├── drawable/                # 图形资源
│   ├── values/                  # 颜色、字符串、主题
│   └── anim/                    # 动画
└── AndroidManifest.xml
```

## 🎨 自定义

### 修改颜色
编辑 `res/values/colors.xml` 文件。

### 修改布局
编辑 `res/layout/` 下的XML文件。

### 添加天气功能
需要集成天气API，在 `MainActivity.kt` 中添加网络请求。

## 📝 许可证

MIT License

## 🤝 贡献

欢迎提交Issue和Pull Request！
