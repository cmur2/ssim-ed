package sed.pre.terrain;

import sed.pre.TextureMapBuilder;

public class TerrainLUTBuilder extends TextureMapBuilder {
    
    public TerrainLUTBuilder(int width, int height) {
        super(width, height);
    }
    
    public void setType(TerrainType type, int x, int y, int w, int h) {
        System.out.format("setType(%s,%d,%d,%d,%d)\n", type, x, y, w, h);
        int[] data = new int[w*h*4];
        for(int index = 0; index < w*h*4; index += 4) {
            data[index+0] = type.getId(); // R
            data[index+1] = 0; // G
            data[index+2] = 0; // B
            data[index+3] = 255; // A
        }
        texture.getRaster().setPixels(x, y, w, h, data);
    }
}
