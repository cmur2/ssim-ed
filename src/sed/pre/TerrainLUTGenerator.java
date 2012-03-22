package sed.pre;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import sed.pre.terrain.TerrainLUTBuilder;
import sed.pre.terrain.TerrainType;

public class TerrainLUTGenerator {
    
    public static final float MaxAltitude = 7000f;
    
    private static final int Width = 256;
    private static final int Height = 256;
    
    public static void main(String[] args) {
        System.out.println("Terrain LUT Generator");
        
        File outputFile = new File(args.length > 0 ? args[0] : "lut.png");
        System.out.format("Using output (.png) file: %s\n", outputFile.getAbsolutePath());
        
        System.out.println("Applying commands...");
        
        final float maxA = MaxAltitude;
        TerrainLUTBuilder builder = new TerrainLUTBuilder(Width, Height, maxA) {{
            setTypeRect(TerrainType.Default, 0, 0, Width, Height);
            
            setType(TerrainType.OceanFloor,  0, 90, -maxA,  -100);
            setType(TerrainType.LakeFloor,   0, 90,  -100,   -25);
            
            setType(TerrainType.Shore,       0, 10,   -25,    25);
            setType(TerrainType.Cliff,      10, 90,   -25,    25);
            
            setType(TerrainType.Plain,       0, 20,    25,  1500);
            setType(TerrainType.HighPlain,   0, 20,  1500,  3000);
            setType(TerrainType.Hill,       20, 90,    25,  3000);
            
            setType(TerrainType.MountainTop,    0, 20,  3000,  maxA);
            setType(TerrainType.Mountains,     20, 90,  3000,  5000);
            setType(TerrainType.HighMountains, 20, 90,  5000,  maxA);
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
