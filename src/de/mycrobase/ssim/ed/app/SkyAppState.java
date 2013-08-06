package de.mycrobase.ssim.ed.app;


import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.NullComparator;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;

import de.mycrobase.ssim.ed.FixedOrderComparator;
import de.mycrobase.ssim.ed.SurfaceCameraControl;
import de.mycrobase.ssim.ed.mission.Mission;
import de.mycrobase.ssim.ed.sky.SkyBoxTexture;
import de.mycrobase.ssim.ed.sky.SkyGradient;
import de.mycrobase.ssim.ed.sky.Sun;

/**
 * <b>Base layer</b> {@link AppState} providing a sky {@link Node}
 * (to attach all sky related things at), a {@link SkyGradient} and
 * a {@link Sun}.
 * 
 * @author cn
 */
public class SkyAppState extends BasicAppState {
    
    private static final float UpdateInterval = 1f; // in seconds
    private static final int SkyBoxTextureUpdateCounterLimit = 30;
    
    private float hemisphereRadius;
    private Mission mission;
    
    // exists only while AppState is attached
    private Node skyNode;
    private Sun sun;
    private SkyGradient skyGradient;
    private int skyBoxTextureUpdateCounter;
    private SkyBoxTexture skyBoxTexture;
    
    public SkyAppState(float hemisphereRadius, Mission mission) {
        super(UpdateInterval);
        this.hemisphereRadius = hemisphereRadius;
        this.mission = mission;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        skyNode = new Node("SkyNode");
        skyNode.setCullHint(CullHint.Never);
        getApp().getRootNode().attachChild(skyNode);
        
        // since the constructors shouldn't do anything related to processing
        // the delayed update should work
        sun = new Sun(getState(SimClockAppState.class).getSimClock(), mission);
        skyGradient = new SkyGradient(sun);
        updateSky();
        
        skyBoxTextureUpdateCounter = 0;
        skyBoxTexture = new SkyBoxTexture(skyGradient, getApp().getExecutor());
        updateSkyBoxTexture();
        
        getApp().getViewPort().getQueue().setGeometryComparator(Bucket.Sky, new FixedOrderComparator());
        
        skyNode.addControl(new SurfaceCameraControl(getApp().getCamera()));
    }
    
    @Override
    protected void intervalUpdate(float dt) {
        updateSky();
        
        if(skyBoxTextureUpdateCounter >= SkyBoxTextureUpdateCounterLimit) {
            updateSkyBoxTexture();
            skyBoxTextureUpdateCounter = 0;
        }
        skyBoxTextureUpdateCounter++;
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        skyNode.removeControl(SurfaceCameraControl.class);
        getApp().getViewPort().getQueue().setGeometryComparator(Bucket.Sky, new NullComparator());
        getApp().getRootNode().detachChild(skyNode);
        
        skyNode = null;
        sun = null;
        skyGradient = null;
        skyBoxTexture = null;
    }
    
    private void updateSky() {
        sun.update();
        skyGradient.setTurbidity(getState(WeatherAppState.class).getWeather().getFloat("air.turbidity"));
        skyGradient.update();
    }
    
    private void updateSkyBoxTexture() {
        skyBoxTexture.update();
    }
    
    // public API
    
    public Node getSkyNode() {
        return skyNode;
    }
    
    public Sun getSun() {
        return sun;
    }
    
    public SkyGradient getSkyGradient() {
        return skyGradient;
    }
    
    public SkyBoxTexture getSkyBoxTexture() {
        return skyBoxTexture;
    }

    public float getNightThetaMax() {
        return SkyGradient.NightThetaMax;
    }

    public ColorRGBA getNightSunColor() {
        return SkyGradient.NightSunColor;
    }

    public float getHemisphereRadius() {
        return hemisphereRadius;
    }
}
