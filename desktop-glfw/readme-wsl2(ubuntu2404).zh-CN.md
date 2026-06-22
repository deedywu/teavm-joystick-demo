# WSL2 Ubuntu 24.04 下配置 `desktop-glfw`

[English](<readme-wsl2(ubuntu2404).md>) | **中文**

`desktop-glfw` 会先把 Java 代码通过 TeaVM 生成为 C，再交给 CMake 和系统编译器生成原生程序。

这个模块不要直接在 IDE 里运行 [GlfwLauncher.java](src/main/java/com/libgdx/joystick/glfw/GlfwLauncher.java)。正确入口是 Gradle 任务：

- `:desktop-glfw:gdx_teavm_glfw_generate`
- `:desktop-glfw:gdx_teavm_glfw_build`
- `:desktop-glfw:gdx_teavm_glfw_run`

下面默认你已经在 WSL2 Ubuntu 24.04 终端里，并且 `java` / `javac` 已经可用。

## 1. 安装构建依赖

```bash
sudo apt update
sudo apt install -y \
  git curl unzip \
  build-essential cmake pkg-config \
  libglfw3-dev libglew-dev \
  libgl1-mesa-dev libglu1-mesa-dev \
  libx11-dev libxrandr-dev libxinerama-dev libxcursor-dev libxi-dev libxxf86vm-dev \
  libegl1-mesa-dev libwayland-dev libxkbcommon-dev wayland-protocols \
  mesa-utils
```

这里最关键的几项：

- `build-essential`：`gcc`、`g++`、`make`
- `cmake`：原生构建
- `pkg-config`：查找系统库
- `libglfw3-dev`：GLFW 头文件和 CMake 配置
- `libglew-dev`：GLEW 头文件和库
- `libgl1-mesa-dev` / `libglu1-mesa-dev`：OpenGL 开发文件

装好后确认：

```bash
cmake --version
pkg-config --version
pkg-config --modversion glfw3
pkg-config --modversion glew
```

## 2. 准备 FreeType 源码缓存

GLFW 相关 Gradle 任务会在首次使用时自动准备最新的 FreeType 源码。
如果你想提前拉取，可以单独执行：

```bash
./gradlew :desktop-glfw:freetype_sync_source
```

默认缓存目录在：

```text
.gradle/desktop-glfw/freetype
```

任务成功后，源码也会同步到：

```text
desktop-glfw/native/thirdparty/freetype
```

如果想强制重新下载，额外加上 `-PglfwFreetypeForceDownload=true`。
如果想把同步到项目内的源码清掉，可以执行：

```bash
./gradlew :desktop-glfw:freetype_clean_source
```

## 3. 给 `gradlew` 加执行权限

```bash
chmod +x ./gradlew
```

## 4. 如果要直接弹窗运行，先确认 WSLg 可用

如果你只关心能不能编译，这一步可以跳过。  
如果你要在 WSL2 里直接打开窗口，先检查图形环境：

```bash
echo "$DISPLAY"
echo "$WAYLAND_DISPLAY"
glxinfo -B
```

通常满足下面两点就可以继续：

- `DISPLAY` 或 `WAYLAND_DISPLAY` 不是空的
- `glxinfo -B` 能正常输出 OpenGL 信息

## 5. 运行构建任务

在项目根目录执行：

```bash
./gradlew :desktop-glfw:gdx_teavm_glfw_generate
./gradlew :desktop-glfw:gdx_teavm_glfw_build
./gradlew :desktop-glfw:gdx_teavm_glfw_run
```

说明：

- `gdx_teavm_glfw_generate`：生成 TeaVM C 代码并整理资源
- `gdx_teavm_glfw_build`：调用 CMake 编译原生程序
- `gdx_teavm_glfw_run`：生成、编译并运行程序

如果只想先确认能否编译成功：

```bash
./gradlew :desktop-glfw:gdx_teavm_glfw_build
```

如果要从干净状态重新验证：

```bash
./gradlew clean :desktop-glfw:gdx_teavm_glfw_run
```

## 6. 生成结果在哪

重点目录：

- `desktop-glfw/build/dist/glfw`
- `desktop-glfw/build/dist/glfw/CMakeLists.txt`
- `desktop-glfw/build/dist/glfw/app_debug.sh`
- `desktop-glfw/build/dist/glfw/c/release/app_debug`

`build/dist/glfw` 是生成目录，不建议手工长期改动。真正需要维护的源码在：

- `desktop-glfw/native/src`
- `.gradle/desktop-glfw/freetype`：Gradle 下载缓存的 FreeType 源码
- `desktop-glfw/native/thirdparty`：仅在你刻意保留本地 FreeType checkout 时才需要关注

## 常见问题

### 1. `./gradlew: Permission denied`

```bash
chmod +x ./gradlew
```

### 2. 找不到 `glfw3Config.cmake`

```bash
sudo apt install -y libglfw3-dev
```

### 3. 找不到 GLEW

```bash
sudo apt install -y libglew-dev
```

### 4. FreeType 源码缓存准备失败

```bash
./gradlew :desktop-glfw:freetype_sync_source --info
```

如果想丢弃缓存并重新下载最新版本：

```bash
./gradlew :desktop-glfw:freetype_clean_cache :desktop-glfw:freetype_sync_source -PglfwFreetypeForceDownload=true
```

### 5. 编译成功但窗口打不开

优先检查 WSLg：

```bash
echo "$DISPLAY"
echo "$WAYLAND_DISPLAY"
glxinfo -B
```

如果只是先验证构建链路，可以先停在：

```bash
./gradlew :desktop-glfw:gdx_teavm_glfw_build
```

## 补充

日常调游戏逻辑更适合用 `:desktop-lwjgl3`。  
`desktop-glfw` 更适合验证 TeaVM Native GLFW 这条原生构建链路。
