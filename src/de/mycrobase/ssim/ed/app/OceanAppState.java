package de.mycrobase.ssim.ed.app;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;

import de.mycrobase.ssim.ed.mesh.OceanSurface;

public class OceanAppState extends BasicAppState {

    private static final float UpdateInterval = 5f; // in seconds

    private static final int NumGridTiles = 64; // should be odd
    
    // exists only while AppState is attached
    private Node oceanNode;
    private OceanSurface ocean;
    
    public OceanAppState() {
        super(UpdateInterval);
    }

    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        ocean = new OceanSurface(NumGridTiles, NumGridTiles, 400f, 400f);
        ocean.setAConstant(.001f);
        ocean.setConvergenceConstant(.15f);
        ocean.setWaveHeightScale(.05f);
        ocean.setWindVelocity(new Vector3f(15,0,15));
        ocean.initSim();
        
        oceanNode = new Node("OceanNode");
        oceanNode.setCullHint(CullHint.Never);
        
        Material oceanMat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        oceanMat.setColor("Color", new ColorRGBA(0.5f, 0.5f, 1f, 1));
        oceanMat.getAdditionalRenderState().setWireframe(true);
        
        oceanNode.attachChild(buildOceanTile(ocean, oceanMat, Vector3f.ZERO));
        
        getApp().getRootNode().attachChild(oceanNode);
    }
    
    @Override
    public void update(float dt) {
        // we need the timed functionality too
        super.update(dt);
        
        ocean.update(dt);
    }
    
    @Override
    protected void intervalUpdate() {
        
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getApp().getRootNode().detachChild(oceanNode);
        
        ocean = null;
        oceanNode = null;
    }
    
    private Geometry buildOceanTile(OceanSurface ocean, Material mat, Vector3f offset) {
        Geometry geom = new Geometry("OceanSurface"+offset.toString(), ocean);
        geom.setMaterial(mat);
        geom.setLocalTranslation(offset);
        geom.setLocalScale(1);
        return geom;
    }
}
