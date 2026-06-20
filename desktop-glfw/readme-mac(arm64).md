# macOS arm64 下配置 `desktop-glfw`

`desktop-glfw` 会先把 Java 代码通过 TeaVM 生成为 C，再交给 CMake 和系统图形库生成原生程序。

这个模块不要直接在 IDE 里运行 [GlfwLauncher.java](src/main/java/com/libgdx/joystick/glfw/GlfwLauncher.java)。正确入口是 Gradle 任务：

- `:desktop-glfw:gdx_teavm_glfw_generate`
- `:desktop-glfw:gdx_teavm_glfw_build`
- `:desktop-glfw:gdx_teavm_glfw_run`

下面默认你已经在 Apple Silicon 机器上，并且 `brew`、`java`、`javac` 都已经可用。

## 1. 安装 Xcode Command Line Tools

先装编译工具链：

```bash
xcode-select --install
```

装完后确认：

```bash
clang --version
xcode-select -p
```

## 2. 安装构建依赖

```bash
brew install cmake pkg-config glfw glew
```

装好后确认：

```bash
cmake --version
pkg-config --version
brew list --versions cmake pkg-config glfw glew
find /opt/homebrew -path '*/glfw3Config.cmake' | head
find /opt/homebrew -path '*/glew-config.cmake' | head
```

这几个包缺一项，后面的 CMake 阶段都可能失败。

## 3. 初始化 FreeType submodule

首次 clone 后执行：

```bash
git submodule update --init --recursive
```

执行完后，下面这个目录里应该有内容：

```text
desktop-glfw/native/thirdparty/freetype
```

如果这里是空目录，CMake 会在 `add_subdirectory(...)` 阶段报错。

## 4. 给 `gradlew` 加执行权限

```bash
chmod +x ./gradlew
```

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

如果你只想先确认编译是否通过：

```bash
./gradlew :desktop-glfw:gdx_teavm_glfw_build
```

如果想从干净状态完整验证：

```bash
./gradlew clean :desktop-glfw:gdx_teavm_glfw_run
```

## 6. 生成结果在哪

重点目录：

- `desktop-glfw/build/dist/glfw`
- `desktop-glfw/build/dist/glfw/CMakeLists.txt`
- `desktop-glfw/build/dist/glfw/app_debug.sh`
- `desktop-glfw/build/dist/glfw/c/release/app_debug`

`build/dist/glfw` 是生成目录，不建议长期手工改动。真正需要维护的源码在：

- `desktop-glfw/native/src`
- `desktop-glfw/native/thirdparty`

## 常见问题

### 1. `./gradlew: permission denied`

```bash
chmod +x ./gradlew
```

### 2. 找不到 `glfw3Config.cmake`

```bash
brew install glfw
find /opt/homebrew -path '*/glfw3Config.cmake' | head
```

### 3. 找不到 GLEW

```bash
brew install glew pkg-config
find /opt/homebrew -path '*/glew-config.cmake' | head
```

### 4. `thirdparty/freetype` 不存在

```bash
git submodule update --init --recursive
```

## 补充

平时调游戏逻辑更适合用 `:desktop-lwjgl3`。  
`desktop-glfw` 更适合验证 TeaVM Native GLFW 这条原生构建链路。
