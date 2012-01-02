package sed;

import org.apache.log4j.Logger;

import sed.sky.CloudProcessor;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;

public class CloudAppState extends AbstractAppState {
    
    private static final Logger logger = Logger.getLogger(CloudAppState.class);
    private static final float UpdateInterval = 30f; // in seconds
    
    private float time = 0;
    
    // exists only while AppState is living
    private Main app;
    private Geometry geom;
    
    public CloudAppState() {
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        app = (Main) baseApp;
        
        CloudProcessor cp = new CloudProcessor(app.getAssetManager());
        app.getViewPort().addProcessor(cp);
        
        // TODO: CloudPlane
        Quad cloudQuad = new Quad(10, 10);
        geom = new Geometry("Clouds", cloudQuad);
        
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", cp.getTex());
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        geom.setMaterial(mat);
        geom.setQueueBucket(Bucket.Transparent);
                
        //updateClouds();
        
        app.getRootNode().attachChild(geom);
    }
        
    @Override
    public void update(float dt) {
        if(time > UpdateInterval) {
            time = 0;
            updateClouds();
        }
        time += dt;
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        app.getRootNode().detachChild(geom);
        
        app = null;
        geom = null;
    }
    
    private void updateClouds() {
        
    }
}
