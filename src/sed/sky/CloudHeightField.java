package sed.sky;

import com.jme3.math.Vector3f;

import chlib.noise.NoiseUtil;

public class CloudHeightField {
    
    private int size;
    private int numOctaves;
    
    private float zoom;
    private Vector3f shift;
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
    
    public Vector3f getShift() {
        return shift;
    }
    
    public void setShift(Vector3f shift) {
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
                // alternative: NoiseUtil.turbulance2
                float turbulance = NoiseUtil.fBm(
                    (shift.x * size + column)/zoom,
                    (shift.y * size + row)/zoom,
                    shift.z,
                    numOctaves,
                    2f,
                    .5f);
                float height = (turbulance*255f) - cloudCover;
                if(height < 0) { height = 0; }
                store[column][row] = height;
            }
        }
        return store;
    }
}
