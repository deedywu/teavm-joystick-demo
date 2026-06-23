# libGDX Joystick Demo

**English** | [中文](README.zh-CN.md)

This repository contains a small joystick indicator demo built with `libGDX`. It can run as a regular desktop application and can also be built for the web and for TeaVM Native GLFW targets.

## Tech Stack

- Java 17+
- Gradle Wrapper
- libGDX 1.14.2
- gdx-controllers 2.2.4
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
- Supports mouse, keyboard, and gamepad/controller input

### Run the web version

```powershell
.\gradlew.bat web-teavm:run
```

Notes:

- Build entry: `com.libgdx.joystick.web.TeaVMBuilder`
- Web launcher: `com.libgdx.joystick.web.TeaVMLauncher`
- Local preview URL: `http://localhost:8089`
- Supports mouse, keyboard, and browser Gamepad API input

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
- FreeType source is prepared automatically before the GLFW native tasks run
- Optional prefetch task: `:desktop-glfw:freetype_sync_source`
- Cached download location: `.gradle/desktop-glfw/freetype`
- Synced project source location: `desktop-glfw/native/thirdparty/freetype`
- Optional cleanup for the synced project source: `:desktop-glfw:freetype_clean_source`
- Do not run `com.libgdx.joystick.glfw.GlfwLauncher` directly from the IDE
- For day-to-day gameplay debugging, `:desktop-lwjgl3` is usually the better choice
- Supports mouse, keyboard, and native GLFW gamepad input
- Windows setup guide: [readme-win11(msvc).md](<desktop-glfw/readme-win11(msvc).md>)
- WSL2 Ubuntu 24.04 setup guide: [readme-wsl2(ubuntu2404).md](<desktop-glfw/readme-wsl2(ubuntu2404).md>)
- macOS arm64 setup guide: [readme-mac(arm64).md](<desktop-glfw/readme-mac(arm64).md>)

### About the `desktop-glfw` module

`desktop-glfw` is not a regular JVM desktop launcher. It is a TeaVM Native GLFW build pipeline.

Its main responsibilities are:

- Turn the game logic in `core` into C code with TeaVM
- Feed the generated C code, the local FreeType bridge, the latest FreeType source prepared by Gradle, and platform-native dependencies into CMake
- Produce a runnable native desktop executable

Recommended usage:

- Do not treat `com.libgdx.joystick.glfw.GlfwLauncher` as a normal Java `main()`
- Drive this module through `desktop-glfw:gdx_teavm_glfw_generate`, `desktop-glfw:gdx_teavm_glfw_build`, and `desktop-glfw:gdx_teavm_glfw_run`
- Prefetch or refresh the FreeType cache with `desktop-glfw:freetype_sync_source` and `desktop-glfw:freetype_clean_cache` when needed
- `desktop-glfw:freetype_sync_source` also syncs the prepared source into `desktop-glfw/native/thirdparty/freetype`
- Platform-specific setup guides are available for Windows 11 + MSVC, WSL2 Ubuntu 24.04, and macOS arm64

## Project Structure

- `core`: core game logic
- `core` also reads the standard `Controllers` API directly for controller state
- `desktop-lwjgl3`: regular desktop launcher
- `web-teavm`: web build and preview module
- `desktop-glfw`: TeaVM Native GLFW desktop module
- `assets`: textures, fonts, and other assets

## What It Does

The demo draws a joystick base at the center of the screen. It now acts as a small input tester:

- Mouse / touch dragging rotates the arrow in real time
- Keyboard input is shown in the on-screen event text
- Gamepad / controller input is supported on desktop, web, and TeaVM Native GLFW builds
- The bottom HUD shows the current I/O source, connected controller name, and the latest input event

## Common Commands

```powershell
.\gradlew.bat build
.\gradlew.bat desktop-lwjgl3:run
.\gradlew.bat web-teavm:run
.\gradlew.bat :desktop-glfw:gdx_teavm_glfw_build
```
