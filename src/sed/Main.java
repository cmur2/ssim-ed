package sed;

import java.io.IOException;
import java.io.InputStream;

import org.jdom.Element;

import sed.weather.BasicWeatherController;
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

/**
 * ÄÖÜ
 */
public class Main extends SimpleApplication {
    
    public static void main(String[] args) {
        Main main = new Main();
        main.setShowSettings(false);
        main.start();
    }
    
    // TODO: Key z -> y
    // TODO: java.util.logging to Log4J bridge
    
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
//        Element root = assetManager.loadAsset(new AssetKey<Element>("weather/default.xml"));
        weatherController = new StaticWeatherController();
        weatherController.registerProperty("sky.turbidity", 2f, Float.class);
    }
    
    private String getTextByPropName(Element root, String key) {
        Element cur = root;
        String[] parts = key.split("\\.");
        for(int i = 0; i < parts.length; i++) {
//            System.out.println(childreen[i]);
            cur = cur.getChild(parts[i]);
            if(cur == null) return null;
        }
        return cur.getText();
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
