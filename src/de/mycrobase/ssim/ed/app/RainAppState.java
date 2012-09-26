package de.mycrobase.ssim.ed.app;

import ssim.util.MathExt;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;

import de.mycrobase.ssim.ed.mesh.RainParticles;
import de.mycrobase.ssim.ed.util.TempVars;
import de.mycrobase.ssim.ed.weather.Weather;
import de.mycrobase.ssim.ed.weather.ext.PrecipitationType;

public class RainAppState extends BasicAppState {
    
    private static final float UpdateInterval = 5f; // in seconds
    
    private static final float GridStep = 100f; // in m
    private static final int NumGridTiles = 5; // should be odd
    
    // range in which the particles will follow the camera
    private static final float RainLowerY = 0f; // in m
    private static final float RainUpperY = 500f; // in m
    
    // exists only while AppState is attached
    private Node rainNode;
    private RainParticles rain;
    
    private PrecipitationType curType;
    private Vector3f windVelocity;
    
    public RainAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        curType = PrecipitationType.None;
        
        // TODO: precipitation.intensity should influence numDrops
        rain = new RainParticles(200, GridStep);
        rain.setMinY( -50f);
        rain.setMaxY(+200f);
        rain.setInitY(+400f);
        rain.setLineWidth(2f);
        
        rainNode = new Node("RainNode");
        // Maybe disable culling if plopping is too intensive even with
        // enlarged virtual bounds in RainParticles:
        //rainNode.setCullHint(CullHint.Never);
        
        Material rainMat = new Material(getApp().getAssetManager(), "shaders/RainParticles.j3md");
        rainMat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        
        final int numGridTilesHalf = NumGridTiles/2;
        for(int ix = -numGridTilesHalf; ix <= +numGridTilesHalf; ix++) {
            for(int iz = -numGridTilesHalf; iz <= +numGridTilesHalf; iz++) {
                Vector3f offset = new Vector3f(ix,0,iz);
                offset.multLocal(GridStep);
                rainNode.attachChild(buildRainTile(rain, rainMat, offset));
            }
        }
        
        updateRain();
    }
    
    @Override
    public void update(float dt) {
        // we need the timed functionality too
        super.update(dt);
        
        if(curType != PrecipitationType.None) {
            rain.update(dt);
        }
        
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
    protected void intervalUpdate(float dt) {
        updateRain();
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
        geom.setQueueBucket(Bucket.Transparent);
        geom.setLocalTranslation(offset);
        return geom;
    }

    private void updateRain() {
        PrecipitationType oldType = curType;
        curType = PrecipitationType.fromId(getWeather().getInt("precipitation.form"));
        
        updateParticleProperties();
        updateWindVelo();
        
        if(curType == PrecipitationType.None) {
            if(getApp().getRootNode().hasChild(rainNode)) {
                getApp().getRootNode().detachChild(rainNode);
            }
        } else {
            if(!getApp().getRootNode().hasChild(rainNode)) {
                getApp().getRootNode().attachChild(rainNode);
            }
        }
        
        if(oldType == PrecipitationType.None && curType != PrecipitationType.None) {
            // if there was no precipitation before and now there is some, it
            // must have "started" again
            rain.initFirstDrops();
        }
    }
    
    private void updateParticleProperties() {
        float intensity = getWeather().getFloat("precipitation.intensity");
        
        switch(curType) {
        case None: {
            rain.setDropLength(0);
            rain.setDropLengthVar(0);
            rain.setDropColor(ColorRGBA.Black);
            rain.setDropColorVar(ColorRGBA.Black);
            rain.setDropVelocity(0);
            rain.setDropVelocityVar(0);
            break;
        }
        case Rain: {
            rain.setDropLength(2.0f + 3.5f * intensity); // in m
            rain.setDropLengthVar(0.5f); // in m
            rain.setDropColor(new ColorRGBA(0.4f, 0.4f, 0.5f, 0.3f));
            rain.setDropColorVar(new ColorRGBA(0.1f, 0.1f, 0.1f, 0.0f));
            rain.setDropVelocity(90f); // in m/s
            rain.setDropVelocityVar(15f); // in m/s
            break;
        }
        case IcePellets: {
            rain.setDropLength(1.5f + 1.0f * intensity); // in m
            rain.setDropLengthVar(0.5f); // in m
            rain.setDropColor(new ColorRGBA(0.7f, 0.7f, 0.7f, 0.5f));
            rain.setDropColorVar(new ColorRGBA(0.1f, 0.1f, 0.1f, 0.0f));
            rain.setDropVelocity(75f); // in m/s
            rain.setDropVelocityVar(10f); // in m/s
            break;
        }
        case Snow: {
            rain.setDropLength(0.2f + 0.3f * intensity); // in m
            rain.setDropLengthVar(0.1f); // in m
            rain.setDropColor(new ColorRGBA(0.9f, 0.9f, 0.9f, 0.5f));
            rain.setDropColorVar(new ColorRGBA(0.1f, 0.1f, 0.1f, 0.0f));
            rain.setDropVelocity(30f); // in m/s
            rain.setDropVelocityVar(3f); // in m/s
            break;
        }
        }
    }
    
    private void updateWindVelo() {
        if(windVelocity == null) {
            windVelocity = new Vector3f();
        }
        float direction = getWeather().getFloat("wind.direction");
        float strength = getWeather().getFloat("wind.strength");
        // windVelocity will be: direction into which wind is blowing and magnitude
        // reflects strength of wind
        windVelocity.set(
            (float) Math.sin(direction * FastMath.DEG_TO_RAD),
            0,
            -(float) Math.cos(direction * FastMath.DEG_TO_RAD));
        windVelocity.negateLocal();
        windVelocity.multLocal(strength*0.514f); // in m/s
        rain.setWindVelocity(windVelocity);
    }
    
    private Weather getWeather() {
        return getState(WeatherAppState.class).getWeather();
    }
}
