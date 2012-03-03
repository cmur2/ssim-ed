package sed.app;

import sed.MapLoader;
import sed.MapLoader.Map;
import jme3tools.converters.ImageToAwt;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetKey;
import com.jme3.material.Material;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;

/**
 * <b>Higher layer</b> {@link AppState} responsible for rendering terrain.
 * 
 * @author cn
 */
public class TerrainAppState extends BasicAppState {
    
    private static final float UpdateInterval = 30f; // in seconds
    
    // exists only while AppState is attached
    private TerrainQuad terrainRoot;
    
    public TerrainAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        AssetKey<MapLoader.Map> mapKey = new AssetKey<MapLoader.Map>("maps/" + getApp().getMission().getMapFile());
        MapLoader.Map map = getApp().getAssetManager().loadAsset(mapKey);
        //System.out.println(map);
        
        // TODO: build TerraMoney glue
        
        Material matRock = new Material(getApp().getAssetManager(), "Common/MatDefs/Terrain/Terrain.j3md");
        matRock.setBoolean("useTriPlanarMapping", false);
        
        // GRASS texture
        Texture grass = getApp().getAssetManager().loadTexture("Textures/Terrain/splat/grass.jpg");
        grass.setWrap(WrapMode.Repeat);
        matRock.setTexture("Tex1", grass);
        matRock.setFloat("Tex1Scale", 64);
        
        // DIRT texture
        Texture dirt = getApp().getAssetManager().loadTexture("Textures/Terrain/splat/dirt.jpg");
        dirt.setWrap(WrapMode.Repeat);
        matRock.setTexture("Tex2", dirt);
        matRock.setFloat("Tex2Scale", 32);
        
        // ROCK texture
        Texture rock = getApp().getAssetManager().loadTexture("Textures/Terrain/splat/road.jpg");
        rock.setWrap(WrapMode.Repeat);
        matRock.setTexture("Tex3", rock);
        matRock.setFloat("Tex3Scale", 128);
        
        // ALPHA map (for splat textures)
        matRock.setTexture("Alpha", getApp().getAssetManager().loadTexture("Textures/Terrain/splat/alphamap.png"));
        
        
        Texture heightMapImage = getApp().getAssetManager().loadTexture("Textures/Terrain/splat/mountains512.png");
        
        java.awt.Image heightMapImageAwt = ImageToAwt.convert(heightMapImage.getImage(), false, false, 0);
        
        AbstractHeightMap heightmap = null;
        try {
            heightmap = new ImageBasedHeightMap(heightMapImageAwt, 1f);
            heightmap.load();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        
        terrainRoot = new TerrainQuad("terrain", 65, 513, heightmap.getHeightMap());
        TerrainLodControl control = new TerrainLodControl(terrainRoot, getApp().getCamera());
        control.setLodCalculator(new DistanceLodCalculator(65, 2.7f));
        terrainRoot.addControl(control);
        terrainRoot.setMaterial(matRock);
        //terrainRoot.setLocalTranslation(0, -100, 0);
        //terrainRoot.setLocalScale(2f, 1f, 2f);
        getApp().getRootNode().attachChild(terrainRoot);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        terrainRoot = null;
    }
}
