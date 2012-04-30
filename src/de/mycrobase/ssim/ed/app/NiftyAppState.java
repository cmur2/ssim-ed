package de.mycrobase.ssim.ed.app;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.niftygui.NiftyJmeDisplay;

import de.lessvoid.nifty.Nifty;
import de.mycrobase.ssim.ed.GameMode;
import de.mycrobase.ssim.ed.GameModeListener;
import de.mycrobase.ssim.ed.app.screen.BasicScreenAppState;
import de.mycrobase.ssim.ed.app.screen.CreditsScreenAppState;
import de.mycrobase.ssim.ed.app.screen.GameScreenAppState;
import de.mycrobase.ssim.ed.app.screen.IntroScreenAppState;
import de.mycrobase.ssim.ed.app.screen.MainScreenAppState;
import de.mycrobase.ssim.ed.app.screen.OptionsScreenAppState;
import de.mycrobase.ssim.ed.app.screen.PauseScreenAppState;
import de.mycrobase.ssim.ed.app.screen.SingleScreenAppState;

public class NiftyAppState extends BasicAppState implements GameModeListener {
    
    private static final float UpdateInterval = 1f; // in seconds
    
    // exists only while AppState is attached
    private NiftyJmeDisplay niftyDisplay;
    private Nifty nifty;
    
    public NiftyAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        niftyDisplay = new NiftyJmeDisplay(
            getApp().getAssetManager(), getApp().getInputManager(),
            getApp().getAudioRenderer(), getApp().getGuiViewPort());
        
        nifty = niftyDisplay.getNifty();
        //nifty.setDebugOptionPanelColors(true);
        
        //nifty.setGlobalProperties(new Properties() {{
        //}});
        
        // disable verbose Nifty messages *after* Nifty creation
        java.util.logging.Logger.getLogger("de.lessvoid.nifty").setLevel(java.util.logging.Level.WARNING);
        java.util.logging.Logger.getLogger("NiftyInputEventHandlingLog").setLevel(java.util.logging.Level.WARNING);
        
        String[] files = {
            "intro", "main", "credits", "options", "single", "game", "pause"
        };
        BasicScreenAppState[] states = {
            getState(IntroScreenAppState.class),
            getState(MainScreenAppState.class),
            getState(CreditsScreenAppState.class),
            getState(OptionsScreenAppState.class),
            getState(SingleScreenAppState.class),
            getState(GameScreenAppState.class),
            getState(PauseScreenAppState.class)
        };
        
        loadScreens(files, states, "intro");
        
        // manual call to avoid code duplication
        gameModeChanged(null, getApp().getCurrentMode());
        
        getApp().addGameModeListener(this);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getApp().getGuiViewPort().removeProcessor(niftyDisplay);
        //nifty.exit();
        
        niftyDisplay = null;
        nifty = null;
    }
    
    @Override
    public void gameModeChanged(GameMode oldMode, GameMode newMode) {
        // hide Nifty GUI in running mode (but input remains active, see
        // GameScreenAppState)
        if(newMode == GameMode.Running) {
            if(getApp().getGuiViewPort().getProcessors().contains(niftyDisplay)) {
                getApp().getGuiViewPort().removeProcessor(niftyDisplay);
            }
        } else {
            if(!getApp().getGuiViewPort().getProcessors().contains(niftyDisplay)) {
                getApp().getGuiViewPort().addProcessor(niftyDisplay);
            }
        }
        
        if(oldMode != null) {
            // implement central screen switch on mode change
            if(oldMode != GameMode.Running && newMode == GameMode.Running) {
                nifty.gotoScreen("game");
            }
            if(oldMode != GameMode.Paused && newMode == GameMode.Paused) {
                nifty.gotoScreen("pause");
            }
            if(oldMode != GameMode.Stopped && newMode == GameMode.Stopped) {
                nifty.gotoScreen("single");
            }
        }
    }
    
    private void loadScreens(String[] files, BasicScreenAppState[] states, String startScreen) {
        nifty.registerScreenController(states);
        //nifty.prepareScreens("fileId"); // not reachable :/
        
        for(String file : files) {
            nifty.addXml(String.format("gui/%s.xml", file));
        }
        
        nifty.gotoScreen(startScreen);
    }
}
