package de.mycrobase.ssim.ed.app;

import ssim.util.MathExt;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.LodControl;

import de.mycrobase.ssim.ed.mesh.OceanSurface;
import de.mycrobase.ssim.ed.ocean.PhillipsSpectrum;
import de.mycrobase.ssim.ed.util.TempVars;

public class OceanAppState extends BasicAppState {

    private static final float UpdateInterval = 5f; // in seconds

    private static final float GridStep = 400f; // in m
    private static final int GridSize = 64;
    private static final int NumGridTiles = 13; // should be odd
    
    // exists only while AppState is attached
    private PhillipsSpectrum phillipsSpectrum;
    private Node oceanNode;
    private OceanSurface ocean;
    
    public OceanAppState() {
        super(UpdateInterval);
    }

    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        phillipsSpectrum = new PhillipsSpectrum(true);
        phillipsSpectrum.setAConstant(.001f);
        phillipsSpectrum.setSmallWaveCutoff(1f);
        phillipsSpectrum.setWindVelocity(new Vector3f(15,0,15));
        
        ocean = new OceanSurface(GridSize, GridSize, GridStep, GridStep, phillipsSpectrum);
        // TODO: Params in Weather
        ocean.setWaveHeightScale(.03f);
        ocean.setLambda(.05f);
        ocean.initSim();
        
        oceanNode = new Node("OceanNode");
        
//        Material oceanMat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
//        oceanMat.setColor("Color", new ColorRGBA(0.5f, 0.5f, 1f, 1));
//        oceanMat.getAdditionalRenderState().setWireframe(true);
        
//        Material oceanMat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/ShowNormals.j3md");
        
        // TODO: need shader
        Material oceanMat = new Material(getApp().getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        oceanMat.setColor("Diffuse", new ColorRGBA(0.5f, 0.5f, 1f, 1));
        oceanMat.setColor("Specular", ColorRGBA.White);
        oceanMat.setBoolean("UseMaterialColors", true);
        
        // TODO: add far ocean tiles, maybe as second LOD-Mesh for all tiles?
        
        final int numGridTilesHalf = NumGridTiles/2;
        for(int ix = -numGridTilesHalf; ix <= +numGridTilesHalf; ix++) {
            for(int iz = -numGridTilesHalf; iz <= +numGridTilesHalf; iz++) {
                Vector3f offset = new Vector3f(ix,0,iz);
                offset.multLocal(GridStep);
                oceanNode.attachChild(buildOceanTile(ocean, oceanMat, offset));
            }
        }
        
        getApp().getRootNode().attachChild(oceanNode);
    }
    
    @Override
    public void update(float dt) {
        // we need the timed functionality too
        super.update(dt);
        
        ocean.update(dt);
        
        TempVars vars = TempVars.get();
        
        Vector3f loc = vars.vect1.set(getApp().getCamera().getLocation());
        Vector3f gridLoc = vars.vect2.set(
            MathExt.floor(loc.x/GridStep)*GridStep,
            0,
            MathExt.floor(loc.z/GridStep)*GridStep
        );
        oceanNode.setLocalTranslation(gridLoc);
        
        vars.release();
    }
    
    @Override
    protected void intervalUpdate() {
        // TODO: update ocean surface parameters
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
        
        LodControl lod = new LodControl();
        lod.setTrisPerPixel(0.7f);
        geom.addControl(lod);
        
        return geom;
    }
}
