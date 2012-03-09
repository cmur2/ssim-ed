package sed.app;

import sed.TempVars;
import sed.Util;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

/**
 * <b>Higher layer</b> {@link AppState} responsible for setting up scene
 * lighting.
 * 
 * @author cn
 */
public class LightingAppState extends BasicAppState {
    
    private static final float UpdateInterval = 30f; // in seconds
    
    // exists only while AppState is attached
    private DirectionalLight sunLight;
    private AmbientLight envLight;
    
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
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getApp().getRootNode().removeLight(sunLight);
        getApp().getRootNode().removeLight(envLight);
        
        sunLight = null;
        envLight = null;
    }
    
    @Override
    protected void intervalUpdate() {
        updateSunLight();
        updateEnvLight();
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
    
    private SkyAppState getSkyAppState() {
        return getState(SkyAppState.class);
    }
}
