package sed.app;

import org.apache.log4j.Logger;

import sed.sky.CloudPlane;
import sed.sky.CloudProcessor;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;

/**
 * <b>Higher layer</b> {@link AppState} responsible for cloud rendering.
 * 
 * @author cn
 */
public class CloudAppState extends BasicAppState {
    
    private static final Logger logger = Logger.getLogger(CloudAppState.class);
    private static final float UpdateInterval = 10f; // in seconds
    
    // exists only while AppState is attached
    private CloudProcessor cloudProcessor;
    private Geometry geom;

    private Vector3f sunPosition;
    private Vector3f sunColor;
    private float[] sunColorArray;
    private Vector3f cloudShift;
    
    public CloudAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        cloudProcessor = new CloudProcessor(getApp().getAssetManager(), CloudProcessor.Mode.RenderGPU, UpdateInterval);
        getApp().getViewPort().addProcessor(cloudProcessor);
        
        //Quad cloudQuad = new Quad(10, 10);
        CloudPlane cloudQuad = new CloudPlane(750f, 50f, new Vector3f(0, 500, 0));
        geom = new Geometry("Clouds", cloudQuad);
        
        //Material mat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        //mat.setColor("Color", ColorRGBA.Orange);
        //mat.getAdditionalRenderState().setWireframe(true);
        //mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        
        Material mat = new Material(getApp().getAssetManager(), "shaders/CloudFinal.j3md");
        mat.setTexture("ColorMap", cloudProcessor.getCloudTex());
        //mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        geom.setMaterial(mat);
        geom.setQueueBucket(Bucket.Transparent);
        
        // update before attaching since all SceneProcessors are initialized
        // then (and the CP requires some variables set)
        intervalUpdate();
        
        getState(SkyAppState.class).getSkyNode().attachChild(geom);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getState(SkyAppState.class).getSkyNode().detachChild(geom);
        getApp().getViewPort().removeProcessor(cloudProcessor);
        
        cloudProcessor = null;
        geom = null;
        sunPosition = null;
        sunColor = null;
        sunColorArray = null;
        cloudShift = null;
    }
    
    @Override
    protected void intervalUpdate() {
        cloudProcessor.setCloudCover(getApp().getWeather().getFloat("cloud.cover"));
        cloudProcessor.setCloudSharpness(getApp().getWeather().getFloat("cloud.sharpness"));
        cloudProcessor.setWayFactor(getApp().getWeather().getFloat("cloud.way-factor"));
        cloudProcessor.setZoom(getApp().getWeather().getInt("cloud.zoom"));
        
        sunPosition = getState(SkyAppState.class).getSun().getSunPosition(sunPosition);
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
