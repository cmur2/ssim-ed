package de.mycrobase.ssim.ed.terrain;

import ssim.util.MathExt;

import com.jme3.math.Vector3f;
import com.jme3.terrain.heightmap.HeightMap;

public class BinaryMapBasedHeightMap implements HeightMap {

    private static final float DefaultY = -100f; // in m
    
    private BinaryMap map;
    private int offsetx;
    private int offsetz;
    private int quadSize;
    private float sampleDistance;

    private float[] heightMap = null;
    private int size = 0;
    
    /** Allows scaling the Y height of the map. */
    private float heightScale = 1.0f;
    /** The filter is used to erode the terrain. */
    private float filter = 0.5f;
    
    public BinaryMapBasedHeightMap(BinaryMap map,
            Vector3f offset, int quadSize, float sampleDistance)
    {
        this.map = map;
        this.offsetx = (int) offset.x;
        this.offsetz = (int) offset.z;
        this.quadSize = quadSize;
        this.sampleDistance = sampleDistance;
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
        heightMap = generateHeightMapSampled(sampleDistance);
        size = quadSize;
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void unloadHeightMap() {
        heightMap = null;
    }
    
    /** Unused */
    private float[] generateHeightMap() {
        float[] data = new float[quadSize*quadSize];
        for(int z = 0; z < quadSize; z++) {
            for(int x = 0; x < quadSize; x++) {
                int iz = offsetz * (quadSize-1) + z;
                int ix = offsetx * (quadSize-1) + x;
                data[x + z*quadSize] = getElevation(iz, ix);
            }
        }
        return data;
    }
    
    private float[] generateHeightMapSampled(float sampleDist) {
        float[] data = new float[quadSize*quadSize];
        for(int z = 0; z < quadSize; z++) {
            for(int x = 0; x < quadSize; x++) {
                int iz = offsetz * (quadSize-1) + z;
                int ix = offsetx * (quadSize-1) + x;
                // scale sample point coordinates by sampleDist, divide elevation
                // by sampleDist to allow uniform scale of whole geometry by
                // sampleDist
                data[x + z*quadSize] = getElevation(iz*sampleDist, ix*sampleDist)/sampleDist;
            }
        }
        return data;
    }
    
    private float getElevation(float z, float x) {
        int x0 = (int) (x / map.weDiff);
        int z0 = (int) (z / map.nsDiff);
        int x1 = x0 + 1;
        int z1 = z0 + 1;
        // y00 --- y01
        //  |       |
        // y10 --- y11
        float y00 = getElevation(z0, x0);
        float y01 = getElevation(z0, x1);
        float y10 = getElevation(z1, x0);
        float y11 = getElevation(z1, x1);
        float r1 = MathExt.frac(x / (float) map.weDiff);
        float r2 = MathExt.frac(z / (float) map.nsDiff);
        double y1 = MathExt.interpolateLinear(y00, y01, r1);
        double y2 = MathExt.interpolateLinear(y10, y11, r1);
        return (float) MathExt.interpolateLinear(y1, y2, r2);
//        double y1 = MathExt.interpolateCosine(y00, y01, r1);
//        double y2 = MathExt.interpolateCosine(y10, y11, r1);
//        return (float) MathExt.interpolateCosine(y1, y2, r2);
    }
    
    private float getElevation(int iz, int ix) {
        if(iz >= 0 && ix >= 0 && iz < map.nsNum && ix < map.weNum) {
            return map.elevs[iz][ix];
        } else {
            return DefaultY;
        }
    }
}
