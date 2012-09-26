package de.mycrobase.ssim.ed.app;

import ssim.util.MathExt;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Spatial;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.shadow.PssmShadowRenderer.FilterMode;

import de.mycrobase.ssim.ed.util.TempVars;
import de.mycrobase.ssim.ed.weather.Weather;

public class ShadowAppState extends BasicAppState {

    private static final float UpdateInterval = 30f; // in seconds
    private static final int TexSize = 1024;
    private static final int NumSplits = 3;
    
    // exists only while AppState is attached
    private PssmShadowRenderer pssm;
    
    public ShadowAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        pssm = new PssmShadowRenderer(getApp().getAssetManager(), TexSize, NumSplits);
        // dithering is cooool :)
        pssm.setFilterMode(FilterMode.Dither);
        updateShadows();
        
        getApp().getViewPort().addProcessor(pssm);
    }
    
    @Override
    protected void intervalUpdate(float dt) {
        updateShadows();
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getApp().getViewPort().removeProcessor(pssm);
        
        pssm = null;
    }
    
    private void updateShadows() {
        TempVars vars = TempVars.get();
        
        Vector3f sunPosition = getSkyAppState().getSun().getSunPosition(vars.vect1);
        sunPosition.negateLocal();
        pssm.setDirection(sunPosition);
        
        // range 0 to 1
        float baseIntensity = getSkyAppState().getSkyGradient().getShadowBaseIntensity();
        // range -1 (totally overcast) to +1 (completely clear) 
        float cloudInfluence = (getWeather().getFloat("cloud.cover")/255f)*2f - 1f;
        // combine
        baseIntensity += cloudInfluence * 0.3f;
        pssm.setShadowIntensity(MathExt.clamp01(baseIntensity));
        
        vars.release();
    }
    
    private Weather getWeather() {
        return getState(WeatherAppState.class).getWeather();
    }
    
    private SkyAppState getSkyAppState() {
        return getState(SkyAppState.class);
    }
}
