package com.libgdx.joystick.web;

import com.github.xpenatan.gdx.teavm.backends.shared.config.AssetFileHandle;
import com.github.xpenatan.gdx.teavm.backends.shared.config.compiler.TeaCompiler;
import com.github.xpenatan.gdx.teavm.backends.web.config.backend.WebBackend;
import org.teavm.vm.TeaVMOptimizationLevel;

public class TeaVMBuilder {
    private static final int ONE_MB = 1 << 20;

    public static void main(String[] args) {
        WebBackend backend = new WebBackend()
            .setWebAssembly(true)
            .setHtmlTitle("方向指示器网页演示")
            .setStartJettyAfterBuild(false);

        TeaCompiler compiler = new TeaCompiler(backend);
        compiler.addAssets(new AssetFileHandle(TeaVMPaths.assetsDir().toString()));
        // 网页版体量不大，但给宽松一点的内存能少踩很多莫名其妙的构建问题。
        compiler.setMaxHeapSize((int) (1.8 * 1024 * ONE_MB));
        compiler.setMinDirectBuffersSize(1024 * ONE_MB);
        compiler.setOptimizationLevel(TeaVMOptimizationLevel.FULL);
        compiler.setDebugInformationGenerated(false);
        compiler.setMainClass(TeaVMLauncher.class.getName());
        compiler.build(TeaVMPaths.distDir().toFile());
    }
}
