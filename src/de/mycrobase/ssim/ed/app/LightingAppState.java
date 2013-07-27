package de.mycrobase.ssim.ed.app;


import org.apache.log4j.Logger;

import ssim.util.MathExt;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.shadow.EdgeFilteringMode;

import de.mycrobase.ssim.ed.util.TempVars;
import de.mycrobase.ssim.ed.util.Util;
import de.mycrobase.ssim.ed.weather.Weather;

/**
 * <b>Higher layer</b> {@link AppState} responsible for setting up scene
 * lighting and shadows.
 * 
 * @author cn
 */
public class LightingAppState extends BasicAppState {
    
    private static final Logger logger = Logger.getLogger(LightingAppState.class);
    
    private static final float UpdateInterval = 30f; // in seconds
    
    // exists only while AppState is attached
    private DirectionalLight sunLight;
    private AmbientLight envLight;
    private DirectionalLightShadowRenderer shadowRenderer;
    
    private int shadowTexSize;
    private int shadowNumSplits;
    
    public LightingAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        sunLight = new DirectionalLight();
        updateSunLight();
        getApp().getRootNode().addLight(sunLight);
        
        envLight = new AmbientLight();
        updateEnvLight();
        getApp().getRootNode().addLight(envLight);
        
        evalSettings();
        logger.info(String.format("Shadow map texture size: %d", shadowTexSize));
        logger.info(String.format("Number of shadow maps: %d", shadowNumSplits));
        
        shadowRenderer = new DirectionalLightShadowRenderer(
            getApp().getAssetManager(), shadowTexSize, shadowNumSplits);
        shadowRenderer.setLight(sunLight);
        shadowRenderer.setEdgeFilteringMode(EdgeFilteringMode.Dither);
        updateShadows();
        getApp().getViewPort().addProcessor(shadowRenderer);
    }
    
    @Override
    protected void intervalUpdate(float dt) {
        updateSunLight();
        updateEnvLight();
        updateShadows();
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getApp().getViewPort().removeProcessor(shadowRenderer);
        getApp().getRootNode().removeLight(envLight);
        getApp().getRootNode().removeLight(sunLight);
        
        sunLight = null;
        envLight = null;
    }
    
    private void updateSunLight() {
        TempVars vars = TempVars.get();
        
        ColorRGBA sunLightColor = getSkyAppState().getSkyGradient().getSunLightColor(vars.color1);
        sunLight.setColor(sunLightColor);
        
        Vector3f sunPosition = getSkyAppState().getSun().getSunPosition(vars.vect1);
        sunPosition.negateLocal();
        sunLight.setDirection(sunPosition);
        
        vars.release();
    }
    
    private void updateEnvLight() {
        TempVars vars = TempVars.get();
        
        Vector3f v = getState(WeatherAppState.class).getWeather().getVec3("sky.light");
        ColorRGBA envColor = Util.setTo(vars.color1, v, 1f);
        envLight.setColor(envColor);
        
        vars.release();
    }
    
    private void updateShadows() {
        // range 0 to 1
        float baseIntensity = getSkyAppState().getSkyGradient().getShadowBaseIntensity();
        // range -1 (totally overcast) to +1 (completely clear) 
        float cloudInfluence = (getWeather().getFloat("cloud.cover")/255f)*2f - 1f;
        // combine
        baseIntensity += cloudInfluence * 0.3f;
        shadowRenderer.setShadowIntensity(MathExt.clamp01(baseIntensity));
    }

    private void evalSettings() {
        int detailLevel = getApp().getSettingsManager().getInteger("engine.detail.level");
        switch(detailLevel) {
            case 0: {
                shadowTexSize = 256; shadowNumSplits = 1;
                break;
            }
            case 1: {
                shadowTexSize = 512; shadowNumSplits = 2;
                break;
            }
            case 2: {
                shadowTexSize = 1024; shadowNumSplits = 3;
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
