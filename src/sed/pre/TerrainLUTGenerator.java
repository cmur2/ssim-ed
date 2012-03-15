package sed.pre;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import sed.pre.terrain.TerrainLUTBuilder;
import sed.pre.terrain.TerrainType;

public class TerrainLUTGenerator {
    
    private static final int Width = 256;
    private static final int Height = 256;
    
    public static void main(String[] args) {
        System.out.println("Terrain LUT Generator");
        
        File outputFile = new File(args.length > 0 ? args[0] : "lut.png");
        System.out.format("Using output (.png) file: %s\n", outputFile.getAbsolutePath());
        
        System.out.println("Applying commands...");
        
        TerrainLUTBuilder builder = new TerrainLUTBuilder(Width, Height) {{
            setType(TerrainType.Default, 0, 0, Width, Height);
            setType(TerrainType.Gras, Width-84, 0, 84, 128);
            setType(TerrainType.Mountain, 0, 0, Width-84, 128);
        }};
        
        BufferedImage lutTex = builder.getTexture();
        try {
            System.out.println("Writing image...");
            ImageIO.write(lutTex, "png", outputFile);
            System.out.println("Done.");
        } catch(IOException ex) {
            ex.printStackTrace();
            System.out.println("An error occured during export of the image! Exiting...");
        }
    }
    
}
