package de.mycrobase.ssim.ed.app.screen;

import java.util.Locale;

import org.apache.log4j.Logger;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.KeyInputHandler;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.tools.SizeValue;
import de.mycrobase.ssim.ed.mission.Mission;

public class LoadingScreenAppState extends BasicScreenAppState implements KeyInputHandler {

    private static final Logger logger = Logger.getLogger(LoadingScreenAppState.class);
    
    // exists only while AppState is attached
    
    // exists only while Controller is bound
    private Label statusText;
    private Element loadProgress;
    
    // exists only while Screen is visible
    private Mission mission;
    
    // needed by Nifty
    public LoadingScreenAppState() {
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        // ...
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        // ...
    }

    @Override
    public void bind(Nifty nifty, Screen screen) {
        super.bind(nifty, screen);
        
        statusText = getScreen().findNiftyControl("status_text", Label.class);
        loadProgress = getScreen().findElementByName("load_progress");
    }
    
    @Override
    public void onStartScreen() {
        super.onStartScreen();
        
        getApp().doGameInit(mission);
    }
    
    @Override
    public void onEndScreen() {
        super.onEndScreen();
        
        mission = null;
    }
    
    @Override
    public boolean keyEvent(NiftyInputEvent inputEvent) {
        return false;
    }
    
    @Override
    public String translate(String key) {
        return super.translate("loading." + key);
    }

    // interact
    
    public void setMission(Mission mission) {
        if(this.mission != null) {
            throw new UnsupportedOperationException("The selected mission can be only set once!");
        }
        this.mission = mission;
    }
    
    public void setStatusTextKey(String key) {
        statusText.setText(translate("status.text." + key));
    }
    
    public void setProgress(float progress) {
        String width = String.format("%d%%", (int) (progress * 100));
        loadProgress.setConstraintWidth(new SizeValue(width));
        loadProgress.getParent().layoutElements();
    }
}
