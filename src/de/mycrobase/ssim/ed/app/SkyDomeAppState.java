package de.mycrobase.ssim.ed.app;

import org.apache.log4j.Logger;


import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;

import de.mycrobase.ssim.ed.FixedOrderComparator;
import de.mycrobase.ssim.ed.mesh.SkyDome;
import de.mycrobase.ssim.ed.sky.SkyBoxTexture;

/**
 * <b>Higher layer</b> {@link AppState} responsible for rendering sky dome.
 * 
 * @author cn
 */
public class SkyDomeAppState extends BasicAppState {
    
    private static final Logger logger = Logger.getLogger(SkyDomeAppState.class);
    private static final float UpdateInterval = 30f; // in seconds
    
    // exists only while AppState is attached
    private Geometry geom;
    private SkyBoxTexture skyBoxTexture;
    
    public SkyDomeAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        geom = new Geometry("SkyDome");
        
        Material mat = new Material(getApp().getAssetManager(), "shaders/SkyDome.j3md");
        //mat.getAdditionalRenderState().setWireframe(true);
        mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
        
        skyBoxTexture = new SkyBoxTexture(getSkyAppState().getSkyGradient(), getApp().getExecutor());
        skyBoxTexture.update();
        mat.setTexture("SkyBox", skyBoxTexture);
        
        geom.setMaterial(mat);
        geom.setUserData(FixedOrderComparator.ORDER_INDEX, 10);
        geom.setQueueBucket(Bucket.Sky);
        
        //Box s = new Box(Vector3f.ZERO, 1, 1, 1);
        SkyDome s = new SkyDome(getSkyAppState().getHemisphereRadius(), 2f, 2f);
        geom.setMesh(s);
        
        getSkyAppState().getSkyNode().attachChild(geom);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        if(getSkyAppState() != null && getSkyAppState().getSkyNode() != null) {
            getSkyAppState().getSkyNode().detachChild(geom);
        }
        
        geom = null;
        skyBoxTexture = null;
    }
    
    @Override
    protected void intervalUpdate() {
        skyBoxTexture.update();
    }
    
    private SkyAppState getSkyAppState() {
        return getState(SkyAppState.class);
    }
}
