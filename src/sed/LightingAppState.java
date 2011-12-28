package sed;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public class LightingAppState extends AbstractAppState {
    
    // NightSun is the moon actually
    private static final ColorRGBA NightSunColor = new ColorRGBA(0.1f, 0.1f, 0.1f, 1f);
    
    // TODO: sync with SkyGradient
    private static final float NightThetaMax = 100f;
    
    private float time = 0;
    
    // exists only while AppState is living
    private Main app;
    private DirectionalLight sunLight;
    private Vector2f sunAngles;
    private Vector3f sunPosition;
    private float[] sunColorArray;
    private ColorRGBA sunColor;
    
    public LightingAppState() {
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        app = (Main) baseApp;
        
        sunLight = new DirectionalLight();
        updateSunLight();
        app.getRootNode().addLight(sunLight);
    }
    
    @Override
    public void update(float dt) {
        if(time > 30f) {
            time = 0;
            updateSunLight();
        }
        time += dt;
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        app.getRootNode().removeLight(sunLight);
        
        app = null;
        sunLight = null;
    }
    
    private void updateSunLight() {
        sunPosition = app.getSun().getSunPosition(sunPosition);
        
        sunAngles = app.getSun().getSunAngles(sunAngles);
        float thetaDeg = (float) Math.toDegrees(sunAngles.y);
        if(thetaDeg > NightThetaMax) {
            sunLight.setColor(NightSunColor);
        } else {
            if(sunColorArray == null) {
                sunColorArray = new float[3];
            }
            if(sunColor == null) {
                sunColor = new ColorRGBA();
            }
            app.getSkyGradient().getSkycolor(sunColorArray, sunPosition.x, sunPosition.y, sunPosition.z);
            sunColor.set(sunColorArray[0], sunColorArray[1], sunColorArray[2], 1f);
            sunLight.setColor(sunColor);
        }
        
        sunPosition.negateLocal();
        sunLight.setDirection(sunPosition);
    }
}
