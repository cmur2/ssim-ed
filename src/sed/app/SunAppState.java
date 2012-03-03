package sed.app;

import org.apache.log4j.Logger;

import sed.sky.SunQuad;
import sed.sky.SunTexture;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;

public class SunAppState extends BasicAppState {
    
    private static final Logger logger = Logger.getLogger(SunAppState.class);
    private static final float UpdateInterval = 30f; // in seconds
    
 // exists only while AppState is attached
    private Geometry geom;
    private SunTexture sunTexture;
    private Node sunTranslationNode;
    private Vector3f sunTranslation;
    
    public SunAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        SunQuad sunQuad = new SunQuad(100f);
        geom = new Geometry("Sun", sunQuad);
        Material mat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        
        sunTexture = new SunTexture(getSkyAppState().getSun());
        sunTexture.setLensflareEnabled(getApp().getWeather().getBool("sun.lensflare.enabled"));
        sunTexture.setLensflareShininess(getApp().getWeather().getFloat("sun.lensflare.shininess"));
        sunTexture.update();
        mat.setTexture("ColorMap", sunTexture);
        
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        geom.setMaterial(mat);
        geom.setQueueBucket(Bucket.Transparent);
        
        BillboardControl bbControl = new BillboardControl();
        bbControl.setAlignment(BillboardControl.Alignment.Screen);
        geom.addControl(bbControl);
        
        sunTranslationNode = new Node("SunTranslation");
        updateSunTranslation();
        
        sunTranslationNode.attachChild(geom);
        getSkyAppState().getSkyNode().attachChild(sunTranslationNode);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getSkyAppState().getSkyNode().detachChild(sunTranslationNode);
        
        geom = null;
        sunTexture = null;
        sunTranslationNode = null;
        sunTranslation = null;
    }
    
    @Override
    protected void intervalUpdate() {
        updateSunTranslation();
        sunTexture.setLensflareEnabled(getApp().getWeather().getBool("sun.lensflare.enabled"));
        sunTexture.setLensflareShininess(getApp().getWeather().getFloat("sun.lensflare.shininess"));
        sunTexture.update();
    }
    
    // TODO: build SunControl to move sun
    
    private void updateSunTranslation() {
        sunTranslation = getSkyAppState().getSun().getSunPosition(sunTranslation);
        sunTranslation.multLocal(0.9f * getSkyAppState().getHemisphereRadius());
        sunTranslationNode.setLocalTranslation(sunTranslation);
    }
    
    private SkyAppState getSkyAppState() {
        return getState(SkyAppState.class);
    }
}