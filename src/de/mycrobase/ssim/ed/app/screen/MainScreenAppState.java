package de.mycrobase.ssim.ed.app.screen;

import org.apache.log4j.Logger;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;

import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.KeyInputHandler;

public class MainScreenAppState extends BasicScreenAppState implements KeyInputHandler {
    
    private static final Logger logger = Logger.getLogger(MainScreenAppState.class);
    
    // exists only while AppState is attached
    
    // needed by Nifty
    public MainScreenAppState() {
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
            doQuit();
            return true;
        }
        return false;
    }
    
    @Override
    public String translate(String key) {
        return super.translate("main." + key);
    }
    
    // interact

    public void doStart() {
        logger.debug("doStart");
        getNifty().gotoScreen("single");
    }

    public void doOptions() {
        logger.debug("doOptions");
        getNifty().gotoScreen("options");
    }

    public void doCredits() {
        logger.debug("doCredits");
        getNifty().gotoScreen("credits");
    }
    
    public void doQuit() {
        logger.debug("doQuit");
        getApp().stop();
    }
}
