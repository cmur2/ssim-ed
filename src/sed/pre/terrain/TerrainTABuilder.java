package sed.pre.terrain;

import java.awt.Color;

import sed.pre.TextureMapBuilder;

public class TerrainTABuilder extends TextureMapBuilder {

    private int nTilesWidth;
    private int nTilesHeight;
    
    public TerrainTABuilder(int width, int height, int nTilesWidth, int nTilesHeight) {
        super(width, height);
        this.nTilesWidth = nTilesWidth;
        this.nTilesHeight = nTilesHeight;
    }
    
    public void setAll(Color color) {
        int w = getWidth();
        int h = getHeight();
        int[] data = new int[w*h*4];
        for(int index = 0; index < w*h*4; index += 4) {
            data[index+0] = color.getRed(); // R
            data[index+1] = color.getGreen(); // G
            data[index+2] = color.getBlue(); // B
            data[index+3] = color.getAlpha(); // A
        }
        getTexture().getRaster().setPixels(0, 0, w, h, data);
    }
    
    public void setType(TerrainType type, Color color) {
        int id = type.getId();
        
        int tileWidth = getWidth() / nTilesWidth; // in px
        int tileHeight = getHeight() / nTilesHeight; // in px
        
        int x = id % nTilesWidth * tileWidth;
        int y = id / nTilesHeight * tileHeight;
        int w = tileWidth;
        int h = tileHeight;
        
        int[] data = new int[w*h*4];
        for(int index = 0; index < w*h*4; index += 4) {
            data[index+0] = color.getRed(); // R
            data[index+1] = color.getGreen(); // G
            data[index+2] = color.getBlue(); // B
            data[index+3] = color.getAlpha(); // A
        }
        getTexture().getRaster().setPixels(x, getHeight()-y-h, w, h, data);
    }
}
