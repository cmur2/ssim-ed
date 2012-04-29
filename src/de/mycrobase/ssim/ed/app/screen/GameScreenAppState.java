package de.mycrobase.ssim.ed.app.screen;

import org.apache.log4j.Logger;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;

import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.KeyInputHandler;

public class GameScreenAppState extends BasicScreenAppState implements KeyInputHandler {

    private static final Logger logger = Logger.getLogger(GameScreenAppState.class);
    
    // exists only while AppState is attached
    
    // needed by Nifty
    public GameScreenAppState() {
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
    public boolean keyEvent(NiftyInputEvent inputEvent) {
        if(inputEvent == NiftyInputEvent.Escape) {
            doPause();
            return true;
        }
        return false;
    }
    
    @Override
    public String translate(String key) {
        return super.translate("game." + key);
    }

    // interact
    
    public void doPause() {
        logger.debug("doPause");
        getApp().doGamePause();
    }
}
