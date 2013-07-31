package de.mycrobase.ssim.ed.app;

import org.apache.log4j.Logger;

import ssim.util.MathExt;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.LodControl;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.scene.shape.Quad;
import com.jme3.water.SimpleWaterProcessor;

import de.mycrobase.ssim.ed.mesh.OceanBorder;
import de.mycrobase.ssim.ed.mesh.OceanSurface;
import de.mycrobase.ssim.ed.ocean.PhillipsSpectrum;
import de.mycrobase.ssim.ed.ocean.ReflectionProcessor;
import de.mycrobase.ssim.ed.util.TempVars;
import de.mycrobase.ssim.ed.weather.Weather;

public class OceanAppState extends BasicAppState {

    private static final Logger logger = Logger.getLogger(OceanAppState.class);
    
    private static final float UpdateInterval = 5f; // in seconds

    private static final float GridStep = 400f; // in m
    private static final int GridSize = 64;
    private static final int NumGridTiles = 11; // should be odd
    private static final float TileTexCoordScale = 16f;
    private static final Vector2f TexCoordOffsetVelo = new Vector2f(0.05f, 0.1f);

    // exists only while AppState is attached
    private ReflectionProcessor reflectionProcessor;
    private PhillipsSpectrum phillipsSpectrum;
    private Node oceanNode;
    private Material oceanMat;
    private OceanSurface ocean;
    private Vector2f texCoordOffset;
    
    private int reflectionTexSize;
    
    public OceanAppState() {
        super(UpdateInterval);
    }

    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        evalSettings();
        logger.info(String.format("Reflection map texture size: %d", reflectionTexSize));
        
        reflectionProcessor = new ReflectionProcessor(getApp().getRootNode(), reflectionTexSize);
        getApp().getViewPort().addProcessor(reflectionProcessor);
        
        de.mycrobase.ssim.ed.ocean.SimpleWaterProcessor waterProcessor;
        {
            waterProcessor = new de.mycrobase.ssim.ed.ocean.SimpleWaterProcessor(getApp().getAssetManager());
            waterProcessor.setReflectionScene(getApp().getRootNode());
            waterProcessor.setLightPosition(new Vector3f(1f, 1f, 0f).normalizeLocal());
//            getApp().getViewPort().addProcessor(waterProcessor);
            
//            Quad quad = new Quad(40000,40000);
//            quad.scaleTextureCoordinates(new Vector2f(6f,6f));
//            Geometry water=new Geometry("water", quad);
//            water.setLocalTranslation(-10000, 0, 10000);
//            water.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.HALF_PI, Vector3f.UNIT_X));
//            water.setMaterial(waterProcessor.getMaterial());
//            getApp().getRootNode().attachChild(water);
        }
        
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
        
        texCoordOffset = new Vector2f();

        // TODO: improve ocean shader
        oceanMat = new Material(getApp().getAssetManager(), "shaders/Ocean.j3md");
        oceanMat.setVector2("TexCoordOffset", texCoordOffset);
        oceanMat.setColor("WaterColor", new ColorRGBA(0.0039f, 0.00196f, 0.145f, 1.0f));
        
        {
            double etaratio = 1.0003/1.3333; // ^= firstIndex/secondIndex
            double r0 = Math.pow((1.0-etaratio) / (1.0+etaratio), 2.0);
            oceanMat.setFloat("R0", (float) r0);
        }
        
        oceanMat.setFloat("Shininess", 16f);
        oceanMat.setFloat("ShininessFactor", 0.2f);
        oceanMat.setTexture("SkyBox", getSkyAppState().getSkyBoxTexture());
        oceanMat.setTexture("ReflectionMap", reflectionProcessor.getReflectionTexture());
        //oceanMat.setTexture("ReflectionMap", waterProcessor.getReflectionTexture());
        {
            Texture normalMap = getApp().getAssetManager().loadTexture("textures/SineWaveBumpMap.png");
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
        
        // scroll texture coordinate offset
        texCoordOffset.addLocal(TexCoordOffsetVelo.x * dt, TexCoordOffsetVelo.y * dt);
        oceanMat.setVector2("TexCoordOffset", texCoordOffset);
        
        vars.release();
    }
    
    @Override
    protected void intervalUpdate(float dt) {
        updateOceanParameters();
        updateFog();
        
        // wrap texture coordinate offset periodically
        texCoordOffset.x = MathExt.frac(texCoordOffset.x);
        texCoordOffset.y = MathExt.frac(texCoordOffset.y);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getApp().getViewPort().removeProcessor(reflectionProcessor);
        getApp().getRootNode().detachChild(oceanNode);
        
        reflectionProcessor = null;
        ocean = null;
        oceanMat = null;
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
        float size = getState(CameraAppState.class).getMaxVisibility() * 2;
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
            float strength = 8; //getWeather().getFloat("wind.strength");
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

    private void evalSettings() {
        int detailLevel = getApp().getSettingsManager().getInteger("engine.detail.level");
        switch(detailLevel) {
            case 0: {
                reflectionTexSize = 256;
                break;
            }
            case 1: {
                reflectionTexSize = 512;
                break;
            }
            case 2: {
                reflectionTexSize = 1024;
                break;
            }
        }
    }
    
    private Weather getWeather() {
        return getState(WeatherAppState.class).getWeather();
    }
    
    private SkyAppState getSkyAppState() {
        return getState(SkyAppState.class);
    }
}
