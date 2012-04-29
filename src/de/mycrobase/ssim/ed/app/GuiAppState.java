package de.mycrobase.ssim.ed.app;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;

public class GuiAppState extends BasicAppState {
    
    public static final String INPUT_MAPPING_SPEED_UP = "SED_SpeedUp";
    public static final String INPUT_MAPPING_SPEED_DOWN = "SED_SpeedDown";
    
    private static final float UpdateInterval = 1f; // in seconds
    
    private BitmapText clockLabel;
    private BitmapText speedLabel;
    private InputHandler handler;
    
    public GuiAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        BitmapFont font = getApp().getAssetManager().loadFont("Interface/Fonts/Console.fnt");
        
        clockLabel = new BitmapText(font);
        updateClockLabel();
        getApp().getGuiNode().attachChild(clockLabel);
        
        speedLabel = new BitmapText(font);
        updateSpeedLabel();
        getApp().getGuiNode().attachChild(speedLabel);
        
        handler = new InputHandler();
        getApp().getInputManager().addMapping(INPUT_MAPPING_SPEED_UP, new KeyTrigger(KeyInput.KEY_F10));
        getApp().getInputManager().addListener(handler, INPUT_MAPPING_SPEED_UP);
        getApp().getInputManager().addMapping(INPUT_MAPPING_SPEED_DOWN, new KeyTrigger(KeyInput.KEY_F9));
        getApp().getInputManager().addListener(handler, INPUT_MAPPING_SPEED_DOWN);
    }
    
    @Override
    protected void intervalUpdate() {
        updateClockLabel();
        updateSpeedLabel();
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getApp().getGuiNode().detachChild(clockLabel);
        getApp().getGuiNode().detachChild(speedLabel);
        getApp().getInputManager().deleteMapping(INPUT_MAPPING_SPEED_UP);
        getApp().getInputManager().deleteMapping(INPUT_MAPPING_SPEED_DOWN);
        getApp().getInputManager().removeListener(handler);
        
        clockLabel = null;
        speedLabel = null;
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
    
    private class InputHandler implements ActionListener {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if(!isPressed) {
                return;
            }
            if(name.equals(INPUT_MAPPING_SPEED_UP)) {
                getApp().setSpeed(getApp().getSpeed()*2f);
            } else if(name.equals(INPUT_MAPPING_SPEED_DOWN)) {
                getApp().setSpeed(getApp().getSpeed()*.5f);
            }
        }
    }
}
