package sed.terrain;

import sed.MapLoader;

public class BinaryMapManipulator {
    
    public static float[] crop(MapLoader.Map map, int size) {
        float[] data = new float[size*size];
        for(int z = 0; z < size; z++) {
            for(int x = 0; x < size; x++) {
                data[x + z*size] = map.elevs[z*6][x*6]*(4/130f);
            }
        }
        
        return data;
    }
    
    public static float[] test(MapLoader.Map map, int size) {
        float[] data = new float[size*size];
        data[0] = 255;
        int x = 0;
        int z = size*2/3;
        data[x + z*size] = 128;
        return data;
    }
}
