package sed;

import org.apache.log4j.Logger;

import sed.sky.CloudProcessor;

import chlib.noise.NoiseUtil;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Quad;

public class CloudAppState extends AbstractAppState {
    
    private static final Logger logger = Logger.getLogger(CloudAppState.class);
    private static final float UpdateInterval = 10f; // in seconds
    
    private float time = 0;
    
    // exists only while AppState is living
    private Main app;
    private CloudProcessor cloudProcessor;
    private Geometry geom;
    
    public CloudAppState() {
        // TODO: initialize NoiseUtil more central, and with random seed
        NoiseUtil.reinitialize(4569845);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        app = (Main) baseApp;
        
        cloudProcessor = new CloudProcessor(CloudProcessor.Mode.AllCPU, app.getAssetManager());
        app.getViewPort().addProcessor(cloudProcessor);
        
        // TODO: CloudPlane
        Quad cloudQuad = new Quad(10, 10);
        geom = new Geometry("Clouds", cloudQuad);
        
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        //Material mat = new Material(app.getAssetManager(), "shaders/CloudFinal.j3md");
        mat.setTexture("ColorMap", cloudProcessor.getCloudTex());
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        geom.setMaterial(mat);
        geom.setQueueBucket(Bucket.Transparent);
                
        updateClouds();
        
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
        cloudProcessor = null;
        geom = null;
    }
    
    private void updateClouds() {
        cloudProcessor.setCloudCover(50);
        cloudProcessor.setCloudSharpness(0.96f);
        cloudProcessor.setWayFactor(0.0005f);
        cloudProcessor.setSunPosition(new Vector3f(256/2, 256/2, 5000));
        cloudProcessor.setSunLightColor(new Vector3f(1.0f, 1.0f, 1.0f));
        // TODO: some movement (wind)/change (permutation) should be done (aka cloud physics)
    }
}