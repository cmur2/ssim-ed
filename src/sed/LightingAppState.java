package sed;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public class LightingAppState extends AbstractAppState {
    
    private static final float UpdateInterval = 30f; // in seconds
    
    // since the sun is below the horizon it should actually be black
    private static final ColorRGBA NightSunColor = ColorRGBA.Black;
    
    // TODO: sync with SkyGradient
    private static final float NightThetaMax = 106f;
    
    private float time = 0;
    
    // exists only while AppState is living
    private Main app;
    private DirectionalLight sunLight;
    private AmbientLight envLight;
    
    private Vector2f sunAngles;
    private Vector3f sunPosition;
    private float[] sunColorArray;
    private ColorRGBA sunColor;
    
    private ColorRGBA envColor;
    
    public LightingAppState() {
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        app = (Main) baseApp;
        
        sunLight = new DirectionalLight();
        updateSunLight();
        app.getRootNode().addLight(sunLight);
        
        envLight = new AmbientLight();
        updateEnvLight();
        app.getRootNode().addLight(envLight);
    }
    
    @Override
    public void update(float dt) {
        if(time > UpdateInterval) {
            time = 0;
            updateSunLight();
            updateEnvLight();
        }
        time += dt;
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        app.getRootNode().removeLight(sunLight);
        app.getRootNode().removeLight(envLight);
        
        app = null;
        sunLight = null;
        envLight = null;
    }
    
    private void updateSunLight() {
        sunPosition = app.getSun().getSunPosition(sunPosition);
        
        sunAngles = app.getSun().getSunAngles(sunAngles);
        float thetaDeg = (float) Math.toDegrees(sunAngles.y);
        if(thetaDeg > NightThetaMax) {
            sunLight.setColor(NightSunColor);
        } else {
            if(sunColor == null) {
                sunColor = new ColorRGBA();
            }
            sunColorArray = app.getSkyGradient().getSkyColor(sunPosition, sunColorArray);
            Util.setTo(sunColor, sunColorArray);
            sunLight.setColor(sunColor);
        }
        
        sunPosition.negateLocal();
        sunLight.setDirection(sunPosition);
    }
    
    private void updateEnvLight() {
        Vector3f v = app.getWeather().getVec3("sky.light");
        if(envColor == null) {
            envColor = new ColorRGBA();
        }
        Util.setTo(envColor, v, 1f);
        envLight.setColor(envColor);
    }
}
