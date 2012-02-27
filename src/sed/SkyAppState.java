package sed;

import org.apache.log4j.Logger;

import sed.sky.SkyBoxTexture;
import sed.sky.SkyDome;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.scene.Geometry;

public class SkyAppState extends AbstractAppState {
    
    // TODO: realize SkyDome and Sun as background geometry in jme? Bucket.Sky
    
    public static final float HemisphereRadius = 1000f;
    
    private static final Logger logger = Logger.getLogger(SkyAppState.class);
    private static final float UpdateInterval = 30f; // in seconds
    
    private float time = 0;
    
    // exists only while AppState is living
    private Main app;
    private Geometry geom;
    private SkyBoxTexture skyBoxTexture;
    
    public SkyAppState() {
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        app = (Main) baseApp;
        
        float timeOfDay = app.getSimClock().hourTime();
        
        geom = new Geometry("SkyDome");
        
        Material mat = new Material(app.getAssetManager(), "shaders/Sky.j3md");
        //mat.getAdditionalRenderState().setWireframe(true);
        mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        
        skyBoxTexture = new SkyBoxTexture(app.getSkyGradient());
        skyBoxTexture.update();
        mat.setTexture("SkyBox", skyBoxTexture);
        
        geom.setMaterial(mat);
        
        //Box s = new Box(Vector3f.ZERO, 1, 1, 1);
        SkyDome s = new SkyDome(HemisphereRadius, 2f, 2f);
        geom.setMesh(s);
        
        app.getSkyNode().attachChild(geom);
    }
    
    @Override
    public void update(float dt) {
        if(time >= UpdateInterval) {
            time -= UpdateInterval;
            logger.debug("Redraw sky");

            skyBoxTexture.update();
        }
        time += dt;
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        app.getSkyNode().detachChild(geom);
        
        app = null;
        geom = null;
        skyBoxTexture = null;
    }
}
