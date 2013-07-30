package de.mycrobase.ssim.ed.pre;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

import de.mycrobase.ssim.ed.pre.ocean.SineWavesParamGenerator;

public class SineWaveBumpMapGenerator {
    
    public static final int TexSize = 512;
    
    public static void main(String[] args) {
        System.out.println("Terrain Atlas Generator");
        
        File outputFile = new File(args.length > 0 ? args[0] : "sbm.png");
        System.out.format("Using output (.png) file: %s\n", outputFile.getAbsolutePath());

        SineWavesParamGenerator swpGenerator = new SineWavesParamGenerator(8);
        swpGenerator.setBumpMapSize(TexSize);
        swpGenerator.setRatioAmpOverLambda(0.005f);
        swpGenerator.setMinLambda(4);
        swpGenerator.setMaxLambda(24);
        //swpGenerator.setWindDirection(environmentD.weatherD.getWindDirection());
        swpGenerator.setMeanPhiFactor(0.10f);
        swpGenerator.setPhiFactorDeviation(0.05f);
        swpGenerator.update0();
        
        BufferedImage smbTex = new BufferedImage(TexSize, TexSize, BufferedImage.TYPE_3BYTE_BGR);
        
        {
            int w = TexSize;
            int h = TexSize;
            int[] data = new int[w*h*3];
            int index = 0;
            Vector2f varTexCoord = new Vector2f();
            Vector3f bumpNormal = new Vector3f();
            
            // read-only cache
            float uniTime = 0f;
            int uniNumWaves = swpGenerator.getNumActiveWaves();
            float[] uniAmp = swpGenerator.getAmplitudes();
            //float[] uniLambda;
            Vector2f[] uniDir = swpGenerator.getDirections();
            float[] uniOmega = swpGenerator.getOmegas();
            float[] uniPhi = swpGenerator.getPhis();
            
            for(int y = 0; y < h; y++) {
                for(int x = 0; x < w; x++) {
                    varTexCoord.set(x / (float) TexSize, y / (float) TexSize);
                
                    bumpNormal.set(0f, 0f, 1f);
                    for(int i = 0; i < uniNumWaves; i++) {
                        float c = (float) (uniOmega[i] * uniAmp[i] * Math.cos(varTexCoord.dot(uniDir[i]) *  uniOmega[i] + uniTime * uniPhi[i]));
                        bumpNormal.x -= uniDir[i].x * c;
                        bumpNormal.y -= uniDir[i].y * c;
                    }
                    bumpNormal.normalizeLocal();
                    bumpNormal.addLocal(1f, 1f, 1f);
                    bumpNormal.multLocal(0.5f);
                    
                    data[index+0] = (int) (bumpNormal.x * 255); // R
                    data[index+1] = (int) (bumpNormal.y * 255); // G
                    data[index+2] = (int) (bumpNormal.z * 255); // B
                    
                    index += 3;
                }
            }
            smbTex.getRaster().setPixels(0, 0, w, h, data);
        }
        
        try {
            System.out.println("Writing image...");
            ImageIO.write(smbTex, "png", outputFile);
            System.out.println("Done.");
        } catch(IOException ex) {
            ex.printStackTrace();
            System.out.println("An error occured during export of the image! Exiting...");
        }
    }
}
