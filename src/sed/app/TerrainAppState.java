package sed.app;

import java.util.Arrays;

import sed.terrain.BinaryMap;
import sed.terrain.BinaryMapTileLoader;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetKey;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainGrid;
import com.jme3.terrain.geomipmap.TerrainGridTileLoader;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;

public class TerrainAppState extends BasicAppState {
    
    private static final float UpdateInterval = 30f; // in seconds
    
    private static final int PatchSize = 128 + 1;
    private static final int MaxVisibleSize = 4 * (PatchSize-1) + 1;
    private static final float LODMultiplier = 2.7f;
    
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
        
        Material mat = new Material(getApp().getAssetManager(), "shaders/TerrainAtlas.j3md");
        // Attach a lookup table texture that maps (slope,altitude) tuples to
        // TerrainType IDs. Therefore no filtering/interpolation to the textures
        // values should be done since they represent discrete ID integers.
        {
            Texture lutTex = getApp().getAssetManager().loadTexture("textures/TerrainLUT.png");
            lutTex.setMinFilter(MinFilter.NearestNoMipMaps);
            lutTex.setMagFilter(MagFilter.Nearest);
            mat.setTexture("TerrainLUT", lutTex);
        }
        // Attach a texture atlas that contains multiple textures via sub-tiling.
        // The TerrainType ID will be used to select the matching texture for
        // the given TerrainType.
        {
            Texture taTex = getApp().getAssetManager().loadTexture("textures/TerrainAtlas.png");
            // Temporary hack to prevent mipmap usage on GPU, we could use
            // NearestNoMipMaps too:
            taTex.setMinFilter(MinFilter.BilinearNoMipMaps);
            mat.setTexture("TerrainAtlas", taTex);
        }
        // The inverse maximum altitude (same as used in TerrainLUT generation!)
        // is needed to bring the altitude (in m) down to [0,1]
        mat.setFloat("InvMaxAltitude", 1f/sed.pre.TerrainLUTGenerator.MaxAltitude);
        // Pass some additional parameters describing the atlas properties to
        // the shader that it needs to perform valid texture lookups on the atlas
        {
            Vector3f atlasParameters = new Vector3f();
            // x: nTilesWidth (== nTilesHeight)
            // y: 1 / nTilesWidth
            // z: 1 / width (== 1/height)
            atlasParameters.x = sed.pre.TerrainAtlasGenerator.NumTiles;
            atlasParameters.y = 1f/atlasParameters.x;
            atlasParameters.z = 1f/sed.pre.TerrainAtlasGenerator.TexSize;
            mat.setVector3("AtlasParameters", atlasParameters);
        }
        
        final float sampleDistance = (float) (map.weDiff + map.nsNum)/2f * 0.5f;
        
        TerrainGridTileLoader loader = new BinaryMapTileLoader(map, sampleDistance);
        
        terrainGrid = new TerrainGrid("TerrainGrid", PatchSize, MaxVisibleSize, loader);
        terrainGrid.setMaterial(mat);
        terrainGrid.setLocalTranslation(0, 0, 0);
        terrainGrid.setLocalScale(sampleDistance);
        
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
