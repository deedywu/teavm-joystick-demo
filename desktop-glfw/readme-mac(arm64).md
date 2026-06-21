# `desktop-glfw` Setup on macOS arm64

**English** | [中文](<readme-mac(arm64).zh-CN.md>)

`desktop-glfw` first turns the Java code into C with TeaVM, then builds a native executable with CMake and the platform graphics libraries.

Do not run [GlfwLauncher.java](src/main/java/com/libgdx/joystick/glfw/GlfwLauncher.java) directly from the IDE. Use these Gradle tasks instead:

- `:desktop-glfw:gdx_teavm_glfw_generate`
- `:desktop-glfw:gdx_teavm_glfw_build`
- `:desktop-glfw:gdx_teavm_glfw_run`

The steps below assume you are already on Apple Silicon and that `brew`, `java`, and `javac` are available.

## 1. Install Xcode Command Line Tools

Install the compiler toolchain first:

```bash
xcode-select --install
```

Verify it after installation:

```bash
clang --version
xcode-select -p
```

## 2. Install build dependencies

```bash
brew install cmake pkg-config glfw glew
```

Verify them after installation:

```bash
cmake --version
pkg-config --version
brew list --versions cmake pkg-config glfw glew
find /opt/homebrew -path '*/glfw3Config.cmake' | head
find /opt/homebrew -path '*/glew-config.cmake' | head
```

If any of these packages are missing, the CMake step can fail.

## 3. Initialize the FreeType submodule

Run this after cloning:

```bash
git submodule update --init --recursive
```

After that, this directory should contain files:

```text
desktop-glfw/native/thirdparty/freetype
```

If it is empty, CMake will fail in the `add_subdirectory(...)` step.

## 4. Make `gradlew` executable

```bash
chmod +x ./gradlew
```

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

If you only want to verify the native build first:

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

### 1. `./gradlew: permission denied`

```bash
chmod +x ./gradlew
```

### 2. `glfw3Config.cmake` cannot be found

```bash
brew install glfw
find /opt/homebrew -path '*/glfw3Config.cmake' | head
```

### 3. GLEW cannot be found

```bash
brew install glew pkg-config
find /opt/homebrew -path '*/glew-config.cmake' | head
```

### 4. `thirdparty/freetype` does not exist

```bash
git submodule update --init --recursive
```

## Note

For everyday gameplay debugging, `:desktop-lwjgl3` is usually the better option.  
`desktop-glfw` is better when you want to verify the TeaVM Native GLFW pipeline itself.
