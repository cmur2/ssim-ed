package sed.app;

import sed.sky.RainParticles;
import sed.util.TempVars;
import ssim.util.MathExt;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;

public class RainAppState extends BasicAppState {
    
    private static final float UpdateInterval = 2f; // in seconds
    
    private static final float GridStep = 100f; // in m
    private static final int NumGridTiles = 5; // should be odd
   
    private static final float RainLowerY = 0f; // in m
    private static final float RainUpperY = 500f; // in m
    
    // exists only while AppState is attached
    private Node rainNode;
    private RainParticles rain;
    
    public RainAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        rain = new RainParticles(200, 100f);
        rain.setDropLength(5.5f); // in m
        rain.setDropLengthVar(0.5f); // in m
        rain.setDropColor(new ColorRGBA(0.4f, 0.4f, 0.5f, 1.0f));
        rain.setDropColorVar(new ColorRGBA(0.1f, 0.1f, 0.1f, 0.0f));
        rain.setDropVelocity(90f); // in m/s
        rain.setDropVelocityVar(15f); // in m/s
        rain.setMinY( -50f);
        rain.setMaxY(+200f);
        rain.setInitY(+400f);
        rain.initFirstDrops();
        
        rainNode = new Node("RainNode");
        rainNode.setCullHint(CullHint.Never);
        
        Material rainMat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        rainMat.setBoolean("VertexColor", true);
        
        final int numGridTilesHalf = NumGridTiles/2;
        for(int ix = -numGridTilesHalf; ix <= +numGridTilesHalf; ix++) {
            for(int iz = -numGridTilesHalf; iz <= +numGridTilesHalf; iz++) {
                Vector3f offset = new Vector3f(ix,0,iz);
                offset.multLocal(GridStep);
                rainNode.attachChild(buildRainTile(rain, rainMat, offset));
            }
        }
        
        getApp().getRootNode().attachChild(rainNode);
    }
    
    @Override
    public void update(float dt) {
        // we need the timed functionality too
        super.update(dt);
        
        rain.update(dt);
        
        TempVars vars = TempVars.get();
        
        Vector3f loc = vars.vect1.set(getApp().getCamera().getLocation());
        Vector3f gridLoc = vars.vect2.set(
            MathExt.floor(loc.x/GridStep)*GridStep,
            MathExt.clamp(loc.y, RainLowerY-(rain.getMinY()), RainUpperY-(rain.getMaxY())),
            MathExt.floor(loc.z/GridStep)*GridStep
        );
        rainNode.setLocalTranslation(gridLoc);
        
        vars.release();
    }
    
    @Override
    protected void intervalUpdate() {
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getApp().getRootNode().detachChild(rainNode);
        
        rain = null;
        rainNode = null;
    }
    
    private Geometry buildRainTile(RainParticles rain, Material mat, Vector3f offset) {
        Geometry geom = new Geometry("RainParticles"+offset.toString(), rain);
        geom.setMaterial(mat);
        geom.setLocalTranslation(offset);
        return geom;
    }
}
