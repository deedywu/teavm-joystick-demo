package com.libgdx.joystick.web;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

final class TeaVMPaths {
    private TeaVMPaths() {
    }

    static Path moduleDir() {
        Path startDir = Paths.get("").toAbsolutePath().normalize();
        for (Path path = startDir; path != null; path = path.getParent()) {
            if (isTeaVMModule(path)) {
                return path;
            }

            // 有时会从项目根目录启动，这里顺手把根目录下的 web-teavm 模块也兼容掉。
            Path webModuleDir = path.resolve("web-teavm");
            if (isTeaVMModule(webModuleDir)) {
                return webModuleDir;
            }
        }

        throw new IllegalStateException("没有找到 web-teavm 模块，起始目录是：" + startDir);
    }

    static Path distDir() {
        return moduleDir().resolve("build").resolve("dist");
    }

    static Path webAppDir() {
        return distDir().resolve("webapp");
    }

    static Path assetsDir() {
        return moduleDir().getParent().resolve("assets");
    }

    private static boolean isTeaVMModule(Path path) {
        return path != null
            && "web-teavm".equals(String.valueOf(path.getFileName()))
            && Files.isRegularFile(path.resolve("build.gradle"));
    }
}
