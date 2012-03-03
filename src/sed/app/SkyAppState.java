package sed.app;

import sed.SurfaceCameraControl;
import sed.sky.SkyGradient;
import sed.sky.Sun;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;

/**
 * <b>Base layer</b> {@link AppState} providing a sky {@link Node}
 * (to attach all sky related things at), a {@link SkyGradient} and
 * a {@link Sun}.
 * 
 * @author cn
 */
public class SkyAppState extends BasicAppState {
    
    // TODO: Realize SkyDome and Sun as background geometry in jME via Bucket.Sky?
    
    private static final float HemisphereRadius = 1000f;
    
    private static final float UpdateInterval = 1f; // in seconds
    
    // exists only while AppState is attached
    private Node skyNode;
    private Sun sun;
    private SkyGradient skyGradient;
    
    public SkyAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        skyNode = new Node("SkyNode");
        skyNode.setCullHint(CullHint.Never);
        getApp().getRootNode().attachChild(skyNode);
        
        // since the constructors shouldn't do anything related to processing
        // the delayed update should work
        sun = new Sun(getApp().getSimClock(), getApp().getMission());
        skyGradient = new SkyGradient(sun);
        intervalUpdate();
        
        skyNode.addControl(new SurfaceCameraControl(getApp().getCamera()));
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
    }
    
    @Override
    protected void intervalUpdate() {
        sun.update();
        skyGradient.setTurbidity(getApp().getWeather().getFloat("sky.turbidity"));
        skyGradient.update();
    }
    
    // public API
    
    public Node getSkyNode() {
        return skyNode;
    }
    
    public Sun getSun() {
        return sun;
    }
    
    public SkyGradient getSkyGradient() {
        return skyGradient;
    }

    public float getNightThetaMax() {
        return SkyGradient.NightThetaMax;
    }

    public ColorRGBA getNightSunColor() {
        return SkyGradient.NightSunColor;
    }

    public float getHemisphereRadius() {
        return HemisphereRadius;
    }
}
