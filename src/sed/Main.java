package sed;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import sed.app.CameraAppState;
import sed.app.CloudAppState;
import sed.app.DebugAppState;
import sed.app.GuiAppState;
import sed.app.LightingAppState;
import sed.app.SkyAppState;
import sed.app.SkyDomeAppState;
import sed.app.StarAppState;
import sed.app.SunAppState;
import sed.app.WeatherAppState;
import sed.mission.Mission;
import sed.mission.MissionParser;
import ssim.sim.SimClock;
import chlib.noise.NoiseUtil;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;

import de.altimos.util.logger.JLFBridge;

public class Main extends SimpleApplication {
    
    private static final Logger logger = Logger.getLogger(Main.class);
    private static final float UpdateInterval = 5f; // in seconds
    
    private static final float MaxVisibility = 2000f;
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
    
    private ScheduledExecutorService executor;
    private Mission mission;
    private SimClock simClock;
    
    @Override
    public void simpleInitApp() {
        // TODO: Settings system
        
        // TODO: Initialize NoiseUtil with random seed
        NoiseUtil.reinitialize(DebugSeed);
        
        assetManager.registerLoader(XMLLoader.class, "xml");
        assetManager.registerLoader(MapLoader.class, "map");
        
        int numWorker = (int) (1.5f * Runtime.getRuntime().availableProcessors());
        logger.info(String.format("Worker thread pool size: %d", numWorker));
        executor = Executors.newScheduledThreadPool(numWorker);
        
        initMission();
        
        simClock = SimClock.createClock(getMission().getTimeOfDay());
        assert simClock != null : "SimClock init failed - wrong parameters!";
        
        speed = 1f;
        
        // AppState base layer:
        // these serve as a common base for the higher AppStates
        stateManager.attach(new CameraAppState(MaxVisibility));
        stateManager.attach(new WeatherAppState("clear"));
        stateManager.attach(new SkyAppState(0.5f*MaxVisibility));
        
        // AppState higher layer:
        // these have no dependencies to each other, just to the base layer
        stateManager.attach(new SkyDomeAppState());
        stateManager.attach(new SunAppState());
        stateManager.attach(new LightingAppState());
        stateManager.attach(new StarAppState());
        stateManager.attach(new CloudAppState());
        //stateManager.attach(new TerrainAppState());
        stateManager.attach(new GuiAppState());
        stateManager.attach(new DebugAppState());
        
        {
            //Box boxBox = new Box(Vector3f.ZERO, 1, 1, 1);
            //Geometry boxGeom = new Geometry("Box", boxBox);
            //Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            //boxMat.setColor("Color", ColorRGBA.Blue);
            //boxGeom.setMaterial(boxMat);
            //rootNode.attachChild(boxGeom);
        }
        
        {
            //Sphere rockSphere = new Sphere(32,32, 2f);
            //Geometry rockGeom = new Geometry("Shiny rock", rockSphere);
            //rockSphere.setTextureMode(Sphere.TextureMode.Projected);
            //com.jme3.util.TangentBinormalGenerator.generate(rockSphere);
            //Material rockMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
            //rockMat.setColor("Diffuse", ColorRGBA.White);
            //rockMat.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"));
            //rockMat.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"));
            //rockMat.setFloat("Shininess", 5f); // [1,128]
            //rockGeom.setMaterial(rockMat);
            //rockGeom.rotate(1.6f, 0, 0);
            //rockGeom.scale(10f);
            //rootNode.attachChild(rockGeom);
        }
    }
    
    private void initMission() {
        mission = MissionParser.load(assetManager, "missions/mission_01.xml");
    }
    
    @Override
    public void simpleUpdate(float dt) {
        if(time > UpdateInterval) {
            time -= UpdateInterval;
            // ...
            //Util.printSceneGraph(rootNode);
        }
        
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
    
    public ScheduledExecutorService getExecutor() {
        return executor;
    }
    
    public float getSpeed() {
        return speed;
    }
    
    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
