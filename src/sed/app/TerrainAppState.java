package sed.app;

import java.nio.ByteBuffer;
import java.util.Arrays;

import javax.vecmath.Color3f;

import sed.MapLoader;
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
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;

public class TerrainAppState extends BasicAppState {
    
    private static final float UpdateInterval = 30f; // in seconds
    
    private static final int PatchSize = 129;
    private static final int MaxVisibleSize = 257;
    private static final float LODMultiplier = 5f;
    private static final float TerrainScale = 1/25f;
    
    private static final int HeightGradientTexSize = 256; // in px
    private static final float MeterPerTexel = 10f;
    
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
        AssetKey<MapLoader.Map> mapKey = new AssetKey<MapLoader.Map>(path);
        MapLoader.Map map = getApp().getAssetManager().loadAsset(mapKey);
        
        // TODO: implement terrain shader(s)
        Material mat = new Material(getApp().getAssetManager(), "shaders/TerrainGradient.j3md");
        mat.setTexture("HeightGradient", generateHeightGradient());
        mat.setFloat("InvHeightGradientTexWidth", 1f/HeightGradientTexSize);
        mat.setFloat("InvMeterPerTexel", 1f/MeterPerTexel);
        
        TerrainGridTileLoader loader = new BinaryMapTileLoader(map);
        
        terrainGrid = new TerrainGrid("TerrainGrid", PatchSize, MaxVisibleSize, loader);
        terrainGrid.setMaterial(mat);
        terrainGrid.setLocalTranslation(0, 0, 0);
        terrainGrid.setLocalScale(map.woDiff * TerrainScale, TerrainScale, map.nsDiff * TerrainScale);
        
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
    
    private Texture2D generateHeightGradient() {
        int texSize = HeightGradientTexSize;
        int inMid = texSize/4;
        int[] colors = new int[texSize * 1 * 3];
        
        for(int i = 0; i < inMid-10; i++) {
            colors[i*3] = 48;
            colors[i*3+1] = 26;
            colors[i*3+2] = 16;
        }
        // Slope
        for(int i = inMid-10; i < inMid-5; i++) {
            colors[i*3] = 96;
            colors[i*3+1] = 64;
            colors[i*3+2] = 0;
        }
        // Strand
        for(int i = inMid-5; i < inMid+1; i++) {
            colors[i*3] = 240;
            colors[i*3+1] = 192;
            colors[i*3+2] = 64;
        }
        // Gras
        for(int i = inMid+1; i < texSize-1; i++) {
            colors[i*3] = 144;
            colors[i*3+1] = 208;
            colors[i*3+2] = 64;
        }
        colors[(256-1)*3] = 96;
        colors[(256-1)*3+1] = 112;
        colors[(256-1)*3+2] = 128;
        
        // Generate weight for 1-D gaussian blur
        final float sigma = 1.0f;
        final float ZwoSigma2 = 2f*sigma*sigma;
        final float kwFactor = 1f / (float) Math.pow(Math.PI*ZwoSigma2, 0.5);
        // at any distance > 3*sigma the weight < per mill, so irrelevant
        float[] kernelWeights = new float[(int)Math.ceil(3*sigma)];
        for(int i = 0; i < kernelWeights.length; i++) {
            kernelWeights[i] = kwFactor * (float) Math.exp(-i*i / ZwoSigma2);
            //System.out.println(kernelWeights[i]);
        }
        
        ByteBuffer colorsBlurred = BufferUtils.createByteBuffer(texSize * 1 * 3);
        
        // Apply gaussian blur
        Color3f sum = new Color3f();
        Color3f add = new Color3f();
        for(int i = 0; i < texSize; i++) {
            sum.set(colors[i*3], colors[i*3+1], colors[i*3+2]);
            sum.scale(kernelWeights[0]);
            for(int j = 1; j < kernelWeights.length; j++) {
                if(i-j < 0) {
                    add.set(colors[(i+j)*3], colors[(i+j)*3+1], colors[(i+j)*3+2]);
                    add.scale(2);
                } else if(i+j >= texSize) {
                    add.set(colors[(i-j)*3], colors[(i-j)*3+1], colors[(i-j)*3+2]);
                    add.scale(2);
                } else {
                    add.set(colors[(i+j)*3], colors[(i+j)*3+1], colors[(i+j)*3+2]);
                    add.scale(kernelWeights[j]);
                    sum.add(add);
                    add.set(colors[(i-j)*3], colors[(i-j)*3+1], colors[(i-j)*3+2]);
                }
                add.scale(kernelWeights[j]);
                sum.add(add);
            }
            colorsBlurred.put((byte) sum.x);
            colorsBlurred.put((byte) sum.y);
            colorsBlurred.put((byte) sum.z);
        }
        
        Image img = new Image(Image.Format.RGB8, texSize, 1, colorsBlurred);
        return new Texture2D(img);
    }
}
