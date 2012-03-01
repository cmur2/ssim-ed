package sed.terrain;

import sed.MapLoader;

import com.jme3.terrain.heightmap.HeightMap;

public class BinaryMapBasedHeightMap implements HeightMap {

    private MapLoader.Map map;

    private float[] heightMap = null;
    private int size = 0;
    
    /** Allows scaling the Y height of the map. */
    private float heightScale = 1.0f;
    /** The filter is used to erode the terrain. */
    private float filter = 0.5f;
    
    // TODO: how to specify size/map rerastering/clipping?
    public BinaryMapBasedHeightMap(MapLoader.Map map) {
        this.map = map;
    }
    
    /** {@inheritDoc} */
    @Override
    public float[] getHeightMap() {
        return heightMap;
    }

    /** {@inheritDoc} */
    @Override
    public float[] getScaledHeightMap() {
        float[] scaledHeightMap = new float[heightMap.length];
        for(int i = 0; i < heightMap.length; i++) {
            scaledHeightMap[i] = heightScale * heightMap[i];
        }
        return scaledHeightMap;
    }

    /** {@inheritDoc} */
    @Override
    public float getTrueHeightAtPoint(int x, int z) {
        // TODO: rlly?
        return heightMap[x + z*size];
    }

    /** {@inheritDoc} */
    @Override
    public float getScaledHeightAtPoint(int x, int z) {
        return getTrueHeightAtPoint(x, z) * heightScale;
    }

    /** {@inheritDoc} */
    @Override
    public float getInterpolatedHeight(float x, float z) {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void setHeightAtPoint(float height, int x, int z) {
        heightMap[x + z*size] = height;
    }

    /** {@inheritDoc} */
    @Override
    public int getSize() {
        return size;
    }

    /** {@inheritDoc} */
    @Override
    public void setSize(int size) throws Exception {
        if(size <= 0) {
            throw new Exception("Size must be greater than 0!");
        }
        this.size = size;
    }

    /** {@inheritDoc} */
    @Override
    public void setHeightScale(float heightScale) {
        this.heightScale = heightScale;
    }

    /** {@inheritDoc} */
    @Override
    public void setMagnificationFilter(float filter) throws Exception {
        if(filter < 0f || filter > 1f) {
            throw new Exception("Magnification filter must be between 0 and 1!");
        }
        this.filter = filter;
    }

    /** {@inheritDoc} */
    @Override
    public boolean load() {
        
        heightMap = BinaryMapManipulator.crop(map, 65);
        size = 65;
        
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void unloadHeightMap() {
        heightMap = null;
    }
}
