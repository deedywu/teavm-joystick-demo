# `desktop-glfw` 模块说明

这是当前项目的 TeaVM GLFW 原生桌面目标模块。

它的职责不是像普通 JVM 桌面模块那样直接运行 Java，而是：

1. 把 `core` 里的游戏逻辑通过 TeaVM 生成为 C 代码
2. 把生成出的 C、GLFW/GLEW 依赖、以及本项目自定义的 FreeType bridge 一起交给 CMake/MSVC 编译
3. 产出 Windows 原生可执行文件

## 当前推荐用法

### 首次拉取仓库

`desktop-glfw` 模块依赖 `native/thirdparty/freetype` 这个 Git submodule。

如果刚 clone 仓库，或者拉到了新的 submodule 指针提交后本地目录不完整，需要先执行：

```powershell
git submodule update --init --recursive
```

### 不推荐

不要直接在 IDE 里把 [GlfwLauncher.java](src/main/java/com/libgdx/joystick/glfw/GlfwLauncher.java) 当成普通 Java `main()` 运行。

原因：

- 这个入口类面向 TeaVM GLFW 原生后端
- 它依赖 TeaVM 生成/原生链接后的运行环境
- 直接在 JVM 中运行，容易遇到 `UnsatisfiedLinkError` 或 TeaVM interop 相关报错

### 推荐

在 Windows 上，优先通过 Gradle 任务运行：

```powershell
./gradlew :desktop-glfw:gdx_teavm_glfw_generate
./gradlew :desktop-glfw:gdx_teavm_glfw_build
./gradlew :desktop-glfw:gdx_teavm_glfw_run
```

说明：

- `gdx_teavm_glfw_generate`
  只做 TeaVM C 代码生成与资源整理
- `gdx_teavm_glfw_build`
  生成后调用 CMake/MSBuild 构建原生程序
- `gdx_teavm_glfw_run`
  先生成、再构建、最后运行生成的原生程序

## Windows 环境要求

当前项目已经针对你这台机器做了 Windows/MSVC 路径适配，默认目标是 Visual Studio 2026 Community。

建议环境：

- JDK 17
- Visual Studio Community 2026
  需要包含 C++ 工具链
- Visual Studio 自带的 CMake 或系统 PATH 中可用的 `cmake`

当前项目已验证的路径包括：

``` 下面是默认的vs安装路径，实际你自己的为准 ```
- `C:\Program Files\Microsoft Visual Studio\18\Community\Common7\IDE\CommonExtensions\Microsoft\CMake\CMake\bin\cmake.exe`
- `C:\Program Files\Microsoft Visual Studio\18\Community\MSBuild\Current\Bin\MSBuild.exe`

## 目录说明

### 模块源码

- [build.gradle](build.gradle)
  `desktop-glfw` 模块自己的构建脚本
- [src/main/java/com/libgdx/joystick/glfw/GlfwLauncher.java](src/main/java/com/libgdx/joystick/glfw/GlfwLauncher.java)
  TeaVM GLFW 原生入口

### 自定义原生桥接代码

- [native/src/freetype](native/src/freetype)
  本项目自己维护的 FreeType bridge

里面最重要的是：

- [gdx2d_freetype_bridge.h](native/src/freetype/include/gdx2d_freetype_bridge.h)
  `gdx2d_freetype_*` 的统一声明头
- [gdx2d_freetype_bridge.c](native/src/freetype/src/gdx2d_freetype_bridge.c)
  `gdx2d_freetype_*` 的 C 实现

### 第三方源码

- [native/thirdparty/freetype](native/thirdparty/freetype)
  以 Git submodule 方式接入的 FreeType 上游源码

这部分尽量视为第三方源码，不建议随意改动。  
真正要改项目适配时，优先改 `native/src/freetype` 里的 bridge。

如果刚 clone 仓库后这个目录是空的，需要执行：

```powershell
git submodule update --init --recursive
```

### 生成产物目录

- `build/dist/glfw`
  TeaVM 生成的 GLFW 原生产物目录

其中重点有：

- `build/dist/glfw/CMakeLists.txt`
  生成后又经过本项目脚本补丁处理的 CMake 文件
- `build/dist/glfw/app_debug.bat`
  Windows Debug 构建脚本
- `build/dist/glfw/c/release/app_debug.exe`
  当前 Windows Debug 可执行文件
- `build/dist/glfw/c/external_cpp`
  生成过程中同步进去的外部 C/C++ 依赖与桥接代码

注意：

- `build/dist/...` 是生成目录
- 不建议手工长期维护这里的文件
- 需要长期维护的源码应放在 `native/src` 或 `native/thirdparty`

## 当前 FreeType 状态

当前 `desktop-glfw` 模块已经接入了：

- 本地接管的 `gdx-freetype` Java 源码
- TeaVM 风格的 `FreeType.java` bridge 改造
- 本地 `gdx2d_freetype_*` C bridge
- `freetype` 源码子工程

目前结论：

- Windows 下已经可以 `build`
- 原生程序已经可以成功拉起窗口
- 但编译阶段仍有一部分 `gdx2d_freetype_*` 相关 warning 尚未完全清理

也就是说，这条链已经“可用”，但还在持续做工程化收口。

## 调试建议

如果只是想快速看效果：

- 优先跑 `:desktop-lwjgl3` 模块(debug调试也建议这个模块)

如果是验证 TeaVM GLFW 原生链：

- 优先跑 `:desktop-glfw:gdx_teavm_glfw_build`
- 如果需要运行，再跑 `:desktop-glfw:gdx_teavm_glfw_run`

如果 `build` 失败，优先检查：

1. 是否有残留的 `app_debug.exe` 正在运行
2. Visual Studio 的 `cmake` / `MSBuild` 路径是否存在
3. `native/src/freetype` 与 `native/thirdparty/freetype` 是否完整

## 一句话总结

`desktop-glfw` 模块是“TeaVM 生成 C + 本地 C/C++ bridge + MSVC 构建”的原生桌面目标，不是普通的 JVM 启动模块。  
正确姿势是通过 `gdx_teavm_glfw_*` 任务来生成、构建和运行。
