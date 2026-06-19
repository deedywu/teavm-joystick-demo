# libGDX Joystick Demo

这是一个基于 `libGDX` 的方向指示器演示项目，支持桌面端运行，也支持通过 `TeaVM` 构建为网页版本。

## 技术栈

- Java 17+
- Gradle Wrapper
- libGDX 1.14.2
- LWJGL3 桌面后端
- TeaVM Web 后端
- FreeType 字体渲染

## 环境要求

- JDK 17 或更高版本

项目自带 `gradlew` / `gradlew.bat`，开箱即用，不需要额外安装 Gradle。

## 快速开始

在项目根目录执行命令即可。

### 运行桌面版

```powershell
.\gradlew.bat lwjgl3:run
```

说明：

- 启动入口：`com.libgdx.joystick.lwjgl3.Lwjgl3Launcher`
- 启动后会打开桌面窗口，默认大小为 `960 x 640`

### 运行网页版

```powershell
.\gradlew.bat teavm:run
```

说明：

- 构建入口：`com.libgdx.joystick.web.TeaVMBuilder`
- Web 启动入口：`com.libgdx.joystick.web.TeaVMLauncher`
- 本地预览地址：`http://localhost:8089`

## 项目结构

- `core`：核心游戏逻辑
- `lwjgl3`：桌面端启动模块
- `teavm`：网页端构建与预览模块
- `assets`：图片、字体等资源文件

## 功能说明

程序会在屏幕中心显示一个方向底座，点击或按住拖动后，箭头会根据拖动方向实时旋转，用于演示方向输入效果。

## 常用命令

```powershell
.\gradlew.bat build
.\gradlew.bat lwjgl3:run
.\gradlew.bat teavm:run
```
