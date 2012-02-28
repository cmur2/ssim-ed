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

    private Vector3f sunPosition;
    private Vector3f sunColor;
    private float[] sunColorArray;
    private Vector3f cloudShift;
    
    public CloudAppState() {
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        app = (Main) baseApp;
        
        cloudProcessor = new CloudProcessor(app.getAssetManager(), CloudProcessor.Mode.RenderGPU, UpdateInterval);
        app.getViewPort().addProcessor(cloudProcessor);
        
        // TODO: CloudPlane
        Quad cloudQuad = new Quad(10, 10);
        geom = new Geometry("Clouds", cloudQuad);
        
        //Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        Material mat = new Material(app.getAssetManager(), "shaders/CloudFinal.j3md");
        mat.setTexture("ColorMap", cloudProcessor.getCloudTex());
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        geom.setMaterial(mat);
        geom.setQueueBucket(Bucket.Transparent);
        
        // update before attaching since all SceneProcessors are initialized
        // then (and the CP requires some variables set)
        updateClouds();
        
        app.getRootNode().attachChild(geom);
    }
    
    @Override
    public void update(float dt) {
        if(time >= UpdateInterval) {
            time -= UpdateInterval;
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
        cloudShift = null;
    }
    
    private void updateClouds() {
        cloudProcessor.setCloudCover(app.getWeather().getFloat("cloud.cover"));
        cloudProcessor.setCloudSharpness(app.getWeather().getFloat("cloud.sharpness"));
        cloudProcessor.setWayFactor(app.getWeather().getFloat("cloud.way-factor"));
        cloudProcessor.setZoom(app.getWeather().getInt("cloud.zoom"));
        
        sunPosition = app.getSun().getSunPosition(sunPosition);
        if(sunColor == null) {
            sunColor = new Vector3f();
        }
        // TODO: Clouds lit by real suns color are too dark, maybe modify this color
//        sunColorArray = app.getSkyGradient().getSkyColor(sunPosition, sunColorArray);
//        sunColor.set(sunColorArray[0], sunColorArray[1], sunColorArray[2]);
//        System.out.println(sunColor);
        sunColor.set(1f, 1f, 1f);
        
        cloudProcessor.setSunLightColor(sunColor);

        cloudProcessor.setSunPosition(new Vector3f(-500, 256/2, 3000));
        
        if(cloudShift == null) {
            cloudShift = new Vector3f(Vector3f.ZERO);
        }
        cloudProcessor.setShift(cloudShift);
//        cloudShift.x += 0.025;
        
        // TODO: some movement (wind)/change (permutation) should be done (aka cloud physics)
    }
}
