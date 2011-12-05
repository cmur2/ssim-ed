package sed;

import ssim.sim.SimClock;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;

public class Main extends SimpleApplication {
    
    public static void main(String[] args) {
        Main main = new Main();
        main.setShowSettings(false);
        main.start();
    }
    
    // TODO: Key z -> y
    
    private float time = 0;
    public SimClock clock;
    
    @Override
    public void simpleInitApp() {
        clock = SimClock.createClock(11.00f);
        assert clock != null : "SimClock init failed - wrong parameters!";
        
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
    
    @Override
    public void simpleUpdate(float tpf) {
        if(time > 2) {
            time = 0;
        }
        
        time += tpf;
        clock.step(tpf);
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
