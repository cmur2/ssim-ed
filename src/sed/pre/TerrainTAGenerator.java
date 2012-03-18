package sed.pre;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import sed.pre.terrain.TerrainTABuilder;
import sed.pre.terrain.TerrainType;

public class TerrainTAGenerator {

    public static final int TexSize = 256;
    public static final int NumTiles = 4;
    
    public static void main(String[] args) {
        System.out.println("Terrain TA Generator");
        
        File outputFile = new File(args.length > 0 ? args[0] : "ta.png");
        System.out.format("Using output (.png) file: %s\n", outputFile.getAbsolutePath());
        
        System.out.println("Applying commands...");
        
        TerrainTABuilder taBuilder = new TerrainTABuilder(TexSize, TexSize, NumTiles, NumTiles) {{
            setAll(Color.magenta);
            setType(TerrainType.Default, Color.red);
            
            setType(TerrainType.OceanFloor, new Color(48, 26, 16));
            setType(TerrainType.LakeFloor, new Color(96, 64, 0));
            
            setType(TerrainType.Shore, new Color(199, 168, 85));
            setType(TerrainType.Cliff, new Color(170, 140, 60));
            
            setType(TerrainType.Plain, new Color(100, 188, 64));
            setType(TerrainType.HighPlain, new Color(205, 255, 91));
            setType(TerrainType.Hill, new Color(144, 208, 64));
            
            setType(TerrainType.MountainTop, new Color(245, 245, 245));
            setType(TerrainType.Mountains, new Color(171, 124, 104));
            setType(TerrainType.HighMountains, new Color(110, 120, 128));
        }};
        
        BufferedImage taTex = taBuilder.getTexture();
        try {
            System.out.println("Writing image...");
            ImageIO.write(taTex, "png", outputFile);
            System.out.println("Done.");
        } catch(IOException ex) {
            ex.printStackTrace();
            System.out.println("An error occured during export of the image! Exiting...");
        }
    }
}
