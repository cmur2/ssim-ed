package sed;

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

import de.altimos.util.logger.JLFBridge;

public class Main extends SimpleApplication {
    
    private static final Logger logger = Logger.getLogger(Main.class);
    
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
    
    private Mission mission;
    private SimClock simClock;
    private WeatherController weatherController;
    private Node skyNode;
    private Sun sun;
    private SkyGradient skyGradient;
    
    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(XMLLoader.class, "xml");
        inputManager.deleteTrigger("FLYCAM_Lower", new KeyTrigger(KeyInput.KEY_Z));
        inputManager.addMapping("FLYCAM_Lower", new KeyTrigger(KeyInput.KEY_Y));
        
        initMission();
        
        simClock = SimClock.createClock(getMission().getTimeOfDay());
        assert simClock != null : "SimClock init failed - wrong parameters!";
        
        speed = 10f;
        
        initWeather();
        
        flyCam.setMoveSpeed(10 * 6);
        //flyCam.setDragToRotate(true);
        cam.setLocation(Vector3f.ZERO);
        
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
        
        Box boxBox = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry boxGeom = new Geometry("Box", boxBox);
        Material boxMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        boxMat.setColor("Color", ColorRGBA.Blue);
        boxGeom.setMaterial(boxMat);
        //rootNode.attachChild(boxGeom);
        
        printSceneGraph(rootNode);
    }
    
    private void initMission() {
        mission = MissionParser.load(assetManager, "missions/mission_01.xml");
    }
    
    private void initWeather() {
//        String[] sets = {"clear", "snowy"};
        String[] sets = {"clear"};
        XMLPropertySetBuilder builder = new XMLPropertySetBuilder(assetManager, sets);
        builder.putFloat("sky.turbidity");
        builder.putBool("sun.lensflare-enabled");
        builder.putFloat("sun.lensflare-shininess");
//        builder.putInt("cloud.cover");
//        builder.putVec3("wind");
//        builder.putIntArray("prime");
        
        weatherController = new RandomWeatherController(5*60f, builder.getResults());
        weatherController.registerInterpolator(new Interpolators.FloatInterpolator(), Float.class);
        weatherController.registerInterpolator(new Interpolators.BoolInterpolator(), Boolean.class);
        
//        System.out.println(weatherController.getVec3("wind"));
//        System.out.println(java.util.Arrays.toString(weatherController.getIntArray("prime")));
    }
    
    @Override
    public void simpleUpdate(float dt) {
        if(time > 10f) {
            time = 0;
            //System.out.println(getWeather().getFloat("sky.turbidity"));
        }
        
        skyGradient.setTurbidity(getWeather().getFloat("sky.turbidity"));
        skyGradient.update();
        weatherController.update(dt);
        sun.update();
        
        time += dt;
        simClock.step(dt);
    }
    
    // simple getters
    
    public Mission getMission() {
        return mission;
    }
    
    public SimClock getSimClock() {
        return simClock;
    }
    
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
