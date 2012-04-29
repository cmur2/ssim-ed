package de.mycrobase.ssim.ed.app;

import java.util.Arrays;


import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetKey;
import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainGrid;
import com.jme3.terrain.geomipmap.TerrainGridTileLoader;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;

import de.mycrobase.ssim.ed.terrain.BinaryMap;
import de.mycrobase.ssim.ed.terrain.BinaryMapTileLoader;
import de.mycrobase.ssim.ed.util.TempVars;

public class TerrainAppState extends BasicAppState {
    
    private static final float UpdateInterval = 10f; // in seconds
    
    private static final int PatchSize = 128 + 1;
    private static final int MaxVisibleSize = 4 * (PatchSize-1) + 1;
    private static final float LODMultiplier = 2.7f;
    
    // exists only while AppState is attached
    private TerrainGrid terrainGrid;
    private Material terrainMat;
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
        
        terrainMat = new Material(getApp().getAssetManager(), "shaders/TerrainAtlas.j3md");
        // Attach a lookup table texture that maps (slope,altitude) tuples to
        // TerrainType IDs. Therefore no filtering/interpolation to the textures
        // values should be done since they represent discrete ID integers.
        {
            Texture lutTex = getApp().getAssetManager().loadTexture("textures/TerrainLUT.png");
            lutTex.setMinFilter(MinFilter.NearestNoMipMaps);
            lutTex.setMagFilter(MagFilter.Nearest);
            terrainMat.setTexture("TerrainLUT", lutTex);
        }
        // Attach a texture atlas that contains multiple textures via sub-tiling.
        // The TerrainType ID will be used to select the matching texture for
        // the given TerrainType.
        {
            Texture taTex = getApp().getAssetManager().loadTexture("textures/TerrainAtlas.png");
            // Temporary hack to prevent mipmap usage on GPU, we could use
            // NearestNoMipMaps too:
            taTex.setMinFilter(MinFilter.BilinearNoMipMaps);
            terrainMat.setTexture("TerrainAtlas", taTex);
        }
        // Attach a noise texture (in only 1 channel, Red) to allow shader access
        // to pseudo random noise data
        terrainMat.setTexture("TerrainNoise", getApp().getAssetManager().loadTexture("textures/TerrainNoise.png"));
        // The inverse maximum altitude (same as used in TerrainLUT generation!)
        // is needed to bring the altitude (in m) down to [0,1]
        terrainMat.setFloat("InvMaxAltitude", 1f/de.mycrobase.ssim.ed.pre.TerrainLUTGenerator.MaxAltitude);
        // Pass some additional parameters describing the atlas properties to
        // the shader that it needs to perform valid texture lookups on the atlas
        {
            Vector3f atlasParameters = new Vector3f();
            // x: nTilesWidth (== nTilesHeight)
            // y: 1 / nTilesWidth
            // z: 1 / width (== 1/height)
            atlasParameters.x = de.mycrobase.ssim.ed.pre.TerrainAtlasGenerator.NumTiles;
            atlasParameters.y = 1f/atlasParameters.x;
            atlasParameters.z = 1f/de.mycrobase.ssim.ed.pre.TerrainAtlasGenerator.TexSize;
            terrainMat.setVector3("AtlasParameters", atlasParameters);
        }
        // Pass factors for noise influence on (slope, altitude)
        {
            Vector2f noiseParameters = new Vector2f(0.2f, 0.3f);
            // x: slope weight
            // y: altitude weight
            terrainMat.setVector2("NoiseParameters", noiseParameters);
        }
        // Pass fog parameters into shader necessary for Fog.glsllib
        updateFog();
        
        final float sampleDistance = (float) (map.weDiff + map.nsNum)/2f * 0.5f;
        
        TerrainGridTileLoader loader = new BinaryMapTileLoader(map, sampleDistance);
        
        terrainGrid = new TerrainGrid("TerrainGrid", PatchSize, MaxVisibleSize, loader);
        terrainGrid.setMaterial(terrainMat);
        terrainGrid.setLocalTranslation(0, 0, 0);
        terrainGrid.setLocalScale(sampleDistance);
        
        lodControl = new TerrainLodControl(terrainGrid, Arrays.asList(getApp().getCamera()));
        lodControl.setLodCalculator(new DistanceLodCalculator(PatchSize, LODMultiplier));
        terrainGrid.addControl(lodControl);
        
        terrainGrid.initialize(getApp().getCamera().getLocation());

        getApp().getRootNode().attachChild(terrainGrid);
    }
    
    @Override
    protected void intervalUpdate() {
        updateFog();
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        terrainGrid.removeControl(lodControl);
        getApp().getRootNode().detachChild(terrainGrid);
        
        terrainGrid = null;
    }
    
    private void updateFog() {
        TempVars vars = TempVars.get();
        float[] color = getSkyAppState().getSkyGradient().getSkyColor(0,0,-1, vars.float1);
        //System.out.println(java.util.Arrays.toString(color));
        terrainMat.setVector3("FogColor", new Vector3f(color[0], color[1], color[2]));
        
        float maxDist = getSkyAppState().getHemisphereRadius();
        // fogFactor:
        //   1.0 - full original color
        //   0.0 - full fog color
        // TODO: modify via sky.turbidity later
        float density = getFogDensity(0.75f, maxDist);
        terrainMat.setFloat("FogDensity", density);
        vars.release();
    }
    
    /**
     * Calculates the necessary distance value so that EXP2 fog has the given
     * targetFogFactor at the given distance.
     * 
     * @param targetFogFactor wished fog factor
     * @param maxDist at this distance
     * @return necessary fog density
     */
    private float getFogDensity(float targetFogFactor, float maxDist) {
        return (float) (
            Math.sqrt(1.0 / (maxDist*maxDist)) *
            Math.sqrt(Math.log(1.0 / targetFogFactor))
            );
    }
    
    private SkyAppState getSkyAppState() {
        return getState(SkyAppState.class);
    }
}