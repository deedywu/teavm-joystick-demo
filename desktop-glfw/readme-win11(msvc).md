# Windows 11 Setup for `desktop-glfw` (MSVC)

**English** | [中文](<readme-win11(msvc).zh-CN.md>)

`desktop-glfw` is not a regular JVM desktop launcher. It first turns the Java code into C with TeaVM, then builds a native executable with CMake and MSVC.

Do not run [GlfwLauncher.java](src/main/java/com/libgdx/joystick/glfw/GlfwLauncher.java) directly from the IDE. Use these Gradle tasks instead:

- `:desktop-glfw:gdx_teavm_glfw_generate`
- `:desktop-glfw:gdx_teavm_glfw_build`
- `:desktop-glfw:gdx_teavm_glfw_run`

The steps below assume you are already on Windows 11 and that `git`, `java`, and `javac` are available.

## 1. Install Visual Studio and the C++ toolchain

Install Visual Studio Community with at least these components:

- `Desktop development with C++`
- `MSVC v143/v144 C++ build tools`
- `C++ CMake tools for Windows`
- `Windows 10/11 SDK`

Once installed, these tools must exist:

- `cmake.exe`
- `MSBuild.exe`

The project [build.gradle](build.gradle) already scans common Visual Studio installation directories, so you do not need to hardcode paths.

## 2. Verify CMake and MSBuild

Check the tools from PowerShell:

```powershell
Get-Command cmake
Get-Command msbuild
```

If `Get-Command msbuild` does not find anything, you can also check common installation paths directly:

```powershell
Test-Path "C:\Program Files\Microsoft Visual Studio\18\Community\MSBuild\Current\Bin\MSBuild.exe"
Test-Path "C:\Program Files\Microsoft Visual Studio\18\Community\Common7\IDE\CommonExtensions\Microsoft\CMake\CMake\bin\cmake.exe"
```

If Visual Studio is installed somewhere else, that is still fine. The Gradle script checks the common `Program Files` roots automatically.

## 3. Initialize the FreeType submodule

`desktop-glfw` depends on a FreeType submodule. Run this after cloning:

```powershell
git submodule update --init --recursive
```

After that, this directory should contain files:

```text
desktop-glfw\native\thirdparty\freetype
```

If the directory is empty, the CMake step will fail.

## 4. Run the build tasks

Run these commands from the project root:

```powershell
.\gradlew.bat :desktop-glfw:gdx_teavm_glfw_generate
.\gradlew.bat :desktop-glfw:gdx_teavm_glfw_build
.\gradlew.bat :desktop-glfw:gdx_teavm_glfw_run
```

What they do:

- `gdx_teavm_glfw_generate`: generates TeaVM C code and copies resources
- `gdx_teavm_glfw_build`: builds the native executable with CMake and MSBuild
- `gdx_teavm_glfw_run`: generates, builds, and runs the executable

If you only want to verify the native build first:

```powershell
.\gradlew.bat :desktop-glfw:gdx_teavm_glfw_build
```

If you want to retry from a clean state:

```powershell
.\gradlew.bat clean :desktop-glfw:gdx_teavm_glfw_run
```

## 5. Where the generated files go

Important paths:

- `desktop-glfw\build\dist\glfw`
- `desktop-glfw\build\dist\glfw\CMakeLists.txt`
- `desktop-glfw\build\dist\glfw\app_debug.bat`
- `desktop-glfw\build\dist\glfw\c\release\app_debug.exe`

`build\dist\glfw` is generated output. Do not treat it as long-term source code. The files you should actually maintain are under:

- `desktop-glfw\native\src`
- `desktop-glfw\native\thirdparty`

## Common Problems

### 1. The submodule was not initialized

Typical symptom: CMake says `thirdparty/freetype` is missing.

Fix:

```powershell
git submodule update --init --recursive
```

### 2. `cmake` or `MSBuild` cannot be found

Double-check that the Visual Studio installation includes the C++ workload and CMake tools.

You can verify again with:

```powershell
Get-Command cmake
Get-Command msbuild
```

### 3. `app_debug.exe` is still running

If the previous native process is still alive, rebuilds can fail.

Kill it first:

```powershell
taskkill /IM app_debug.exe /F
```

### 4. Running `main()` directly from the IDE fails

This module is not meant to be launched as a normal Java desktop app. Use the Gradle tasks instead.

## Note

If you mainly want to debug game logic, `:desktop-lwjgl3` is usually more convenient.  
`desktop-glfw` is better when you want to verify the TeaVM Native GLFW pipeline itself.
