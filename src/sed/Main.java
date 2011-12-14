package sed;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import sed.weather.PropertySetBuilder;
import sed.weather.StaticWeatherController;
import sed.weather.WeatherController;
import ssim.sim.SimClock;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoader;
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
        //BasicConfigurator.configure();
        {
            Logger root = Logger.getRootLogger();
            root.addAppender(new ConsoleAppender(new PatternLayout("%-3r [%t] %-5p %c: %m%n")));
        }
        // TODO: does not forward throwables correctly to log4j
//        JLFBridge.installBridge();
        
        Main main = new Main();
        main.setShowSettings(false);
        main.start();
    }
    
    // TODO: Key z -> y
    
    private float time = 0;
    
    public SimClock clock;
    public WeatherController weatherController; 
    
    @Override
    public void simpleInitApp() {
        assetManager.registerLoader(XMLLoader.class, "xml");
        
        clock = SimClock.createClock(11.00f);
        assert clock != null : "SimClock init failed - wrong parameters!";
        
        initWeather();
        
        flyCam.setMoveSpeed(10 * 6);
        //flyCam.setDragToRotate(true);
        
        Node skyNode = new Node("SkyNode");
        skyNode.setCullHint(CullHint.Never);
        rootNode.attachChild(skyNode);
        
        stateManager.attach(new SkyAppState());
        
        Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        Geometry geom = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        geom.setMaterial(mat);
        //rootNode.attachChild(geom);
        
        printSceneGraph(rootNode);
    }
    
    private void initWeather() {
        PropertySetBuilder builder = new PropertySetBuilder(assetManager, "default");
        builder.put("sky.turbidity", Float.class);
        builder.put("cloud.cover", Integer.class);
        builder.put("wind", Vector3f.class);
        builder.put("prime", Integer[].class);
        
        weatherController = new StaticWeatherController(builder.getResult());
        
        System.out.println(weatherController.getVec3("wind"));
        System.out.println(java.util.Arrays.toString(weatherController.getIntArray("prime")));
    }
    
    @Override
    public void simpleUpdate(float dt) {
        if(time > 2) {
            time = 0;
        }
        
        time += dt;
        clock.step(dt);
    }
    
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
