# libGDX Joystick Demo

[English](README.md) | **中文**

这是一个基于 `libGDX` 的方向指示器演示项目，支持普通桌面运行，也支持通过 `TeaVM` 构建为网页版本和 TeaVM Native GLFW 原生桌面版本。

## 技术栈

- Java 17+
- Gradle Wrapper
- libGDX 1.14.2
- gdx-controllers 2.2.4
- LWJGL3 桌面后端
- TeaVM Native GLFW 原生桌面后端
- TeaVM Web 后端
- FreeType 字体渲染

## 环境要求

- JDK 17 或更高版本

项目自带 `gradlew` / `gradlew.bat`，不需要额外安装 Gradle。

## 快速开始

在项目根目录执行命令即可。

### 运行桌面版

```powershell
.\gradlew.bat desktop-lwjgl3:run
```

说明：

- 启动入口：`com.libgdx.joystick.lwjgl3.Lwjgl3Launcher`
- 启动后会打开桌面窗口，默认大小为 `960 x 640`
- 支持鼠标、键盘和手柄 / controller 输入

### 运行网页版

```powershell
.\gradlew.bat web-teavm:run
```

说明：

- 构建入口：`com.libgdx.joystick.web.TeaVMBuilder`
- Web 启动入口：`com.libgdx.joystick.web.TeaVMLauncher`
- 本地预览地址：`http://localhost:8089`
- 支持鼠标、键盘和浏览器 Gamepad API 输入

### 运行 TeaVM Native GLFW 原生桌面版

这是一条原生构建链路，不是普通的 JVM 启动方式。

常用任务：

- `:desktop-glfw:gdx_teavm_glfw_generate`
- `:desktop-glfw:gdx_teavm_glfw_build`
- `:desktop-glfw:gdx_teavm_glfw_run`

Windows 11 + MSVC：

```powershell
.\gradlew.bat :desktop-glfw:gdx_teavm_glfw_build
```

WSL2 Ubuntu 24.04 / Linux：

```bash
./gradlew :desktop-glfw:gdx_teavm_glfw_build
```

macOS arm64：

```bash
./gradlew :desktop-glfw:gdx_teavm_glfw_build
```

说明：

- 模块目录：`desktop-glfw`
- FreeType 源码会在 GLFW 原生任务执行前由 Gradle 自动准备
- 如需提前拉取，可单独执行 `:desktop-glfw:freetype_sync_source`
- 下载缓存目录：`.gradle/desktop-glfw/freetype`
- 同步到项目内的源码目录：`desktop-glfw/native/thirdparty/freetype`
- 如需清理同步后的项目内源码，可执行 `:desktop-glfw:freetype_clean_source`
- 不要直接在 IDE 里运行 `com.libgdx.joystick.glfw.GlfwLauncher`
- 日常调试游戏逻辑更适合 `:desktop-lwjgl3`
- 支持鼠标、键盘和原生 GLFW 手柄输入
- Windows 11 + MSVC 环境说明见 [readme-win11(msvc).zh-CN.md](<desktop-glfw/readme-win11(msvc).zh-CN.md>)
- WSL2 Ubuntu 24.04 环境说明见 [readme-wsl2(ubuntu2404).zh-CN.md](<desktop-glfw/readme-wsl2(ubuntu2404).zh-CN.md>)
- macOS arm64 环境说明见 [readme-mac(arm64).zh-CN.md](<desktop-glfw/readme-mac(arm64).zh-CN.md>)

### `desktop-glfw` 模块说明

`desktop-glfw` 不是普通的 JVM 桌面启动模块，而是一条 TeaVM Native GLFW 原生构建链路。

它的主要职责是：

- 把 `core` 里的游戏逻辑通过 TeaVM 生成为 C 代码
- 把生成出的 C 代码、本地 FreeType bridge、由 Gradle 准备好的最新 FreeType 源码，以及平台相关原生依赖交给 CMake 编译
- 产出可运行的原生桌面程序

使用上建议注意这几点：

- 不要直接把 `com.libgdx.joystick.glfw.GlfwLauncher` 当普通 Java `main()` 运行
- 推荐通过 `desktop-glfw:gdx_teavm_glfw_generate`、`desktop-glfw:gdx_teavm_glfw_build`、`desktop-glfw:gdx_teavm_glfw_run` 这些任务驱动
- 需要预拉取或清理 FreeType 缓存时，可使用 `desktop-glfw:freetype_sync_source` 和 `desktop-glfw:freetype_clean_cache`
- `desktop-glfw:freetype_sync_source` 执行后，也会把准备好的源码同步到 `desktop-glfw/native/thirdparty/freetype`
- 目前已整理好 Windows 11 + MSVC、WSL2 Ubuntu 24.04 和 macOS arm64 三套平台文档

## 项目结构

- `core`：核心游戏逻辑
- `core`：同时直接读取标准 `Controllers` API 的手柄状态
- `desktop-lwjgl3`：普通桌面启动模块
- `web-teavm`：网页端构建与预览模块
- `desktop-glfw`：TeaVM Native GLFW 原生桌面模块
- `assets`：图片、字体等资源文件

## 功能说明

程序会在屏幕中心显示一个方向底座，现在同时也是一个小型输入测试器：

- 鼠标 / 触摸拖拽时，箭头会根据方向实时旋转
- 键盘输入会显示在界面底部的事件文本中
- 桌面、Web 和 TeaVM Native GLFW 三个版本都支持手柄 / controller 输入
- 底部 HUD 会显示当前 I/O 来源、已连接手柄名称，以及最近一次输入事件

## 常用命令

```powershell
.\gradlew.bat build
.\gradlew.bat desktop-lwjgl3:run
.\gradlew.bat web-teavm:run
.\gradlew.bat :desktop-glfw:gdx_teavm_glfw_build
```
