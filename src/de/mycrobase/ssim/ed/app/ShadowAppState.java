package de.mycrobase.ssim.ed.app;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import com.jme3.shadow.BasicShadowRenderer;

import de.mycrobase.ssim.ed.util.TempVars;

public class ShadowAppState extends BasicAppState {

    private static final float UpdateInterval = 30f; // in seconds
    
    // exists only while AppState is attached
    private BasicShadowRenderer bsr;
    
    public ShadowAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        bsr = new BasicShadowRenderer(getApp().getAssetManager(), 256);
        updateLightDir();
        getApp().getViewPort().addProcessor(bsr);
    }
    
    @Override
    protected void intervalUpdate() {
        updateLightDir();
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getApp().getViewPort().removeProcessor(bsr);
        
        bsr = null;
    }
    
    private void updateLightDir() {
        TempVars vars = TempVars.get();
        
        Vector3f sunPosition = getSkyAppState().getSun().getSunPosition(vars.vect1);
        sunPosition.negateLocal();
        bsr.setDirection(sunPosition);
        
        vars.release();
    }
    
    private SkyAppState getSkyAppState() {
        return getState(SkyAppState.class);
    }
}
