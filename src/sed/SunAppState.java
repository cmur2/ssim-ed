package sed;

import org.apache.log4j.Logger;

import sed.sky.SunTexture;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.shape.Quad;

public class SunAppState extends AbstractAppState {
    
    private static final Logger logger = Logger.getLogger(SunAppState.class);
    
    // exists only while AppState is living
    private Main app;
    private Geometry geom;
    
    public SunAppState() {
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        app = (Main) baseApp;
        
        Quad sunQuad = new Quad(2f, 2f);
        geom = new Geometry("Sun", sunQuad);
        Material mat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", new SunTexture(0.95f));
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        geom.setMaterial(mat);
        geom.setQueueBucket(Bucket.Transparent);
        
        BillboardControl bbControl = new BillboardControl();
        bbControl.setAlignment(BillboardControl.Alignment.Screen);
        geom.addControl(bbControl);
        
        Node skyNode = (Node) app.getRootNode().getChild("SkyNode");
        skyNode.attachChild(geom);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        Node skyNode = (Node) app.getRootNode().getChild("SkyNode");
        skyNode.detachChild(geom);
        
        app = null;
        geom = null;
    }
}
