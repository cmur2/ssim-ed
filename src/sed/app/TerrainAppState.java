package sed.app;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.vecmath.Color3f;

import sed.terrain.BinaryMap;
import sed.terrain.BinaryMapTileLoader;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetKey;
import com.jme3.material.Material;
import com.jme3.terrain.geomipmap.TerrainGrid;
import com.jme3.terrain.geomipmap.TerrainGridTileLoader;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;

public class TerrainAppState extends BasicAppState {
    
    private static final float UpdateInterval = 30f; // in seconds
    
    private static final int PatchSize = 129;
    private static final int MaxVisibleSize = 257;
    private static final float LODMultiplier = 5f;
    
    // exists only while AppState is attached
    private TerrainGrid terrainGrid;
    TerrainLodControl lodControl;
    
    public TerrainAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        String path = String.format("maps/%s", getApp().getMission().getMapFile());
        AssetKey<BinaryMap> mapKey = new AssetKey<BinaryMap>(path);
        BinaryMap map = getApp().getAssetManager().loadAsset(mapKey);
        
        // TODO: implement terrain shader(s)
        Material mat = new Material(getApp().getAssetManager(), "shaders/TerrainAtlas.j3md");
        Texture lutTex = getApp().getAssetManager().loadTexture("textures/TerrainLUT.png");
        lutTex.setMinFilter(MinFilter.NearestNoMipMaps);
        lutTex.setMagFilter(MagFilter.Nearest);
        mat.setTexture("TextureTable", lutTex);
        Texture taTex = getApp().getAssetManager().loadTexture("textures/TerrainTA.png");
        // Temporary hack to prevent mip map usage on GPU, we could use
        // NearestNoMipMaps too:
        taTex.setMinFilter(MinFilter.BilinearNoMipMaps);
        mat.setTexture("TextureAtlas", taTex);
        mat.setFloat("InvMaxAltitude", 1f/sed.pre.TerrainLUTGenerator.MaxAltitude);
        
//        Material mat = new Material(getApp().getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
//        mat.setColor("Diffuse", com.jme3.math.ColorRGBA.Green);
//        mat.setBoolean("UseMaterialColors", true);
        
        TerrainGridTileLoader loader = new BinaryMapTileLoader(map);
        
        terrainGrid = new TerrainGrid("TerrainGrid", PatchSize, MaxVisibleSize, loader);
        terrainGrid.setMaterial(mat);
        terrainGrid.setLocalTranslation(0, 0, 0);
        terrainGrid.setLocalScale((float) map.weDiff, 1f, (float) map.nsDiff);
        
        lodControl = new TerrainLodControl(terrainGrid, Arrays.asList(getApp().getCamera()));
        lodControl.setLodCalculator(new DistanceLodCalculator(PatchSize, LODMultiplier));
        terrainGrid.addControl(lodControl);
        
        terrainGrid.initialize(getApp().getCamera().getLocation());

        getApp().getRootNode().attachChild(terrainGrid);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        terrainGrid.removeControl(lodControl);
        getApp().getRootNode().detachChild(terrainGrid);
        
        terrainGrid = null;
    }
}
