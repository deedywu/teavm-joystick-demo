package com.libgdx.joystick.glfw;

import com.github.xpenatan.gdx.teavm.backends.glfw.GLFWApplication;
import com.github.xpenatan.gdx.teavm.backends.glfw.GLFWApplicationConfiguration;
import com.libgdx.joystick.JoystickDemo;

public class GlfwLauncher {
    public static void main(String[] args) {
        // 这是 TeaVM GLFW 的原生入口，不适合像普通 Java main 一样直接在 JVM 中运行。
        // 请通过 `:desktop-glfw:gdx_teavm_glfw_run` 先生成并构建原生程序（win上需要msvc），再启动对应可执行文件。
        GLFWApplicationConfiguration configuration = new GLFWApplicationConfiguration();
        configuration.setTitle("方向指示器演示");
        configuration.setWindowedMode(960, 640);
        configuration.setOpenGLEmulation(GLFWApplicationConfiguration.GLEmulation.GL20, 3, 2);
        configuration.setForegroundFPS(60);

        new GLFWApplication(new JoystickDemo(), configuration);
    }
}
