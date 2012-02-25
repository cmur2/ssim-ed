package sed.sky;

import chlib.noise.NoiseUtil;

public class CloudHeightField {
    
    private int size;
    private int numOctaves;
    
    private float zoom;
    private float shift;
    private float cloudCover;
    
    public CloudHeightField(int size, int numOctaves) {
        this.size = size;
        this.numOctaves = numOctaves;
    }
    
    public float getZoom() {
        return zoom;
    }
    
    public void setZoom(float zoom) {
        this.zoom = zoom;
    }
    
    public float getShift() {
        return shift;
    }
    
    public void setShift(float shift) {
        this.shift = shift;
    }
    
    public float getCloudCover() {
        return cloudCover;
    }
    
    public void setCloudCover(float cloudCover) {
        this.cloudCover = cloudCover;
    }
    
    /**
     * @return a rows-first array with height data in [0,255] 
     */
    public float[][] generate() {
        return generate(null);
    }
    
    /**
     * @param store a float[][] to store the result
     * @return a rows-first array with height data in [0,255] 
     */
    public float[][] generate(float[][] store) {
        if(store == null) {
            store = new float[size][size];
        }
        for(int column = 0; column < size; column++) {
            for(int row = 0; row < size; row++) {
                //float turbulance = NoiseUtil.turbulance2(column/zoom, row/zoom, shift, numOctaves);
                float turbulance = NoiseUtil.fBm(column/zoom, row/zoom, shift, numOctaves, 2f, .5f);
                float height = (turbulance*255f) - cloudCover;
                if(height < 0) { height = 0; }
                store[column][row] = height;
            }
        }
        return store;
    }
}
