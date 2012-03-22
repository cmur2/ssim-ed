package sed.app;

import sed.sky.RainParticles;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial.CullHint;

public class RainAppState extends BasicAppState {
    
    private static final float UpdateInterval = 10f; // in seconds
    
    // exists only while AppState is attached
    private RainParticles rain;
    private Geometry geom;
    
    public RainAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        // 200, 5.5f, new Color3f(0.4f, 0.4f, 0.5f)S
        rain = new RainParticles(200, 100f);
        rain.setDropLength(5.5f); // in m
        rain.setDropLengthVar(0.5f); // in m
        rain.setDropColor(new ColorRGBA(0.4f, 0.4f, 0.5f, 1.0f));
        rain.setDropColorVar(new ColorRGBA(0.1f, 0.1f, 0.1f, 0.0f));
        rain.setDropVelocity(30f); // in m/s
        rain.setDropVelocityVar(5f); // in m/s
        rain.setMinY(0f);
        rain.setMaxY(200f);
        rain.initFirstDrops();
        
        geom = new Geometry("RainParticles", rain);
        
        Material mat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setBoolean("VertexColor", true);
        
        geom.setMaterial(mat);
        geom.setCullHint(CullHint.Never);
        
        getApp().getRootNode().attachChild(geom);
    }
    
    @Override
    public void update(float dt) {
        // we need the timed functionality too
        super.update(dt);
        
        rain.update(dt);
    }
    
    @Override
    protected void intervalUpdate() {
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getApp().getRootNode().detachChild(geom);
        
        rain = null;
        geom = null;
    }
}
