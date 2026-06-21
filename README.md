# libGDX Joystick Demo

**English** | [中文](README.zh-CN.md)

This repository contains a small joystick indicator demo built with `libGDX`. It can run as a regular desktop application and can also be built for the web and for TeaVM Native GLFW targets.

## Tech Stack

- Java 17+
- Gradle Wrapper
- libGDX 1.14.2
- LWJGL3 desktop backend
- TeaVM Native GLFW desktop backend
- TeaVM web backend
- FreeType font rendering

## Requirements

- JDK 17 or newer

The project already includes `gradlew` and `gradlew.bat`, so you do not need a separate Gradle installation.

## Quick Start

Run commands from the project root.

### Run the desktop version

```powershell
.\gradlew.bat desktop-lwjgl3:run
```

Notes:

- Launcher class: `com.libgdx.joystick.lwjgl3.Lwjgl3Launcher`
- The desktop window opens with a default size of `960 x 640`

### Run the web version

```powershell
.\gradlew.bat web-teavm:run
```

Notes:

- Build entry: `com.libgdx.joystick.web.TeaVMBuilder`
- Web launcher: `com.libgdx.joystick.web.TeaVMLauncher`
- Local preview URL: `http://localhost:8089`

### Run the TeaVM Native GLFW desktop version

This is a native build pipeline, not a regular JVM launch path.

Common tasks:

- `:desktop-glfw:gdx_teavm_glfw_generate`
- `:desktop-glfw:gdx_teavm_glfw_build`
- `:desktop-glfw:gdx_teavm_glfw_run`

Windows 11 + MSVC:

```powershell
.\gradlew.bat :desktop-glfw:gdx_teavm_glfw_build
```

WSL2 Ubuntu 24.04 / Linux:

```bash
./gradlew :desktop-glfw:gdx_teavm_glfw_build
```

macOS arm64:

```bash
./gradlew :desktop-glfw:gdx_teavm_glfw_build
```

Notes:

- Module directory: `desktop-glfw`
- This module depends on the `desktop-glfw/native/thirdparty/freetype` Git submodule
- If `desktop-glfw/native/thirdparty/freetype` is empty, run `git submodule update --init --recursive`
- Do not run `com.libgdx.joystick.glfw.GlfwLauncher` directly from the IDE
- For day-to-day gameplay debugging, `:desktop-lwjgl3` is usually the better choice
- Windows setup guide: [readme-win11(msvc).md](<desktop-glfw/readme-win11(msvc).md>)
- WSL2 Ubuntu 24.04 setup guide: [readme-wsl2(ubuntu2404).md](<desktop-glfw/readme-wsl2(ubuntu2404).md>)
- macOS arm64 setup guide: [readme-mac(arm64).md](<desktop-glfw/readme-mac(arm64).md>)

### About the `desktop-glfw` module

`desktop-glfw` is not a regular JVM desktop launcher. It is a TeaVM Native GLFW build pipeline.

Its main responsibilities are:

- Turn the game logic in `core` into C code with TeaVM
- Feed the generated C code, the local FreeType bridge, the FreeType source submodule, and platform-native dependencies into CMake
- Produce a runnable native desktop executable

Recommended usage:

- Do not treat `com.libgdx.joystick.glfw.GlfwLauncher` as a normal Java `main()`
- Drive this module through `desktop-glfw:gdx_teavm_glfw_generate`, `desktop-glfw:gdx_teavm_glfw_build`, and `desktop-glfw:gdx_teavm_glfw_run`
- Keep `desktop-glfw/native/thirdparty/freetype` initialized with `git submodule update --init --recursive`
- Platform-specific setup guides are available for Windows 11 + MSVC, WSL2 Ubuntu 24.04, and macOS arm64

## Project Structure

- `core`: core game logic
- `desktop-lwjgl3`: regular desktop launcher
- `web-teavm`: web build and preview module
- `desktop-glfw`: TeaVM Native GLFW desktop module
- `assets`: textures, fonts, and other assets

## What It Does

The demo draws a joystick base at the center of the screen. When you click or drag, the arrow rotates in real time based on the drag direction.

## Common Commands

```powershell
.\gradlew.bat build
.\gradlew.bat desktop-lwjgl3:run
.\gradlew.bat web-teavm:run
.\gradlew.bat :desktop-glfw:gdx_teavm_glfw_build
```
