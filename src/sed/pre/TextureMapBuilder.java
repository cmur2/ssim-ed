package sed.pre;

import java.awt.image.BufferedImage;

public class TextureMapBuilder {
    
    private int width;
    private int height;
    
    protected BufferedImage texture;
    
    public TextureMapBuilder(int width, int height) {
        this.width = width;
        this.height = height;
        texture = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
    }
    
    public final BufferedImage getTexture() {
        return texture;
    }
}
