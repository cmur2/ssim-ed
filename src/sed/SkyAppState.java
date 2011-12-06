package sed;

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
    
    private float time = 0;
    private SkyGradient skyGradient;
    
    // exists only while AppState is living
    private Main app;
    private Geometry geom;
    private SkyBoxTexture skyBoxTexture;
    
    public SkyAppState() {
        skyGradient = new SkyGradient();
        skyGradient.setTurbidity(2f);
        skyGradient.updateSunPosition(11.00f, 180, 36.4f, (int) (11.8f / 15f), 11.8f);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        app = (Main) baseApp;
        
        geom = new Geometry("SkyDome");
        
        Material mat = new Material(app.getAssetManager(), "shaders_tmp/Sky.j3md");
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
    public void update(float tpf) {
        super.update(tpf);
        
        float timeOfDay = app.clock.hourTime();
        // TODO: skyGradient.updateSunPosition(timeOfDay, julianDay, latitude, standardMeridian, longitude)
        
        time += tpf;
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        Node skyNode = (Node) app.getRootNode().getChild("SkyNode");
        skyNode.detachChild(geom);
        
        app = null;
        geom = null;
        skyBoxTexture = null;
    }
}
