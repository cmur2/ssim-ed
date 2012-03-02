package sed.app;

import org.apache.log4j.Logger;

import sed.sky.SkyBoxTexture;
import sed.sky.SkyDome;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.scene.Geometry;

public class SkyDomeAppState extends BasicAppState {
    
    // TODO: realize SkyDome and Sun as background geometry in jme? Bucket.Sky
    
    public static final float HemisphereRadius = 1000f;
    
    private static final Logger logger = Logger.getLogger(SkyDomeAppState.class);
    private static final float UpdateInterval = 30f; // in seconds
    
    private float time = 0;
    
    // exists only while AppState is living
    private Geometry geom;
    private SkyBoxTexture skyBoxTexture;
    
    public SkyDomeAppState() {
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        geom = new Geometry("SkyDome");
        
        Material mat = new Material(getApp().getAssetManager(), "shaders/SkyDome.j3md");
        //mat.getAdditionalRenderState().setWireframe(true);
        mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        
        skyBoxTexture = new SkyBoxTexture(getState(SkyAppState.class).getSkyGradient(), getApp().getExecutor());
        skyBoxTexture.update();
        mat.setTexture("SkyBox", skyBoxTexture);
        
        geom.setMaterial(mat);
        
        //Box s = new Box(Vector3f.ZERO, 1, 1, 1);
        SkyDome s = new SkyDome(HemisphereRadius, 2f, 2f);
        geom.setMesh(s);
        
        getState(SkyAppState.class).getSkyNode().attachChild(geom);
    }
    
    @Override
    public void update(float dt) {
        if(time >= UpdateInterval) {
            time -= UpdateInterval;
            skyBoxTexture.update();
        }
        time += dt;
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getState(SkyAppState.class).getSkyNode().detachChild(geom);
        
        geom = null;
        skyBoxTexture = null;
    }
}
