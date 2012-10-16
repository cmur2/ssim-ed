package de.mycrobase.ssim.ed.app;

import org.apache.log4j.Logger;

import ssim.util.MathExt;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.shadow.PssmShadowRenderer.FilterMode;

import de.mycrobase.ssim.ed.util.TempVars;
import de.mycrobase.ssim.ed.weather.Weather;

public class ShadowAppState extends BasicAppState {

    private static final Logger logger = Logger.getLogger(ShadowAppState.class);
    
    private static final float UpdateInterval = 30f; // in seconds
    
    // exists only while AppState is attached
    private PssmShadowRenderer pssm;
    
    private int texSize;
    private int numSplits;
    
    public ShadowAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        evalSettings();
        
        logger.info(String.format("Shadow map texture size: %d", texSize));
        logger.info(String.format("Number of shadow maps: %d", numSplits));
        
        pssm = new PssmShadowRenderer(getApp().getAssetManager(), texSize, numSplits);
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
    
    private void evalSettings() {
        int detailLevel = getApp().getSettingsManager().getInteger("engine.detail.level");
        switch(detailLevel) {
            case 0: {
                texSize = 256; numSplits = 1;
                break;
            }
            case 1: {
                texSize = 512; numSplits = 2;
                break;
            }
            case 2: {
                texSize = 1024; numSplits = 3;
                break;
            }
        }
    }
    
    private Weather getWeather() {
        return getState(WeatherAppState.class).getWeather();
    }
    
    private SkyAppState getSkyAppState() {
        return getState(SkyAppState.class);
    }
}
