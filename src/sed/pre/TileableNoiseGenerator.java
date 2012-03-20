package sed.pre;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ssim.util.MathExt;
import chlib.noise.NoiseUtil;

import com.jme3.math.Vector3f;

public class TileableNoiseGenerator {
    
    private static final long DebugSeed = 4569845;
    private static final int TexSize = 512;
    
    private static final int NumOctaves = 8;
    private static final float Zoom = 36f;
    
    public static void main(String[] args) {
        System.out.println("Terrain LUT Generator");
        
        // Important!
        NoiseUtil.reinitialize(DebugSeed);
        
        File outputFile = new File(args.length > 0 ? args[0] : "noisetile.png");
        System.out.format("Using output (.png) file: %s\n", outputFile.getAbsolutePath());
        
        // size == tileSize because we want a tileable texture but only one tile
        // of it that may be wrapped by texture coordinates
        TileableNoiseGenerator generator =
            new TileableNoiseGenerator(TexSize, TexSize, NumOctaves, Zoom, Vector3f.ZERO);
        generator.generate();
        BufferedImage noiseTex = generator.getTexture();

        try {
            System.out.println("Writing image...");
            ImageIO.write(noiseTex, "png", outputFile);
            System.out.println("Done.");
        } catch(IOException ex) {
            ex.printStackTrace();
            System.out.println("An error occured during export of the image! Exiting...");
        }
    }
    
    private int size;
    private int tileSize;
    private int numOctaves;
    private float zoom;
    private Vector3f shift;
    
    private BufferedImage texture;
    
    public TileableNoiseGenerator(int size, int tileSize, int numOctaves, float zoom, Vector3f shift) {
        texture = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
        
        this.size = size;
        this.tileSize = tileSize;
        this.numOctaves = numOctaves;
        this.zoom = zoom;
        this.shift = shift.clone();
    }
    
    public BufferedImage getTexture() {
        return texture;
    }
    
    public void generate() {
        int[] data = new int[size * size * 4];
        for(int column = 0; column < size; column++) {
            for(int row = 0; row < size; row++) {
                // sample octaves before interpolating
                float n = tileFBm(column % tileSize, row % tileSize, tileSize, tileSize);
                // use tiling noise and calculate octaves upon it
//                {
//                    float x = column, y = row, sum = 0, amp = 1;
//                    for(int i = 0; i < numOctaves; i++) {
//                        sum += amp * tileNoiseF(x % tileSize, y % tileSize, tileSize, tileSize);
//                        x *= 2f; y *= 2f; amp *= .5f;
//                    }
//                    n = sum;
//                }
                // bring noise from [0,1] to [-1,+1]
                n = (n + 1f) * 0.5f;
                // convert noise into byte, clamp it!
                int r = (int) MathExt.clamp(n*255, 0, 255);
                int index = (row * size + column) * 4;
                // TODO: use all channels to provide (different) noise data
                data[index+0] = r; // R
                data[index+1] = r; // G
                data[index+2] = r; // B
                data[index+3] = 255; // A
            }
        }
        texture.getRaster().setPixels(0, 0, size, size, data);
    }
    
    private float tileNoiseF(float x, float y, float w, float h) {
        return (
            noiseF(x, y) * (w - x) * (h - y) +
            noiseF(x - w, y) * (x) * (h - y) +
            noiseF(x - w, y - h) * (x) * (y) +
            noiseF(x, y - h) * (w - x) * (y)
            ) / (w * h);
    }
    
    private float noiseF(float x, float y) {
        return NoiseUtil.noiseF((shift.x + x)/zoom, (shift.y + y)/zoom, shift.z);
    }
    
    private float tileFBm(float x, float y, float w, float h) {
        return (
            fBm(x, y) * (w - x) * (h - y) +
            fBm(x - w, y) * (x) * (h - y) +
            fBm(x - w, y - h) * (x) * (y) +
            fBm(x, y - h) * (w - x) * (y)
            ) / (w * h);
    }
    
    private float fBm(float x, float y) {
        return NoiseUtil.fBm(
            (shift.x * size + x)/zoom, (shift.y * size + y)/zoom, shift.z,
            numOctaves, 2f, .5f);
    }
}
