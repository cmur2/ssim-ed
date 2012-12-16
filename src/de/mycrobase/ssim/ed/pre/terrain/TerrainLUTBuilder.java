package de.mycrobase.ssim.ed.pre.terrain;

import de.mycrobase.ssim.ed.pre.TextureMapBuilder;

/**
 * Generates a RGBA texture image that has the ID information in the R
 * component, full alpha, and rest zero. 
 * 
 * @author cn
 */
public class TerrainLUTBuilder extends TextureMapBuilder {
    
    private float maxAltitude;
    private float altitudeDistortionFactor;
    
    public TerrainLUTBuilder(int width, int height, float maxAltitude, float altitudeDistortionFactor) {
        super(width, height);
        this.maxAltitude = maxAltitude;
        this.altitudeDistortionFactor = altitudeDistortionFactor;
    }
    
    public void setTypeRect(TerrainType type, int xs, int ys, int xe, int ye) {
        System.out.format("setTypeRect(%s,%d,%d,%d,%d)\n", type, xs, ys, xe, ye);
        int w = xe-xs;
        int h = ye-ys;
        int rValue = typeToRedValue(type);
        int[] data = new int[w*h*4];
        for(int index = 0; index < w*h*4; index += 4) {
            data[index+0] = rValue; // R
            data[index+1] = 0; // G
            data[index+2] = 0; // B
            data[index+3] = 255; // A
        }
        getTexture().getRaster().setPixels(xs, ys, w, h, data);
    }
    
    public void setType(TerrainType type, float startSlope, float endSlope,
        float startAltitude, float endAltitude)
    {
        if(endAltitude < startAltitude) {
            throw new IllegalArgumentException(
                String.format("endAltitude < startAltitude! (%g < %g)", endAltitude, startAltitude));
        }
        if(endSlope < startSlope) {
            throw new IllegalArgumentException(
                String.format("endSlope < startSlope! (%g < %g)", endSlope, startSlope));
        }
        
        float xs = slopeToX(endSlope);
        float xe = slopeToX(startSlope);
        float ys = altitudeToY(endAltitude);
        float ye = altitudeToY(startAltitude);
        
        setTypeRect(type,
            (int) (xs * getWidth()), (int) (ys * getHeight()),
            (int) (xe * getWidth()), (int) (ye * getHeight()));
    }
    
    public float getCoverage(TerrainType type) {
        int nFound = 0;
        int rValue = typeToRedValue(type);
        
        int[] data = new int[getWidth()*getHeight()*4];
        getTexture().getRaster().getPixels(0, 0, getWidth(), getHeight(), data);
        
        for(int index = 0; index < getWidth()*getHeight()*4; index += 4) {
            if(data[index+0] == rValue) {
                nFound++;
            }
        }
        return (float) nFound / (getWidth()*getHeight());
    }
    
    private float slopeToX(float slope) {
        return (float) Math.cos(Math.toRadians(slope));
    }
    
    private float altitudeToY(float altitude) {
        float x = altitude/maxAltitude;
        if(x > 0) {
            x = (float) Math.pow(x, altitudeDistortionFactor);
        }
        return 1f - (x*0.5f + 0.5f);
    }
    
    private int typeToRedValue(TerrainType type) {
        return type.getId();
    }
}
