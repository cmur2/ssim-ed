package de.mycrobase.ssim.ed.app;

import ssim.util.MathExt;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.LodControl;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.water.WaterFilter;

import de.mycrobase.ssim.ed.mesh.OceanBorder;
import de.mycrobase.ssim.ed.mesh.OceanSurface;
import de.mycrobase.ssim.ed.ocean.PhillipsSpectrum;
import de.mycrobase.ssim.ed.sky.SkyBoxTexture;
import de.mycrobase.ssim.ed.util.TempVars;
import de.mycrobase.ssim.ed.weather.Weather;

public class OceanAppState extends BasicAppState {

    private static final float UpdateInterval = 5f; // in seconds

    private static final float GridStep = 400f; // in m
    private static final int GridSize = 64;
    private static final int NumGridTiles = 11; // should be odd
    private static final float TileTexCoordScale = 1f;

    // exists only while AppState is attached
    private PhillipsSpectrum phillipsSpectrum;
    private Node oceanNode;
    private Material oceanMat;
    private OceanSurface ocean;
    
    public OceanAppState() {
        super(UpdateInterval);
    }

    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        phillipsSpectrum = new PhillipsSpectrum(true);
        //phillipsSpectrum.setWindVelocity(new Vector3f(0,0,-15));
        
        ocean = new OceanSurface(
            GridSize, GridSize, GridStep, GridStep,
            phillipsSpectrum, getApp().getExecutor(),
            TileTexCoordScale
        );
        updateOceanParameters();
        ocean.initSim();
        
        oceanNode = new Node("OceanNode");
        
//        Material oceanMat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
//        oceanMat.setColor("Color", new ColorRGBA(0.5f, 0.5f, 1f, 1));
//        oceanMat.getAdditionalRenderState().setWireframe(true);
        
//        Material oceanMat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/ShowNormals.j3md");
        
//        Material oceanMat = new Material(getApp().getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
//        oceanMat.setColor("Diffuse", new ColorRGBA(0.5f, 0.5f, 1f, 1));
//        //oceanMat.setColor("Specular", ColorRGBA.White);
//        oceanMat.setBoolean("UseMaterialColors", true);

        // TODO: improve ocean shader
        oceanMat = new Material(getApp().getAssetManager(), "shaders/Ocean.j3md");
        oceanMat.setColor("WaterColor", new ColorRGBA(0.0039f, 0.00196f, 0.145f, 1.0f));
        
        {
            double etaratio = 1.0003/1.3333; // ^= firstIndex/secondIndex
            double r0 = Math.pow((1.0-etaratio) / (1.0+etaratio), 2.0);
            oceanMat.setFloat("R0", (float) r0);
        }
        
        oceanMat.setFloat("Shininess", 16f);
        oceanMat.setFloat("ShininessFactor", 0.2f);
        oceanMat.setTexture("SkyBox", getSkyAppState().getSkyBoxTexture());
        {
            Texture normalMap = getApp().getAssetManager().loadTexture("Common/MatDefs/Water/Textures/water_normalmap.dds");
            normalMap.setWrap(WrapMode.Repeat);
            oceanMat.setTexture("NormalMap", normalMap);
        }
        // Pass fog parameters into shader necessary for Fog.glsllib
        updateFog();
        
        // build geometries
        
        final int numGridTilesHalf = NumGridTiles/2;
        for(int ix = -numGridTilesHalf; ix <= +numGridTilesHalf; ix++) {
            for(int iz = -numGridTilesHalf; iz <= +numGridTilesHalf; iz++) {
                Vector3f offset = new Vector3f(ix,0,iz);
                offset.multLocal(GridStep);
                oceanNode.attachChild(buildOceanTile(ocean, oceanMat, offset));
            }
        }
        
        oceanNode.attachChild(buildOceanBorder(oceanMat));
        
        getApp().getRootNode().attachChild(oceanNode);
        
//        FilterPostProcessor fpp = new FilterPostProcessor(getApp().getAssetManager());
//        WaterFilter water = new WaterFilter(getApp().getRootNode(), new Vector3f(-1f, -1f, 0f).normalizeLocal());
//        fpp.addFilter(water);
//        getApp().getViewPort().addProcessor(fpp);
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
    protected void intervalUpdate(float dt) {
        updateOceanParameters();
        updateFog();
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
        
        LodControl lod = new LodControl();
        lod.setTrisPerPixel(2f);
        geom.addControl(lod);
        
        return geom;
    }
    
    private Geometry buildOceanBorder(Material mat) {
        float size = getState(CameraAppState.class).getMaxVisibility() * 1f;
        float innerSize = NumGridTiles * GridStep;
        if(innerSize >= size) {
            throw new IllegalArgumentException("OceanBorder size will be effectively < 0!");
        }
        // calculate texCoord resolution, one texCoord unit for one tile because of tilability
        float texCoordPerMeter = 1.0f / GridStep;
        OceanBorder border = new OceanBorder(size, size, innerSize, innerSize, texCoordPerMeter, TileTexCoordScale);
        
        Geometry geom = new Geometry("OceanBorder", border);
        geom.setMaterial(mat);
        // translate by a half GridStep since NumGridTiles is odd:
        geom.setLocalTranslation(GridStep/2, 0f, GridStep/2);
        
        return geom;
    }
    
    private void updateOceanParameters() {
        phillipsSpectrum.setAConstant(getWeather().getFloat("ocean.a-factor"));
        phillipsSpectrum.setSmallWaveCutoff(getWeather().getFloat("ocean.wave-cutoff"));
        ocean.setWaveHeightScale(getWeather().getFloat("ocean.height-scale"));
        ocean.setLambda(getWeather().getFloat("ocean.choppiness"));
        
        TempVars vars = TempVars.get();
        
        {
            // TODO: disable varying wind parameter, now they are constant
            float direction = 42; //getWeather().getFloat("wind.direction");
            float strength = 0; //getWeather().getFloat("wind.strength");
            // windVelo will be: direction into which wind is blowing and magnitude
            // reflects strength of wind
            Vector3f windVelo = vars.vect1.set(
                (float) Math.sin(direction * FastMath.DEG_TO_RAD),
                0,
                -(float) Math.cos(direction * FastMath.DEG_TO_RAD));
            // We do not negate here since e.g. an x++ in cloudShift will create
            // an animation looking like an x--, so we would have to double negate
            //windVelo.negateLocal();
            windVelo.multLocal(strength*0.514f); // in m/s
            phillipsSpectrum.setWindVelocity(windVelo);
        }
        
        vars.release();
    }
    
    private void updateFog() {
        oceanMat.setVector3("FogColor", getState(AerialAppState.class).getFogColor());
        oceanMat.setFloat("FogDensity", getState(AerialAppState.class).getFogDensity());
    }
    
    private Weather getWeather() {
        return getState(WeatherAppState.class).getWeather();
    }
    
    private SkyAppState getSkyAppState() {
        return getState(SkyAppState.class);
    }
}
