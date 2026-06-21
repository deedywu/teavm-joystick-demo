# `desktop-glfw` Setup on WSL2 Ubuntu 24.04

**English** | [中文](<readme-wsl2(ubuntu2404).zh-CN.md>)

`desktop-glfw` first turns the Java code into C with TeaVM, then builds a native executable with CMake and the system compiler.

Do not run [GlfwLauncher.java](src/main/java/com/libgdx/joystick/glfw/GlfwLauncher.java) directly from the IDE. Use these Gradle tasks instead:

- `:desktop-glfw:gdx_teavm_glfw_generate`
- `:desktop-glfw:gdx_teavm_glfw_build`
- `:desktop-glfw:gdx_teavm_glfw_run`

The steps below assume you are already inside a WSL2 Ubuntu 24.04 terminal and that `java` and `javac` are available.

## 1. Install build dependencies

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

The most important packages here are:

- `build-essential`: `gcc`, `g++`, and `make`
- `cmake`: native build system
- `pkg-config`: library discovery
- `libglfw3-dev`: GLFW headers and CMake config
- `libglew-dev`: GLEW headers and library
- `libgl1-mesa-dev` / `libglu1-mesa-dev`: OpenGL development files

Verify the tools after installation:

```bash
cmake --version
pkg-config --version
pkg-config --modversion glfw3
pkg-config --modversion glew
```

## 2. Initialize the FreeType submodule

Run this after cloning:

```bash
git submodule update --init --recursive
```

After that, this directory should contain files:

```text
desktop-glfw/native/thirdparty/freetype
```

If it is empty, CMake will fail in the `add_subdirectory(...)` step.

## 3. Make `gradlew` executable

```bash
chmod +x ./gradlew
```

## 4. Check WSLg if you want to open a native window

If you only care about compilation, you can skip this section.  
If you want to run the native window inside WSL2, check the graphics environment first:

```bash
echo "$DISPLAY"
echo "$WAYLAND_DISPLAY"
glxinfo -B
```

Usually you want both of these to be true:

- `DISPLAY` or `WAYLAND_DISPLAY` is not empty
- `glxinfo -B` prints valid OpenGL information

## 5. Run the build tasks

Run these commands from the project root:

```bash
./gradlew :desktop-glfw:gdx_teavm_glfw_generate
./gradlew :desktop-glfw:gdx_teavm_glfw_build
./gradlew :desktop-glfw:gdx_teavm_glfw_run
```

What they do:

- `gdx_teavm_glfw_generate`: generates TeaVM C code and copies resources
- `gdx_teavm_glfw_build`: builds the native executable with CMake
- `gdx_teavm_glfw_run`: generates, builds, and runs the executable

If you only want to verify the native build:

```bash
./gradlew :desktop-glfw:gdx_teavm_glfw_build
```

If you want to retry from a clean state:

```bash
./gradlew clean :desktop-glfw:gdx_teavm_glfw_run
```

## 6. Where the generated files go

Important paths:

- `desktop-glfw/build/dist/glfw`
- `desktop-glfw/build/dist/glfw/CMakeLists.txt`
- `desktop-glfw/build/dist/glfw/app_debug.sh`
- `desktop-glfw/build/dist/glfw/c/release/app_debug`

`build/dist/glfw` is generated output. Do not treat it as long-term source code. The files you should actually maintain are under:

- `desktop-glfw/native/src`
- `desktop-glfw/native/thirdparty`

## Common Problems

### 1. `./gradlew: Permission denied`

```bash
chmod +x ./gradlew
```

### 2. `glfw3Config.cmake` cannot be found

```bash
sudo apt install -y libglfw3-dev
```

### 3. GLEW cannot be found

```bash
sudo apt install -y libglew-dev
```

### 4. `thirdparty/freetype` does not exist

```bash
git submodule update --init --recursive
```

### 5. The build succeeds but the window does not open

Check WSLg first:

```bash
echo "$DISPLAY"
echo "$WAYLAND_DISPLAY"
glxinfo -B
```

If you only want to verify the build pipeline, it is completely fine to stop at:

```bash
./gradlew :desktop-glfw:gdx_teavm_glfw_build
```

## Note

For everyday gameplay debugging, `:desktop-lwjgl3` is usually the better option.  
`desktop-glfw` is better when you want to verify the TeaVM Native GLFW pipeline itself.
