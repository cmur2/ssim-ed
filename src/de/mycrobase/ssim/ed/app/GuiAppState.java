package de.mycrobase.ssim.ed.app;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

public class GuiAppState extends BasicAppState {
    
    public static final String INPUT_MAPPING_PAUSE = "SED_Pause";
    public static final String INPUT_MAPPING_SPEED_UP = "SED_SpeedUp";
    public static final String INPUT_MAPPING_SPEED_DOWN = "SED_SpeedDown";
    
    private static final float UpdateInterval = 1f; // in seconds
    
    // exists only while AppState is attached
    private BitmapText clockLabel;
    private BitmapText speedLabel;
    private BitmapText fpsLabel;
    private InputHandler handler;
    
    private float fpsPassedTime;
    private int fpsCounter;
    private int fpsLast;
    
    public GuiAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        BitmapFont font = getApp().getAssetManager().loadFont("Interface/Fonts/Console.fnt");
        
        clockLabel = new BitmapText(font);
        updateClockLabel();
        
        speedLabel = new BitmapText(font);
        updateSpeedLabel();
        
        fpsLabel = new BitmapText(font);
        updateFpsLabel();
        
        if(getApp().getSettingsManager().getBoolean("debug.stats")) {
            getApp().getGuiNode().attachChild(clockLabel);
            getApp().getGuiNode().attachChild(speedLabel);
            getApp().getGuiNode().attachChild(fpsLabel);
        }
        
        handler = new InputHandler();
        getApp().getInputManager().addListener(handler,
            INPUT_MAPPING_SPEED_UP,
            INPUT_MAPPING_SPEED_DOWN,
            INPUT_MAPPING_PAUSE);
    }
    
    @Override
    public void update(float dt) {
        super.update(dt);
        
        fpsPassedTime += dt;
        fpsCounter++;
        
        if(fpsPassedTime >= 1f) {
            fpsLast = (int) (fpsCounter/fpsPassedTime);
            fpsPassedTime = 0f;
            fpsCounter = 0;
        }
    }
    
    @Override
    protected void intervalUpdate(float dt) {
        updateClockLabel();
        updateSpeedLabel();
        updateFpsLabel();
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getApp().getGuiNode().detachChild(clockLabel);
        getApp().getGuiNode().detachChild(speedLabel);
        getApp().getGuiNode().detachChild(fpsLabel);
        getApp().getInputManager().removeListener(handler);
        
        clockLabel = null;
        speedLabel = null;
        fpsLabel = null;
    }
    
    private void updateClockLabel() {
        clockLabel.setText(getState(SimClockAppState.class).getSimClock().mixedTime(true));
        clockLabel.setLocalTranslation(
            getApp().getCamera().getWidth()-clockLabel.getLineWidth()-1,
            clockLabel.getLineHeight(),
            0);
    }
    
    private void updateSpeedLabel() {
        speedLabel.setText(String.format("%.2gx", getApp().getSpeed()));
        speedLabel.setLocalTranslation(
            getApp().getCamera().getWidth()-speedLabel.getLineWidth()-1,
            clockLabel.getLineHeight()+speedLabel.getLineHeight(),
            0);
    }
    
    private void updateFpsLabel() {
        fpsLabel.setText(String.format("FPS: %d", fpsLast));
        fpsLabel.setLocalTranslation(
            0,
            fpsLabel.getLineHeight(),
            0);
    }
    
    private class InputHandler implements ActionListener {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if(!isPressed) {
                return;
            }
            if(name.equals(INPUT_MAPPING_SPEED_UP)) {
                getApp().setSpeed(getApp().getSpeed() * 2.0f);
            }
            else if(name.equals(INPUT_MAPPING_SPEED_DOWN)) {
                getApp().setSpeed(getApp().getSpeed() * 0.5f);
            }
            else if(name.equals(INPUT_MAPPING_PAUSE)) {
                getApp().doGamePause();
            }
        }
    }
}
