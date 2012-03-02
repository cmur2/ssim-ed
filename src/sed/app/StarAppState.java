package sed.app;

import org.apache.log4j.Logger;

import sed.sky.StarField;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.scene.Geometry;

public class StarAppState extends BasicAppState {
    
    private static final Logger logger = Logger.getLogger(StarAppState.class);
    private static final float UpdateInterval = 30f; // in seconds
    
    private static final float StarsThetaMin = 80f;
    
    private float time = 0;
    
    // exists only while AppState is living
    private Geometry geom;
    
    private Vector2f sunAngles;
    
    public StarAppState() {
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        StarField starField = new StarField(100, 0.9f*SkyDomeAppState.HemisphereRadius);
        geom = new Geometry("StarField", starField);
        Material mat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setBoolean("VertexColor", true);
        geom.setMaterial(mat);
        
        getApp().getSkyNode().attachChild(geom);

        updateStars();
    }
    
    @Override
    public void update(float dt) {
        if(time >= UpdateInterval) {
            time -= UpdateInterval;
            updateStars();
        }
        time += dt;
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getApp().getSkyNode().detachChild(geom);
        
        geom = null;
    }
    
    private void updateStars() {
        sunAngles = getApp().getSun().getSunAngles(sunAngles);
        float thetaDeg = (float) Math.toDegrees(sunAngles.y);
        if(thetaDeg > StarsThetaMin) {
            if(!getApp().getSkyNode().hasChild(geom)) {
                logger.debug("Attach star field");
                getApp().getSkyNode().attachChild(geom);
            }
        } else {
            if(getApp().getSkyNode().hasChild(geom)) {
                logger.debug("Detach star field");
                getApp().getSkyNode().detachChild(geom);
            }
        }
    }
}
