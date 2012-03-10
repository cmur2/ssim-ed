package sed.app;

import sed.MapLoader;
import sed.terrain.BinaryMapTileLoader;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetKey;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainGrid;
import com.jme3.terrain.geomipmap.TerrainGridTileLoader;
import com.jme3.terrain.geomipmap.TerrainLodControl;

public class TerrainAppState extends BasicAppState {
    
    private static final float UpdateInterval = 30f; // in seconds
    
    // exists only while AppState is attached
    //private TerrainQuad terrainRoot;
    
    public TerrainAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        AssetKey<MapLoader.Map> mapKey = new AssetKey<MapLoader.Map>("maps/"+getApp().getMission().getMapFile());
        MapLoader.Map map = getApp().getAssetManager().loadAsset(mapKey);
        //System.out.println(map);
                
        Material mat = new Material(getApp().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Brown);
        
        TerrainGridTileLoader loader = new BinaryMapTileLoader(map);
        
        TerrainGrid grid = new TerrainGrid("TerrainGrid", 65, 257, loader);
        grid.setMaterial(mat);
        grid.setLocalTranslation(0, 0, 0);
        grid.setLocalScale(1f, 1f, 1f);
        
        grid.addControl(new TerrainLodControl(grid, getApp().getCamera()));
        //grid.initialize(getApp().getCamera().getLocation());
        grid.initialize(new Vector3f());

        getApp().getRootNode().attachChild(grid);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        //terrainRoot = null;
    }
}
