# libGDX Joystick Demo

这是一个基于 `libGDX` 的方向指示器演示项目，支持桌面端运行，也支持通过 `TeaVM` 构建为网页版本。

## 技术栈

- Java 17+
- Gradle Wrapper
- libGDX 1.14.2
- LWJGL3 桌面后端
- GLFW 桌面后端(powered by teavm)
- Web 后端(powered by teavm)
- FreeType 字体渲染

## 环境要求

- JDK 17 或更高版本

项目自带 `gradlew` / `gradlew.bat`，开箱即用，不需要额外安装 Gradle。

## 快速开始

在项目根目录执行命令即可。

### 运行桌面版

```powershell
.\gradlew.bat desktop-lwjgl3:run
```

说明：

- 启动入口：`com.libgdx.joystick.lwjgl3.Lwjgl3Launcher`
- 启动后会打开桌面窗口，默认大小为 `960 x 640`

### 运行网页版

```powershell
.\gradlew.bat web-teavm:run
```

说明：

- 构建入口：`com.libgdx.joystick.web.TeaVMBuilder`
- Web 启动入口：`com.libgdx.joystick.web.TeaVMLauncher`
- 本地预览地址：`http://localhost:8089`

### 运行 TeaVM GLFW 原生桌面版

```powershell
.\gradlew.bat desktop-glfw:gdx_teavm_glfw_build
```

说明：

- 模块目录：`desktop-glfw`
- 首次 clone 后如果 `desktop-glfw/native/thirdparty/freetype` 为空，先执行 `git submodule update --init --recursive`

### `desktop-glfw` 模块说明

`desktop-glfw` 不是普通的 JVM 桌面启动模块，而是一条 TeaVM Native GLFW 构建链路。

它的主要职责是：

- 把 `core` 里的游戏逻辑通过 TeaVM 生成为 C 代码
- 把生成出的 C 代码、本地 FreeType bridge、以及 `freetype` 第三方源码一起交给 CMake/MSVC 编译
- 产出可运行的原生桌面程序

使用上建议注意这几点：

- 不要直接把 `com.libgdx.joystick.glfw.GlfwLauncher` 当普通 Java `main()` 在 IDE 里运行
- 推荐通过 `desktop-glfw:gdx_teavm_glfw_generate`、`desktop-glfw:gdx_teavm_glfw_build`、`desktop-glfw:gdx_teavm_glfw_run` 这些任务驱动
- `desktop-glfw/native/thirdparty/freetype` 来自 Git submodule，本地缺失时先执行 `git submodule update --init --recursive`
- 该模块当前主要面向 Windows + MSVC 工具链

## 项目结构

- `core`：核心游戏逻辑
- `desktop-lwjgl3`：桌面端启动模块
- `web-teavm`：网页端构建与预览模块
- `desktop-glfw`：TeaVM GLFW 原生桌面模块
- `assets`：图片、字体等资源文件

## 功能说明

程序会在屏幕中心显示一个方向底座，点击或按住拖动后，箭头会根据拖动方向实时旋转，用于演示方向输入效果。

## 常用命令

```powershell
.\gradlew.bat build
.\gradlew.bat desktop-lwjgl3:run
.\gradlew.bat web-teavm:run
```
