package de.mycrobase.ssim.ed.terrain;

import com.jme3.math.Vector3f;

public class BinaryMapManipulator {
    
    public static float[] testcrop(BinaryMap map, int size) {
        float[] data = new float[size*size];
        for(int z = 0; z < size; z++) {
            for(int x = 0; x < size; x++) {
                data[x + z*size] = map.elevs[z*6][x*6]*(4/130f);
            }
        }
        return data;
    }
    
    public static float[] test(BinaryMap map, int size) {
        float[] data = new float[size*size];
        data[0] = 255;
        int x = 0;
        int z = size*2/3;
        data[x + z*size] = 128;
        return data;
    }
    
    public static float[] crop(BinaryMap map, Vector3f offset, int quadSize) {
        float[] data = new float[quadSize*quadSize];
        for(int z = 0; z < quadSize; z++) {
            for(int x = 0; x < quadSize; x++) {
                int iz = (int) (offset.z * (quadSize-1) + z);
                int ix = (int) (offset.x * (quadSize-1) + x);
                if(iz >= 0 && ix >= 0 && iz < map.nsNum && ix < map.weNum) {
                    data[x + z*quadSize] = map.elevs[iz][ix];
                } else {
                    data[x + z*quadSize] = 0;
                }
            }
        }
        return data;
    }
}
