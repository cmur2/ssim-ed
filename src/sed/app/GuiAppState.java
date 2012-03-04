package sed.app;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;

public class GuiAppState extends BasicAppState {
    
    private static final float UpdateInterval = 1f; // in seconds
    
    private BitmapText clockLabel;
    
    // TODO: Metrics App State?
    
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
    }
    
    @Override
    protected void intervalUpdate() {
        updateClockLabel();
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getApp().getGuiNode().detachChild(clockLabel);
        
        clockLabel = null;
    }
    
    private void updateClockLabel() {
        clockLabel.setText(getApp().getSimClock().mixedTime(true));
        clockLabel.setLocalTranslation(
            getApp().getCamera().getWidth()-clockLabel.getLineWidth()-1,
            clockLabel.getLineHeight(),
            0);
    }
}
