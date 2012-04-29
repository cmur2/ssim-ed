package de.mycrobase.ssim.ed.app.screen;

import org.apache.log4j.Logger;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;

import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.KeyInputHandler;

public class OptionsScreenAppState extends BasicScreenAppState implements KeyInputHandler {
    
    private static final Logger logger = Logger.getLogger(OptionsScreenAppState.class);
    
    // exists only while AppState is attached
    
    // needed by Nifty
    public OptionsScreenAppState() {
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
            doAbort();
            return true;
        }
        return false;
    }

    @Override
    public String translate(String key) {
        return super.translate("options." + key);
    }
    
    // interact
    
    public void doApply() {
        logger.debug("doApply");
        getNifty().gotoScreen("main");
    }
    
    public void doAbort() {
        logger.debug("doAbort");
        getNifty().gotoScreen("main");
    }
}
