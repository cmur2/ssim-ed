package de.mycrobase.ssim.ed.app.screen;

import org.apache.log4j.Logger;

import de.altimos.util.translator.Translator;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.screen.Screen;
import de.lessvoid.nifty.screen.ScreenController;
import de.mycrobase.ssim.ed.app.BasicAppState;

public class BasicScreenAppState extends BasicAppState implements ScreenController {

    private static final Logger logger = Logger.getLogger(BasicScreenAppState.class);
    private static final Translator translator = Translator.getGlobal();
    
    private Nifty nifty;
    private Screen screen;
    
    public BasicScreenAppState() {
        super();
    }
    
    public BasicScreenAppState(float intervalTime) {
        super(intervalTime);
    }
    
    @Override
    public void bind(Nifty nifty, Screen screen) {
        logger.debug(String.format("Bind app state %s to screen %s",
            getClass().getSimpleName(), screen));
        
        if(!isInitialized()) {
            logger.warn("App state was not initialized before bound to screen!");
        }
        
        // keep references to nifty and screen
        this.nifty = nifty;
        this.screen = screen;
    }

    @Override
    public void onStartScreen() {
    }

    @Override
    public void onEndScreen() {
    }
    
    // i18n API
    
    public String translate(String key) {
        return translator.translate(key);
    }

    // internal API
    
    protected Nifty getNifty() {
        return nifty;
    }
    
    protected Screen getScreen() {
        return screen;
    }
}
