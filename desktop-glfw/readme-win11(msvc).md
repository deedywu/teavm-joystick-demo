# Windows 11 下配置 `desktop-glfw`（MSVC）

`desktop-glfw` 不是普通的 JVM 桌面启动模块。它会先把 Java 代码通过 TeaVM 生成为 C，再交给 CMake 和 MSVC 编译成原生程序。

这个模块不要直接在 IDE 里运行 [GlfwLauncher.java](src/main/java/com/libgdx/joystick/glfw/GlfwLauncher.java)。正确入口是 Gradle 任务：

- `:desktop-glfw:gdx_teavm_glfw_generate`
- `:desktop-glfw:gdx_teavm_glfw_build`
- `:desktop-glfw:gdx_teavm_glfw_run`

下面默认你已经在 Windows 11 终端里，并且 `git`、`java`、`javac` 都已经可用。

## 1. 安装 Visual Studio 和 C++ 工具链

安装 Visual Studio Community。安装时至少勾选：

- `Desktop development with C++`
- `MSVC v143/v144 C++ build tools`
- `C++ CMake tools for Windows`
- `Windows 10/11 SDK`

装好后，下面这两个工具需要存在：

- `cmake.exe`
- `MSBuild.exe`

本项目的 [build.gradle](build.gradle) 会自动扫描常见的 Visual Studio 安装目录，不需要你手工改脚本。

## 2. 检查 CMake 和 MSBuild

可以在 PowerShell 里先确认工具已经安装好：

```powershell
Get-Command cmake
Get-Command msbuild
```

如果 `Get-Command msbuild` 找不到，也可以直接检查常见目录：

```powershell
Test-Path "C:\Program Files\Microsoft Visual Studio\18\Community\MSBuild\Current\Bin\MSBuild.exe"
Test-Path "C:\Program Files\Microsoft Visual Studio\18\Community\Common7\IDE\CommonExtensions\Microsoft\CMake\CMake\bin\cmake.exe"
```

如果你的 Visual Studio 不是装在默认目录，也没关系，`desktop-glfw/build.gradle` 会在常见 `Program Files` 目录里自动查找。

## 3. 初始化 FreeType submodule

`desktop-glfw` 依赖一个 FreeType submodule。首次 clone 后一定要执行：

```powershell
git submodule update --init --recursive
```

执行完以后，这个目录里应该有内容：

```text
desktop-glfw\native\thirdparty\freetype
```

如果这里是空目录，后面的 CMake 构建会失败。

## 4. 运行构建任务

在项目根目录执行：

```powershell
.\gradlew.bat :desktop-glfw:gdx_teavm_glfw_generate
.\gradlew.bat :desktop-glfw:gdx_teavm_glfw_build
.\gradlew.bat :desktop-glfw:gdx_teavm_glfw_run
```

说明：

- `gdx_teavm_glfw_generate`：生成 TeaVM C 代码并整理资源
- `gdx_teavm_glfw_build`：调用 CMake 和 MSBuild 编译原生程序
- `gdx_teavm_glfw_run`：生成、编译并运行程序

如果只想先验证编译是否通过，可以先跑：

```powershell
.\gradlew.bat :desktop-glfw:gdx_teavm_glfw_build
```

如果想从干净状态重来一遍：

```powershell
.\gradlew.bat clean :desktop-glfw:gdx_teavm_glfw_run
```

## 5. 生成结果在哪

重点目录：

- `desktop-glfw\build\dist\glfw`
- `desktop-glfw\build\dist\glfw\CMakeLists.txt`
- `desktop-glfw\build\dist\glfw\app_debug.bat`
- `desktop-glfw\build\dist\glfw\c\release\app_debug.exe`

`build\dist\glfw` 是生成目录，不建议手工长期修改。真正需要维护的源码在：

- `desktop-glfw\native\src`
- `desktop-glfw\native\thirdparty`

## 常见问题

### 1. `git submodule` 没执行

现象通常是 CMake 报 `thirdparty/freetype` 不存在。

处理方法：

```powershell
git submodule update --init --recursive
```

### 2. `cmake` 或 `MSBuild` 找不到

先确认 Visual Studio 安装时是否勾选了 C++ 工作负载和 CMake 工具。

必要时可以在 PowerShell 里确认：

```powershell
Get-Command cmake
Get-Command msbuild
```

### 3. `app_debug.exe` 正在运行

如果上一次启动后的程序还没退出，重新构建时可能失败。

先把旧进程关掉，再重新跑：

```powershell
taskkill /IM app_debug.exe /F
```

### 4. 在 IDE 里直接点 `main()` 报错

这个模块不是普通 Java 启动方式。不要直接运行 `GlfwLauncher.main()`，请走 Gradle 任务。

## 补充

如果你只是调游戏逻辑，平时更适合用 `:desktop-lwjgl3`。  
`desktop-glfw` 更适合验证 TeaVM Native GLFW 这条原生构建链路。
