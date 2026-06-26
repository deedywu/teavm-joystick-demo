package com.libgdx.joystick;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerMapping;
import com.badlogic.gdx.controllers.Controllers;
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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.Locale;

public class JoystickDemo extends ApplicationAdapter {
    private static final String APP_TITLE = "Joystick Indicator Demo";
    private static final float ARROW_ANGLE_OFFSET = 45f;
    private static final float MIN_VALID_DISTANCE = 4f;
    private static final float CONTROLLER_DEADZONE = 0.25f;
    private static final float CONTROLLER_EVENT_DEADZONE = 0.2f;
    private static final float DPAD_VALUE = 1f;
    private static final long TRANSIENT_EVENT_DURATION_MS = 320L;
    private static final String FONT_FILE = "ZhuoTeQingYaTi-2.otf";
    private static final String GUIDE_TEXT = "Click or drag to move, or connect a controller and use the left stick or D-pad";
    private static final String HELLO_TEXT = "你好";
    private static final String IO_STATUS_IDLE = "Input: idle";
    private static final String TOUCH_STATUS_ACTIVE = "Input: mouse/touch";
    private static final String KEYBOARD_STATUS_ACTIVE = "Input: keyboard";
    private static final String CONTROLLER_STATUS_ACTIVE = "Input: controller";
    private static final String EVENT_TEXT_IDLE = "Event: none";
    private static final String CONTROLLER_NAME_DISCONNECTED = "Controller: disconnected";
    private static final String CONTROLLER_NAME_PREFIX = "Controller: ";

    private final OrthographicCamera camera = new OrthographicCamera();
    private final ScreenViewport viewport = new ScreenViewport(camera);
    private final Vector2 centerPos = new Vector2();
    private final Vector2 touchWorld = new Vector2();
    private final Vector2 dragVector = new Vector2();
    private final Vector2 controllerVector = new Vector2();

    private SpriteBatch batch;
    private Texture centerTexture;
    private Texture arrowTexture;
    private Stage stage;
    private Table layoutTable;
    private Skin skin;
    private Label guideLabel;
    private Label helloLabel;
    private BitmapFont guideFont;
    private float centerWidth;
    private float centerHeight;
    private float arrowWidth;
    private float arrowHeight;
    private int activePointer = -1;
    private boolean arrowVisible;
    private float arrowAngle;
    private boolean controllerActive;
    private Label statusLabel;
    private Label eventLabel;
    private String lastStatusText = "";
    private String lastEventText = "";
    private String ioStatus = IO_STATUS_IDLE;
    private String controllerStatus = CONTROLLER_NAME_DISCONNECTED;
    private String pointerEventText = EVENT_TEXT_IDLE;
    private long pointerEventExpiresAtMillis;
    private String keyboardEventText = EVENT_TEXT_IDLE;
    private long keyboardEventExpiresAtMillis;
    private String controllerEventText = EVENT_TEXT_IDLE;
    private String currentEventText = EVENT_TEXT_IDLE;

    public JoystickDemo() {
    }

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
        refreshControllerState();
        updateDisplayState();

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

        helloLabel = new Label(HELLO_TEXT, skin, "guide");
        helloLabel.setWrap(true);
        helloLabel.setAlignment(Align.center);

        statusLabel = new Label("", skin, "guide");
        statusLabel.setWrap(true);
        statusLabel.setAlignment(Align.center);

        eventLabel = new Label("", skin, "guide");
        eventLabel.setWrap(true);
        eventLabel.setAlignment(Align.center);

        layoutTable = new Table();
        layoutTable.setFillParent(true);
        layoutTable.defaults().padLeft(48f).padRight(48f);
        layoutTable.add(guideLabel).growX().top().padTop(24f);
        layoutTable.row();
        layoutTable.add(helloLabel).growX().top().padTop(8f);
        layoutTable.row();
        layoutTable.add().expandY().fillY();
        layoutTable.row();
        layoutTable.add(statusLabel).growX().bottom().padBottom(8f);
        layoutTable.row();
        layoutTable.add(eventLabel).growX().bottom().padBottom(20f);
        stage.addActor(layoutTable);

        refreshStatusText();
    }

    private BitmapFont createGuideFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_FILE));
        try {
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 26;
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS
                + GUIDE_TEXT
                + IO_STATUS_IDLE
                + TOUCH_STATUS_ACTIVE
                + KEYBOARD_STATUS_ACTIVE
                + CONTROLLER_STATUS_ACTIVE
                + EVENT_TEXT_IDLE
                + CONTROLLER_NAME_DISCONNECTED
                + CONTROLLER_NAME_PREFIX
                + HELLO_TEXT
                + "connected left stick d-pad input idle event mouse touch keyboard controller angle down up wheel move button drag disconnected";
            parameter.minFilter = Texture.TextureFilter.Linear;
            parameter.magFilter = Texture.TextureFilter.Linear;
            return generator.generateFont(parameter);
        } finally {
            generator.dispose();
        }
    }

    private void layoutGuideLabel(int width, int height) {
        if (layoutTable != null) {
            layoutTable.invalidateHierarchy();
        }
    }

    private InputProcessor createInputProcessor() {
        return new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (activePointer >= 0) {
                    return false;
                }

                activePointer = pointer;
                setPointerEvent("Event: mouse/touch down button=" + mouseButtonName(button)
                    + " x=" + screenX + " y=" + screenY);
                refreshTouchArrow(screenX, screenY);
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (pointer != activePointer) {
                    return false;
                }

                refreshTouchArrow(screenX, screenY);
                setPointerEvent("Event: mouse/touch drag x=" + screenX + " y=" + screenY
                    + " angle=" + format(dragVector.angleDeg()));
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (pointer != activePointer) {
                    return false;
                }

                setPointerEvent("Event: mouse/touch up button=" + mouseButtonName(button)
                    + " x=" + screenX + " y=" + screenY);
                releasePointer();
                return true;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                setPointerEvent("Event: mouse move x=" + screenX + " y=" + screenY);
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                setPointerEvent("Event: mouse wheel dx=" + format(amountX) + " dy=" + format(amountY));
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                setKeyboardEvent("Event: key down " + Input.Keys.toString(keycode));
                return false;
            }

            @Override
            public boolean keyUp(int keycode) {
                setKeyboardEvent("Event: key up " + Input.Keys.toString(keycode));
                return false;
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

    private boolean refreshTouchArrow(float screenX, float screenY) {
        touchWorld.set(screenX, screenY);
        viewport.unproject(touchWorld);

        dragVector.set(touchWorld).sub(centerPos);
        if (dragVector.len() < MIN_VALID_DISTANCE) {
            arrowVisible = false;
            return false;
        }

        applyArrowDirection(dragVector);
        return true;
    }

    private void releasePointer() {
        activePointer = -1;
        if (!controllerActive) {
            arrowVisible = false;
            arrowAngle = 0f;
        }
        dragVector.setZero();
    }

    private void refreshControllerState() {
        Controller controller = getActiveController();
        if (controller == null) {
            controllerStatus = CONTROLLER_NAME_DISCONNECTED;
            controllerActive = false;
            controllerEventText = EVENT_TEXT_IDLE;
            controllerVector.setZero();
            if (activePointer < 0) {
                arrowVisible = false;
                arrowAngle = 0f;
            }
            return;
        }

        String controllerName = controller.getName();
        controllerStatus = controllerName == null || controllerName.isBlank()
            ? CONTROLLER_NAME_PREFIX + "connected"
            : CONTROLLER_NAME_PREFIX + controllerName;

        String controllerEvent = readControllerEvent(controller, controllerVector);
        boolean hasDirection = controllerVector.len() >= CONTROLLER_DEADZONE;
        controllerActive = controllerEvent != null && !controllerEvent.isBlank();
        controllerEventText = controllerActive ? controllerEvent : EVENT_TEXT_IDLE;
        if (hasDirection && activePointer < 0) {
            applyArrowDirection(controllerVector);
        } else if (activePointer < 0) {
            arrowVisible = false;
            arrowAngle = 0f;
        }
    }

    private void applyArrowDirection(Vector2 direction) {
        arrowVisible = true;
        // 箭头素材本身朝右上，旋转时要减去 45 度做一次校正。
        arrowAngle = direction.angleDeg() - ARROW_ANGLE_OFFSET;
    }

    private Controller getActiveController() {
        Controller current = Controllers.getCurrent();
        if (current != null && current.isConnected()) {
            return current;
        }

        for (Controller controller : Controllers.getControllers()) {
            if (controller != null && controller.isConnected()) {
                return controller;
            }
        }
        return null;
    }

    private String readControllerEvent(Controller controller, Vector2 outDirection) {
        ControllerMapping mapping = controller.getMapping();
        float x = readAxis(controller, mapping.axisLeftX);
        float y = -readAxis(controller, mapping.axisLeftY);

        if (Math.abs(x) >= CONTROLLER_EVENT_DEADZONE || Math.abs(y) >= CONTROLLER_EVENT_DEADZONE) {
            outDirection.set(x, y);
            return "Controller event: left stick x=" + format(x) + " y=" + format(y) + " angle=" + format(angleOf(x, y));
        }

        float dpadX = readButtonAxis(controller, mapping.buttonDpadLeft, mapping.buttonDpadRight);
        float dpadY = readButtonAxis(controller, mapping.buttonDpadDown, mapping.buttonDpadUp);
        if (dpadX != 0f || dpadY != 0f) {
            outDirection.set(dpadX, dpadY);
            return "Controller event: D-pad x=" + format(dpadX) + " y=" + format(dpadY)
                + " angle=" + format(angleOf(dpadX, dpadY));
        }

        outDirection.setZero();
        StringBuilder pressedButtons = new StringBuilder();
        appendPressedButton(pressedButtons, controller, mapping.buttonA, "A");
        appendPressedButton(pressedButtons, controller, mapping.buttonB, "B");
        appendPressedButton(pressedButtons, controller, mapping.buttonX, "X");
        appendPressedButton(pressedButtons, controller, mapping.buttonY, "Y");
        appendPressedButton(pressedButtons, controller, mapping.buttonL1, "L1");
        appendPressedButton(pressedButtons, controller, mapping.buttonR1, "R1");
        appendPressedButton(pressedButtons, controller, mapping.buttonBack, "BACK");
        appendPressedButton(pressedButtons, controller, mapping.buttonStart, "START");
        appendPressedButton(pressedButtons, controller, mapping.buttonLeftStick, "L3");
        appendPressedButton(pressedButtons, controller, mapping.buttonRightStick, "R3");

        float leftTrigger = readAxis(controller, mapping.buttonL2);
        float rightTrigger = readAxis(controller, mapping.buttonR2);
        if (Math.abs(leftTrigger) >= CONTROLLER_EVENT_DEADZONE || Math.abs(rightTrigger) >= CONTROLLER_EVENT_DEADZONE) {
            if (pressedButtons.length() > 0) {
                pressedButtons.append(", ");
            }
            if (Math.abs(leftTrigger) >= CONTROLLER_EVENT_DEADZONE) {
                pressedButtons.append("L2=").append(format(leftTrigger));
            }
            if (Math.abs(rightTrigger) >= CONTROLLER_EVENT_DEADZONE) {
                if (Math.abs(leftTrigger) >= CONTROLLER_EVENT_DEADZONE) {
                    pressedButtons.append(", ");
                }
                pressedButtons.append("R2=").append(format(rightTrigger));
            }
        }

        if (pressedButtons.length() > 0) {
            return "Controller event: buttons " + pressedButtons;
        }
        return "";
    }

    private float readAxis(Controller controller, int axisIndex) {
        if (axisIndex < 0 || axisIndex >= controller.getAxisCount()) {
            return 0f;
        }
        return controller.getAxis(axisIndex);
    }

    private float readButtonAxis(Controller controller, int negativeButton, int positiveButton) {
        float value = 0f;
        if (negativeButton >= 0 && controller.getButton(negativeButton)) {
            value -= DPAD_VALUE;
        }
        if (positiveButton >= 0 && controller.getButton(positiveButton)) {
            value += DPAD_VALUE;
        }
        return value;
    }

    private void appendPressedButton(StringBuilder builder, Controller controller, int buttonIndex, String name) {
        if (buttonIndex < 0 || !controller.getButton(buttonIndex)) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(name);
    }

    private void updateDisplayState() {
        long now = TimeUtils.millis();
        boolean pointerActive = activePointer >= 0;
        boolean keyboardActive = Gdx.input.isKeyPressed(Input.Keys.ANY_KEY);
        boolean pointerRecent = now <= pointerEventExpiresAtMillis && !EVENT_TEXT_IDLE.equals(pointerEventText);
        boolean keyboardRecent = now <= keyboardEventExpiresAtMillis && !EVENT_TEXT_IDLE.equals(keyboardEventText);

        if (pointerActive) {
            ioStatus = TOUCH_STATUS_ACTIVE;
            currentEventText = pointerEventText;
        } else if (controllerActive) {
            ioStatus = CONTROLLER_STATUS_ACTIVE;
            currentEventText = controllerEventText;
        } else if (keyboardActive) {
            ioStatus = KEYBOARD_STATUS_ACTIVE;
            currentEventText = keyboardEventText;
        } else if (pointerRecent) {
            ioStatus = TOUCH_STATUS_ACTIVE;
            currentEventText = pointerEventText;
        } else if (keyboardRecent) {
            ioStatus = KEYBOARD_STATUS_ACTIVE;
            currentEventText = keyboardEventText;
        } else {
            ioStatus = IO_STATUS_IDLE;
            currentEventText = EVENT_TEXT_IDLE;
        }

        if (!pointerActive && !pointerRecent) {
            pointerEventText = EVENT_TEXT_IDLE;
        }
        if (!keyboardActive && !keyboardRecent) {
            keyboardEventText = EVENT_TEXT_IDLE;
        }

        refreshStatusText();
    }

    private void refreshStatusText() {
        if (statusLabel == null || eventLabel == null) {
            return;
        }

        String statusText = ioStatus + "\n" + controllerStatus;
        if (!statusText.equals(lastStatusText)) {
            lastStatusText = statusText;
            statusLabel.setText(statusText);
        }
        if (!currentEventText.equals(lastEventText)) {
            lastEventText = currentEventText;
            eventLabel.setText(currentEventText);
        }
        layoutGuideLabel(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void setPointerEvent(String eventText) {
        pointerEventText = eventText;
        pointerEventExpiresAtMillis = TimeUtils.millis() + TRANSIENT_EVENT_DURATION_MS;
    }

    private void setKeyboardEvent(String eventText) {
        keyboardEventText = eventText;
        keyboardEventExpiresAtMillis = TimeUtils.millis() + TRANSIENT_EVENT_DURATION_MS;
    }

    private String format(float value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private float angleOf(float x, float y) {
        return new Vector2(x, y).angleDeg();
    }

    private String mouseButtonName(int button) {
        if (button == Input.Buttons.LEFT) {
            return "LEFT";
        }
        if (button == Input.Buttons.RIGHT) {
            return "RIGHT";
        }
        if (button == Input.Buttons.MIDDLE) {
            return "MIDDLE";
        }
        return String.valueOf(button);
    }
}
