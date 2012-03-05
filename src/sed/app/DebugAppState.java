package sed.app;

import sed.DebugGridMesh;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.debug.Arrow;

public class DebugAppState extends BasicAppState {
    
    public static final String INPUT_MAPPING_SHOW_GRID = "SED_ShowGrid";
    
    private boolean showGrid;
    private Node debugNode;
    private InputHandler handler;
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        showGrid = false;
        
        debugNode = new Node("DebugNode");
        debugNode.setCullHint(showGrid ? CullHint.Never : CullHint.Always);
        getApp().getRootNode().attachChild(debugNode);
        
        buildDebugGrid(1000f, 10, 10);
        
        buildTheThreeArrows(50f);
        
        buildTheFourWinds();
        
        handler = new InputHandler();
        getApp().getInputManager().addMapping(INPUT_MAPPING_SHOW_GRID, new KeyTrigger(KeyInput.KEY_F6));
        getApp().getInputManager().addListener(handler, INPUT_MAPPING_SHOW_GRID);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getApp().getRootNode().detachChild(debugNode);
        getApp().getInputManager().deleteMapping(INPUT_MAPPING_SHOW_GRID);
        getApp().getInputManager().removeListener(handler);
        
        debugNode = null;
        handler = null;
    }
    
    private void buildDebugGrid(float gridSize, int gridWidth, int gridHeight) {
        DebugGridMesh debugGrid = new DebugGridMesh(gridSize, gridWidth, gridHeight);
        Geometry debugGridGeom = new Geometry("DebugGrid", debugGrid);
        Material mat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setBoolean("VertexColor", true);
        debugGridGeom.setMaterial(mat);
        
        debugNode.attachChild(debugGridGeom);
    }
    
    private void buildTheThreeArrows(float size) {
        Geometry[] theThreeArrows = new Geometry[3];
        theThreeArrows[0] = buildOneArrow("XAxisArrow", new Vector3f(size, 0, 0), ColorRGBA.Red);
        theThreeArrows[1] = buildOneArrow("YAxisArrow", new Vector3f(0, size, 0), ColorRGBA.Green);
        theThreeArrows[2] = buildOneArrow("ZAxisArrow", new Vector3f(0, 0, size), ColorRGBA.Blue);
        
        for(Geometry arrow : theThreeArrows) {
            debugNode.attachChild(arrow);
        }
    }
    
    private Geometry buildOneArrow(String name, Vector3f direction, ColorRGBA color) {
        Arrow arrow = new Arrow(direction);
        arrow.setLineWidth(1.5f);
        Geometry geom = new Geometry(name, arrow);
        Material mat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        geom.setMaterial(mat);
        geom.setLocalTranslation(0, 0.2f, 0);
        return geom;
    }
    
    private void buildTheFourWinds() {
        float radius = getState(SkyAppState.class).getHemisphereRadius();
        radius *= 0.95f;
        
        BitmapText[] theFourWinds = new BitmapText[4];
        BitmapFont font = getApp().getAssetManager().loadFont("Interface/Fonts/Default.fnt");
        theFourWinds[0] = buildOneDirection("North", new Vector3f(0,0,-radius), font);
        theFourWinds[1] = buildOneDirection("East", new Vector3f(radius,0,0), font);
        theFourWinds[2] = buildOneDirection("South", new Vector3f(0,0,radius), font);
        theFourWinds[3] = buildOneDirection("West", new Vector3f(-radius,0,0), font);
        
        for(BitmapText bt : theFourWinds) {
            debugNode.attachChild(bt);
        }
    }
    
    private BitmapText buildOneDirection(String text, Vector3f translation, BitmapFont font) {
        BitmapText labelText = new BitmapText(font);
        labelText.setText(text);
        
        labelText.setQueueBucket(Bucket.Transparent);
        labelText.setLocalTranslation(translation);
        labelText.move(0, labelText.getLineHeight(), 0);
        
        BillboardControl bbControl = new BillboardControl();
        bbControl.setAlignment(BillboardControl.Alignment.Screen);
        labelText.addControl(bbControl);
        
        return labelText;
    }
    
    private class InputHandler implements ActionListener {
        @Override
        public void onAction(String name, boolean isPressed, float tpf) {
            if(!isPressed) {
                return;
            }
            if(name.equals(INPUT_MAPPING_SHOW_GRID)) {
                showGrid = !showGrid;
                debugNode.setCullHint(showGrid ? CullHint.Never : CullHint.Always);
            }
        }
    }
}
