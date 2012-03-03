package sed.app;

import sed.Util;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
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
    
    private Vector2f sunAngles;
    private Vector3f sunPosition;
    private float[] sunColorArray;
    private ColorRGBA sunColor;
    
    private ColorRGBA envColor;
    
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
        sunPosition = getSkyAppState().getSun().getSunPosition(sunPosition);
        
        sunAngles = getSkyAppState().getSun().getSunAngles(sunAngles);
        float thetaDeg = (float) Math.toDegrees(sunAngles.y);
        if(thetaDeg > getSkyAppState().getNightThetaMax()) {
            sunLight.setColor(getSkyAppState().getNightSunColor());
        } else {
            if(sunColor == null) {
                sunColor = new ColorRGBA();
            }
            sunColorArray = getSkyAppState().getSkyGradient().getSkyColor(sunPosition, sunColorArray);
            Util.setTo(sunColor, sunColorArray);
            sunLight.setColor(sunColor);
        }
        
        sunPosition.negateLocal();
        sunLight.setDirection(sunPosition);
    }
    
    private void updateEnvLight() {
        Vector3f v = getApp().getWeather().getVec3("sky.light");
        if(envColor == null) {
            envColor = new ColorRGBA();
        }
        Util.setTo(envColor, v, 1f);
        envLight.setColor(envColor);
    }
    
    private SkyAppState getSkyAppState() {
        return getState(SkyAppState.class);
    }
}
