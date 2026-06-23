package com.libgdx.joystick.web;

import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import com.github.xpenatan.gdx.teavm.backends.web.dom.impl.WebWindow;
import com.libgdx.joystick.JoystickDemo;
import org.teavm.classlib.PlatformDetector;

public class TeaVMLauncher {
    public static void main(String[] args) {
        if (!PlatformDetector.isTeaVM()) {
            TeaVMDesktopLauncher.launch();
            return;
        }

        WebApplicationConfiguration config = new WebApplicationConfiguration("canvas");
        // Web 端优先使用 CSS 像素做布局，避免高 DPI 屏幕上整体内容过小。
        config.usePhysicalPixels = false;
        config.useGL30 = true;
        config.width = 0;
        config.height = 0;

        WebWindow window = WebWindow.get();
        window.getDocument().setTitle("方向指示器网页演示");

        new TeaVMWebApplication(new JoystickDemo(), config);
    }
}
