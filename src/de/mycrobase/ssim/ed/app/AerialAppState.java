package de.mycrobase.ssim.ed.app;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;

import de.mycrobase.ssim.ed.util.TempVars;

public class AerialAppState extends BasicAppState {

    private static final float UpdateInterval = 1f; // in seconds
    
    // exists only while AppState is attached
    private Vector3f fogColor;
    private float fogDensity;
    
    public AerialAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        fogColor = new Vector3f();
        fogDensity = 0f;
    }
    
    @Override
    protected void intervalUpdate() {
        TempVars vars = TempVars.get();
        // use sky color from south because it's the place where sun influence is biggest
        float[] color = getState(SkyAppState.class).getSkyGradient().getSkyColor(0,0,-1, vars.float1);
        //System.out.println(java.util.Arrays.toString(color));
        fogColor.set(color[0], color[1], color[2]);
        vars.release();
        
        float turbidity = getState(WeatherAppState.class).getWeather().getFloat("air.turbidity");
        // on clear air retain more of the original color with fog than on
        // foggy conditions (simple implementation):
        float targetFogFactor = turbidity <= 3 ? 0.75f : 0.25f;
        
        // HowTo fogFactor:
        //   1.0 - full original color
        //   0.0 - full fog color
        
        float maxDist = getState(SkyAppState.class).getHemisphereRadius();
        fogDensity = getFogDensity(targetFogFactor, maxDist);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        fogColor = null;
        fogDensity = 0f;
    }
    
    /**
     * Calculates the necessary distance value so that EXP2 fog has the given
     * targetFogFactor at the given distance.
     * 
     * @param targetFogFactor wished fog factor
     * @param maxDist at this distance
     * @return necessary fog density
     */
    private float getFogDensity(float targetFogFactor, float maxDist) {
        return (float) (
            Math.sqrt(1.0 / (maxDist*maxDist)) *
            Math.sqrt(Math.log(1.0 / targetFogFactor))
            );
    }
    
    // public API
    
    public Vector3f getFogColor() {
        return fogColor;
    }

    public float getFogDensity() {
        return fogDensity;
    }
}
