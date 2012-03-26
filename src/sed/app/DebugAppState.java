package sed.app;

import sed.DebugGridMesh;
import sed.util.TempVars;
import sed.weather.WindRose;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Sphere;

public class DebugAppState extends BasicAppState {
    
    public static final String INPUT_MAPPING_SHOW_GRID = "SED_ShowGrid";
    
    private static final float UpdateInterval = 0.5f; // in seconds
    
    private static final float StandardRadius = 50f; // in m
    private static final float GridStep = 100f; // in m
    
    private boolean showGrid;
    private Node debugNode;
    private Geometry windRoseGeom;
    private InputHandler handler;
    
    public DebugAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        showGrid = false;
        
        debugNode = new Node("DebugNode");
        debugNode.setCullHint(showGrid ? CullHint.Never : CullHint.Always);
        getApp().getRootNode().attachChild(debugNode);
        
        buildDebugGrid(GridStep);
        
        buildTheThreeArrows(StandardRadius);
        
        buildTheFourWinds();
        
        buildWindRose(1f);
        
        //buildLightDebugSphere();
        
        intervalUpdate();
        
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
    
    private void buildDebugGrid(float gridStep) {
        float gridSize = getState(SkyAppState.class).getHemisphereRadius();
        int gridWH = Math.round(gridSize/gridStep);
        
        DebugGridMesh debugGrid = new DebugGridMesh(gridSize, gridWH, gridWH);
        Geometry debugGridGeom = new Geometry("DebugGrid", debugGrid);
        Material mat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Gray);
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
        labelText.setName("Label"+text);
        labelText.setText(text);
        
        labelText.setQueueBucket(Bucket.Transparent);
        labelText.setLocalTranslation(translation);
        labelText.move(0, labelText.getLineHeight(), 0);
        
        BillboardControl bbControl = new BillboardControl();
        bbControl.setAlignment(BillboardControl.Alignment.Screen);
        labelText.addControl(bbControl);
        
        return labelText;
    }
    
    private void buildWindRose(float size) {
        WindRose rose = new WindRose(size);
        rose.setLineWidth(4f);
        Geometry geom = new Geometry("WindRose", rose);
        Material mat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Brown.add(ColorRGBA.DarkGray));
        geom.setMaterial(mat);
        geom.setLocalTranslation(0, StandardRadius/2, 0);
        windRoseGeom = geom;
        debugNode.attachChild(geom);
    }
    
    private void buildLightDebugSphere() {
        Sphere rockSphere = new Sphere(32,32, 2f);
        Geometry rockGeom = new Geometry("LightSphere", rockSphere);
        rockSphere.setTextureMode(Sphere.TextureMode.Projected);
        com.jme3.util.TangentBinormalGenerator.generate(rockSphere);
        Material rockMat = new Material(getApp().getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        rockMat.setColor("Diffuse", ColorRGBA.White);
        rockMat.setTexture("DiffuseMap", getApp().getAssetManager().loadTexture("Textures/Terrain/Pond/Pond.jpg"));
        rockMat.setTexture("NormalMap", getApp().getAssetManager().loadTexture("Textures/Terrain/Pond/Pond_normal.png"));
        rockMat.setFloat("Shininess", 5f); // [1,128]
        rockGeom.setMaterial(rockMat);
        rockGeom.rotate(1.6f, 0, 0);
        rockGeom.scale(10f);
        debugNode.attachChild(rockGeom);
    }
    
    @Override
    protected void intervalUpdate() {
        updateWindRose();
    }
    
    private void updateWindRose() {
        float direction = getState(WeatherAppState.class).getWeather().getFloat("wind.direction");
        float strength = getState(WeatherAppState.class).getWeather().getFloat("wind.strength");
        TempVars vars = TempVars.get();
        // rotate according to direction
        Matrix3f rot = vars.mat1;
        rot.fromAngleNormalAxis(direction * FastMath.DEG_TO_RAD, vars.vect1.set(0, -1, 0));
        windRoseGeom.setLocalRotation(rot);
        // scale according to strength
        windRoseGeom.setLocalScale(strength*5 + StandardRadius);
        vars.release();
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
