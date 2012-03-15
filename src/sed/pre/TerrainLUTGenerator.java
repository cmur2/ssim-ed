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
        
        TerrainLUTBuilder builder = new TerrainLUTBuilder(Width, Height, 7000f) {{
            setTypeRect(TerrainType.Default, 0, 0, Width, Height);
            // real data
            setType(TerrainType.Gras,        0, 20,    10,  7000);
            setType(TerrainType.Mountain,   20, 90,    10,  7000);
            // under water
            setType(TerrainType.LakeFloor,   0, 90,   -50,   -10);
            setType(TerrainType.OceanFloor,  0, 90, -7000,   -50);
        }};
        
        float defaultCov = builder.getCoverage(TerrainType.Default);
        System.out.format("Default coverage: %.2g%%%s\n",
            defaultCov*100f,
            defaultCov > 0.1f ? " (warning!)" : "");
        
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
