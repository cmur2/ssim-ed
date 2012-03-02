package sed;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import sed.mission.Mission;
import sed.mission.MissionParser;
import sed.sky.SkyGradient;
import sed.sky.Sun;
import sed.weather.Interpolators;
import sed.weather.RandomWeatherController;
import sed.weather.Weather;
import sed.weather.WeatherController;
import sed.weather.XMLPropertySetBuilder;
import ssim.sim.SimClock;

import chlib.noise.NoiseUtil;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

import de.altimos.util.logger.JLFBridge;

public class Main extends SimpleApplication {
    
    private static final Logger logger = Logger.getLogger(Main.class);
    private static final float UpdateInterval = 1f; // in seconds
    private static final long DebugSeed = 4569845;
    
    public static void main(String[] args) {
        //org.apache.log4j.BasicConfigurator.configure();
        {
            Logger root = Logger.getRootLogger();
            root.addAppender(new ConsoleAppender(new PatternLayout("%-3r [%t] %-5p %c: %m%n")));
        }
        JLFBridge.installBridge();
        
        Main main = new Main();
        main.setShowSettings(false);
        main.start();
    }
    
    private float time = 0;
    
    private ScheduledThreadPoolExecutor executor;
    private Mission mission;
    private SimClock simClock;
    private WeatherController weatherController;
    private Node skyNode;
    private Sun sun;
    private SkyGradient skyGradient;
    
    @Override
    public void simpleInitApp() {
        // TODO: Initialize NoiseUtil with random seed
        NoiseUtil.reinitialize(DebugSeed);
        
        assetManager.registerLoader(XMLLoader.class, "xml");
        assetManager.registerLoader(MapLoader.class, "map");
        inputManager.deleteTrigger("FLYCAM_Lower", new KeyTrigger(KeyInput.KEY_Z));
        inputManager.addMapping("FLYCAM_Lower", new KeyTrigger(KeyInput.KEY_Y));
        
        executor = new ScheduledThreadPoolExecutor(2 * Runtime.getRuntime().availableProcessors());
        
        initMission();
        
        simClock = SimClock.createClock(getMission().getTimeOfDay());
        assert simClock != null : "SimClock init failed - wrong parameters!";
        
        speed = 1f;
        
        initWeather();
        
        flyCam.setMoveSpeed(10 * 6);
        //flyCam.setDragToRotate(true);
        //cam.setLocation(new Vector3f(0, -200f, 0));
        
        skyNode = new Node("SkyNode");
        skyNode.setCullHint(CullHint.Never);
        rootNode.attachChild(skyNode);
        
        sun = new Sun(this);
        sun.update();
        skyGradient = new SkyGradient(this);
        skyGradient.setTurbidity(getWeather().getFloat("sky.turbidity"));
        skyGradient.update();
        
        stateManager.attach(new SkyAppState());
        stateManager.attach(new SunAppState());
        stateManager.attach(new LightingAppState());
        stateManager.attach(new StarAppState());
        stateManager.attach(new CloudAppState());
        //stateManager.attach(new TerrainAppState());
        
        {
            Box boxBox = new Box(Vector3f.ZERO, 1, 1, 1);
            Geometry boxGeom = new Geometry("Box", boxBox);
            Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            boxMat.setColor("Color", ColorRGBA.Blue);
            boxGeom.setMaterial(boxMat);
            //rootNode.attachChild(boxGeom);
        }
        
        {
            Sphere rockSphere = new Sphere(32,32, 2f);
            Geometry rockGeom = new Geometry("Shiny rock", rockSphere);
            rockSphere.setTextureMode(Sphere.TextureMode.Projected);
            com.jme3.util.TangentBinormalGenerator.generate(rockSphere);
            Material rockMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            rockMat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"));
            rockMat.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"));
            rockMat.setFloat("Shininess", 5f); // [1,128]
            rockGeom.setMaterial(rockMat);
            rockGeom.rotate(1.6f, 0, 0);
            //rootNode.attachChild(rockGeom);
        }
        
        skyNode.addControl(new SurfaceCameraControl(cam));
        
        //printSceneGraph(rootNode);
    }
    
    private void initMission() {
        mission = MissionParser.load(assetManager, "missions/mission_01.xml");
    }
    
    private void initWeather() {
//        String[] sets = {"clear", "snowy"};
        String[] sets = {"clear"};
        XMLPropertySetBuilder builder = new XMLPropertySetBuilder(assetManager, sets);
        builder.putFloat("sky.turbidity");
        builder.putVec3("sky.light");
        builder.putBool("sun.lensflare.enabled");
        builder.putFloat("sun.lensflare.shininess");
        builder.putFloat("cloud.cover");
        builder.putFloat("cloud.sharpness");
        builder.putFloat("cloud.way-factor");
        builder.putInt("cloud.zoom");
        
        weatherController = new RandomWeatherController(5*60f, builder.getResults());
        weatherController.registerInterpolator(new Interpolators.FloatInterpolator(), Float.class);
        weatherController.registerInterpolator(new Interpolators.BoolInterpolator(), Boolean.class);
        weatherController.registerInterpolator(new Interpolators.Vec3Interpolator(), Vector3f.class);
        weatherController.registerInterpolator(new Interpolators.IntInterpolator(), Integer.class);
    }
    
    @Override
    public void simpleUpdate(float dt) {
        if(time > UpdateInterval) {
            time -= UpdateInterval;
            sun.update();
            skyGradient.setTurbidity(getWeather().getFloat("sky.turbidity"));
            skyGradient.update();
        }
        
        weatherController.update(dt);
        simClock.step(dt);
        
        time += dt;
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        // shutdown all (non daemon) worker pool threads
        executor.shutdown();
    }
    
    // simple getters
    
    public Mission getMission() {
        return mission;
    }
    
    public SimClock getSimClock() {
        return simClock;
    }
    
    /**
     * @return only the {@link Weather} part of the applications
     *         {@link WeatherController} since all other part should only
     *         read weather information
     */
    public Weather getWeather() {
        return weatherController;
    }
    
    public Node getSkyNode() {
        return skyNode;
    }
    
    public Sun getSun() {
        return sun;
    }
    
    public SkyGradient getSkyGradient() {
        return skyGradient;
    }
    
    public ScheduledThreadPoolExecutor getExecutor() {
        return executor;
    }
    
    // helpers
    
    private static void printSceneGraph(Node root) {
        printSceneGraph(root, 0);
    }
    
    private static void printSceneGraph(Spatial root, int indent) {
        String s = "";
        for(int i = 0; i < indent; i++) {
            s += "  ";
        }
        System.out.println(s + root.toString());
        if(!(root instanceof Node))
            return;
        for(Spatial c : ((Node) root).getChildren()) {
            printSceneGraph(c, indent + 1);
        }
    }
}
