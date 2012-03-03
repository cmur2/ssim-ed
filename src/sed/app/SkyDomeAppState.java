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
    
    private static final Logger logger = Logger.getLogger(SkyDomeAppState.class);
    private static final float UpdateInterval = 30f; // in seconds
    
 // exists only while AppState is attached
    private Geometry geom;
    private SkyBoxTexture skyBoxTexture;
    
    public SkyDomeAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        geom = new Geometry("SkyDome");
        
        Material mat = new Material(getApp().getAssetManager(), "shaders/SkyDome.j3md");
        //mat.getAdditionalRenderState().setWireframe(true);
        mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        
        skyBoxTexture = new SkyBoxTexture(getSkyAppState().getSkyGradient(), getApp().getExecutor());
        skyBoxTexture.update();
        mat.setTexture("SkyBox", skyBoxTexture);
        
        geom.setMaterial(mat);
        
        //Box s = new Box(Vector3f.ZERO, 1, 1, 1);
        SkyDome s = new SkyDome(getSkyAppState().getHemisphereRadius(), 2f, 2f);
        geom.setMesh(s);
        
        getSkyAppState().getSkyNode().attachChild(geom);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getSkyAppState().getSkyNode().detachChild(geom);
        
        geom = null;
        skyBoxTexture = null;
    }
    
    @Override
    protected void intervalUpdate() {
        skyBoxTexture.update();
    }
    
    private SkyAppState getSkyAppState() {
        return getState(SkyAppState.class);
    }
}
