package sed;

import org.apache.log4j.Logger;

import sed.sky.SkyBoxTexture;
import sed.sky.SkyDome;
import sed.sky.SkyGradient;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

public class SkyAppState extends AbstractAppState {
    
    private static final Logger logger = Logger.getLogger(SkyAppState.class);
    
    private float time = 0;
    
    // exists only while AppState is living
    private Main app;
    private SkyGradient skyGradient;
    private Geometry geom;
    private SkyBoxTexture skyBoxTexture;
    
    public SkyAppState() {
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        app = (Main) baseApp;
        
        skyGradient = new SkyGradient();
        skyGradient.setTurbidity(app.weatherController.getFloat("sky.turbidity"));
        skyGradient.updateSunPosition(11.00f, 180, 36.4f, (int) (11.8f / 15f), 11.8f);
        
        geom = new Geometry("SkyDome");
        
        Material mat = new Material(app.getAssetManager(), "shaders/Sky.j3md");
        //mat.getAdditionalRenderState().setWireframe(true);
        mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        
        skyBoxTexture = new SkyBoxTexture(skyGradient);
        skyBoxTexture.update();
        mat.setTexture("SkyBox", skyBoxTexture);
        
        geom.setMaterial(mat);
        
        //Box s = new Box(Vector3f.ZERO, 1, 1, 1);
        SkyDome s = new SkyDome();
        geom.setMesh(s);
        
        Node skyNode = (Node) app.getRootNode().getChild("SkyNode");
        skyNode.attachChild(geom);
    }
    
    @Override
    public void update(float dt) {
        float timeOfDay = app.clock.hourTime();
        
        if(time > 30f) {
            time = 0;
            logger.debug("Redraw sky");

            skyGradient.setTurbidity(app.weatherController.getFloat("sky.turbidity"));
            skyGradient.updateSunPosition(11.00f, 180, 36.4f, (int) (11.8f / 15f), 11.8f);
            skyBoxTexture.update();
        }
        
        time += dt;
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        Node skyNode = (Node) app.getRootNode().getChild("SkyNode");
        skyNode.detachChild(geom);
        
        app = null;
        skyGradient = null;
        geom = null;
        skyBoxTexture = null;
    }
}
