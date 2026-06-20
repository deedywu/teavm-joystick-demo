package com.libgdx.joystick.web;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication;
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetInstance;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoader;
import com.github.xpenatan.gdx.teavm.backends.web.assetloader.AssetLoaderListener;

final class TeaVMWebApplication extends WebApplication {
    TeaVMWebApplication(ApplicationListener appListener, WebApplicationConfiguration config) {
        super(appListener, config);
    }

    @Override
    protected void initGdxLibrary() {
        super.initGdxLibrary();
        initFreeTypeLibrary();
    }

    private void initFreeTypeLibrary() {
        AssetLoader assetLoader = AssetInstance.getLoaderInstance();
        addInitQueue();
        assetLoader.loadScript("freetype.js", new AssetLoaderListener<>() {
            @Override
            public void onSuccess(String url, String result) {
                subtractInitQueue();
            }

            @Override
            public void onFailure(String url) {
                throw new GdxRuntimeException("FreeType script failed to load: " + url);
            }
        });
    }
}
