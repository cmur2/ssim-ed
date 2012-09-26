package de.mycrobase.ssim.ed.app;

import org.apache.log4j.Logger;


import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

import de.mycrobase.ssim.ed.FixedOrderComparator;
import de.mycrobase.ssim.ed.mesh.StarField;

/**
 * <b>Higher layer</b> {@link AppState} responsible for rendering a star field.
 * 
 * @author cn
 */
public class StarAppState extends BasicAppState {
    
    private static final Logger logger = Logger.getLogger(StarAppState.class);
    private static final float UpdateInterval = 30f; // in seconds
    
    private static final int NumStars = 100;
    private static final float StarsThetaMin = 80f;
    
    // exists only while AppState is attached
    private Geometry geom;
    
    private Vector2f sunAngles;
    
    public StarAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        StarField starField = new StarField(NumStars, 1.0f * getState(SkyAppState.class).getHemisphereRadius());
        geom = new Geometry("StarField", starField);
        Material mat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setBoolean("VertexColor", true);
        geom.setMaterial(mat);
        geom.setUserData(FixedOrderComparator.ORDER_INDEX, 8);
        geom.setQueueBucket(Bucket.Sky);
        
        getSkyNode().attachChild(geom);

        updateStars();
    }
    
    @Override
    protected void intervalUpdate(float dt) {
        updateStars();
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        if(getSkyAppState() != null && getSkyAppState().getSkyNode() != null) {
            getSkyNode().detachChild(geom);
        }
        
        geom = null;
    }
    
    private void updateStars() {
        sunAngles = getState(SkyAppState.class).getSun().getSunAngles(sunAngles);
        float thetaDeg = (float) Math.toDegrees(sunAngles.y);
        if(thetaDeg > StarsThetaMin) {
            if(!getSkyNode().hasChild(geom)) {
                logger.debug("Attach star field");
                getSkyNode().attachChild(geom);
            }
        } else {
            if(getSkyNode().hasChild(geom)) {
                logger.debug("Detach star field");
                getSkyNode().detachChild(geom);
            }
        }
    }
    
    private SkyAppState getSkyAppState() {
        return getState(SkyAppState.class);
    }
    
    private Node getSkyNode() {
        return getSkyAppState().getSkyNode();
    }
}
