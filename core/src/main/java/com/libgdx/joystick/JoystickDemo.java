package com.libgdx.joystick;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class JoystickDemo extends ApplicationAdapter {
    private static final String APP_TITLE = "方向指示器演示";
    private static final float ARROW_ANGLE_OFFSET = 45f;
    private static final float MIN_VALID_DISTANCE = 4f;
    private static final String FONT_FILE = "ZhuoTeQingYaTi-2.otf";
    private static final String GUIDE_TEXT = "请点击或按住后移动，用来查看方向轮盘的指向变化";

    private final OrthographicCamera camera = new OrthographicCamera();
    private final ScreenViewport viewport = new ScreenViewport(camera);
    private final Vector2 centerPos = new Vector2();
    private final Vector2 touchWorld = new Vector2();
    private final Vector2 dragVector = new Vector2();

    private SpriteBatch batch;
    private Texture centerTexture;
    private Texture arrowTexture;
    private Stage stage;
    private Skin skin;
    private Label guideLabel;
    private BitmapFont guideFont;
    private float centerWidth;
    private float centerHeight;
    private float arrowWidth;
    private float arrowHeight;
    private int activePointer = -1;
    private boolean arrowVisible;
    private float arrowAngle;

    @Override
    public void create() {
        batch = new SpriteBatch();
        centerTexture = loadTexture("center.png");
        arrowTexture = loadTexture("arrow.png");
        setupGuideUi();
        centerWidth = centerTexture.getWidth();
        centerHeight = centerTexture.getHeight();
        arrowWidth = arrowTexture.getWidth();
        arrowHeight = arrowTexture.getHeight();
        Gdx.graphics.setTitle(APP_TITLE);

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.input.setInputProcessor(createInputProcessor());
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.07f, 0.08f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawJoystick();
        batch.end();

        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        stage.getViewport().update(width, height, true);
        // 窗口尺寸变化后，把底座重新放回画面中心。
        centerPos.set(width * 0.5f, height * 0.5f);
        layoutGuideLabel(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        stage.dispose();
        skin.dispose();
        guideFont.dispose();
        centerTexture.dispose();
        arrowTexture.dispose();
    }

    private Texture loadTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private void setupGuideUi() {
        stage = new Stage(new ScreenViewport());
        guideFont = createGuideFont();
        skin = new Skin();
        skin.add("guide-font", guideFont);

        Label.LabelStyle style = new Label.LabelStyle();
        style.font = guideFont;
        skin.add("guide", style);

        guideLabel = new Label(GUIDE_TEXT, skin, "guide");
        guideLabel.setWrap(true);
        guideLabel.setAlignment(Align.center);
        stage.addActor(guideLabel);
    }

    private BitmapFont createGuideFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_FILE));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 26;
        parameter.characters = GUIDE_TEXT + "方向轮盘点击按住后移动，用来查看指向变化，请";
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;

        try {
            return generator.generateFont(parameter);
        } finally {
            generator.dispose();
        }
    }

    private void layoutGuideLabel(int width, int height) {
        float labelWidth = Math.max(220f, Math.min(width - 96f, 720f));
        guideLabel.setWidth(labelWidth);
        guideLabel.pack();
        guideLabel.setSize(labelWidth, guideLabel.getPrefHeight());
        guideLabel.setPosition((width - labelWidth) * 0.5f, Math.max(36f, height * 0.12f));
    }

    private InputProcessor createInputProcessor() {
        return new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (activePointer >= 0) {
                    return false;
                }

                activePointer = pointer;
                refreshArrow(screenX, screenY);
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (pointer != activePointer) {
                    return false;
                }

                refreshArrow(screenX, screenY);
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (pointer != activePointer) {
                    return false;
                }

                releasePointer();
                return true;
            }
        };
    }

    private void drawJoystick() {
        batch.draw(
            centerTexture,
            centerPos.x - centerWidth * 0.5f,
            centerPos.y - centerHeight * 0.5f,
            centerWidth,
            centerHeight
        );

        if (!arrowVisible) {
            return;
        }

        // 箭头贴图从左下角开始绘制，这里直接让它从圆心朝外转出去。
        batch.draw(
            arrowTexture,
            centerPos.x,
            centerPos.y,
            0f,
            0f,
            arrowWidth,
            arrowHeight,
            1f,
            1f,
            arrowAngle,
            0,
            0,
            arrowTexture.getWidth(),
            arrowTexture.getHeight(),
            false,
            false
        );
    }

    private boolean refreshArrow(float screenX, float screenY) {
        touchWorld.set(screenX, screenY);
        viewport.unproject(touchWorld);

        dragVector.set(touchWorld).sub(centerPos);
        if (dragVector.len() < MIN_VALID_DISTANCE) {
            arrowVisible = false;
            return false;
        }

        arrowVisible = true;
        // 箭头素材本身朝右上，旋转时要减去 45 度做一次校正。
        arrowAngle = dragVector.angleDeg() - ARROW_ANGLE_OFFSET;
        return true;
    }

    private void releasePointer() {
        activePointer = -1;
        arrowVisible = false;
        arrowAngle = 0f;
        dragVector.setZero();
    }
}
