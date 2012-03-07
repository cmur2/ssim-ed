package sed.app;

import org.apache.log4j.Logger;

import sed.TempVars;
import sed.sky.CloudPlane;
import sed.sky.CloudProcessor;
import sed.weather.Weather;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
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
        geom = new Geometry("CloudPlane", cloudQuad);
        
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
        
        getSkyAppState().getSkyNode().attachChild(geom);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        if(getSkyAppState().getSkyNode() != null) {
            getSkyAppState().getSkyNode().detachChild(geom);
        }
        getApp().getViewPort().removeProcessor(cloudProcessor);
        
        cloudProcessor = null;
        geom = null;
    }
    
    @Override
    protected void intervalUpdate() {
        cloudProcessor.setCloudCover(getWeather().getFloat("cloud.cover"));
        cloudProcessor.setCloudSharpness(getWeather().getFloat("cloud.sharpness"));
        cloudProcessor.setWayFactor(getWeather().getFloat("cloud.way-factor"));
        cloudProcessor.setZoom(getWeather().getInt("cloud.zoom"));
        
        TempVars vars = TempVars.get();
        
        Vector3f sunPosition = getSkyAppState().getSun().getSunPosition(vars.vect1);
        
        ColorRGBA sunLightColor = getSkyAppState().getSkyGradient().getSunLightColor(vars.color1);
        //ColorRGBA sunLightColor = ColorRGBA.White;
        System.out.println(sunLightColor);
        cloudProcessor.setSunLightColor(sunLightColor);

        cloudProcessor.setSunPosition(new Vector3f(-500, 256/2, 3000));
        
        if(cloudShift == null) {
            cloudShift = new Vector3f(Vector3f.ZERO);
        }
        cloudProcessor.setShift(cloudShift);
//        cloudShift.x += 0.025;
        
        // TODO: some movement (wind)/change (permutation) should be done (aka cloud physics)
        
        vars.release();
    }
    
    private Weather getWeather() {
        return getState(WeatherAppState.class).getWeather();
    }
    
    private SkyAppState getSkyAppState() {
        return getState(SkyAppState.class);
    }
}
