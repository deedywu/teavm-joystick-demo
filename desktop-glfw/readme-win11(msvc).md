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

## 3. Prepare the FreeType source cache

The GLFW Gradle tasks automatically prepare the latest FreeType source tree on first use.
If you want to prefetch it explicitly, run:

```powershell
.\gradlew.bat :desktop-glfw:freetype_sync_source
```

By default the cached copy is stored under:

```text
.gradle\desktop-glfw\freetype
```

After the task succeeds, the source is also synced into:

```text
desktop-glfw\native\thirdparty\freetype
```

To force a fresh download instead, add `-PglfwFreetypeForceDownload=true`.
If you want to remove the synced project copy, run:

```powershell
.\gradlew.bat :desktop-glfw:freetype_clean_source
```

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
- `.gradle\desktop-glfw\freetype` for the cached downloaded FreeType source
- `desktop-glfw\native\thirdparty` only if you intentionally keep a local FreeType checkout

## Common Problems

### 1. The FreeType source cache could not be prepared

Typical symptom: the first GLFW build fails before CMake starts compiling.

Fix:

```powershell
.\gradlew.bat :desktop-glfw:freetype_sync_source --info
```

If you want to discard the cached copy and download the latest revision again:

```powershell
.\gradlew.bat :desktop-glfw:freetype_clean_cache :desktop-glfw:freetype_sync_source -PglfwFreetypeForceDownload=true
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
