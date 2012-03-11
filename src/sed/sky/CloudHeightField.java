package sed.sky;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;

import ssim.util.MathExt;

import com.jme3.math.Vector3f;

import chlib.noise.NoiseUtil;

public class CloudHeightField {
    
    private int size;
    private int numOctaves;
    private ScheduledExecutorService executor;
    
    private float zoom;
    private Vector3f shift;
    private float cloudCover;
    
    private long lastGenerationTime;
    
    public CloudHeightField(int size, int numOctaves, ScheduledExecutorService executor) {
        this.size = size;
        this.numOctaves = numOctaves;
        this.executor = executor;
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
    
    public long getLastGenerationTime() {
        return lastGenerationTime;
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
        long t0 = System.nanoTime();
        try {
            executor.invokeAll(Arrays.asList(
                new QuadrantUpdater(store, 0), new QuadrantUpdater(store, 1),
                new QuadrantUpdater(store, 2), new QuadrantUpdater(store, 3)
            ));
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
        lastGenerationTime = System.nanoTime() - t0;
        return store;
    }
    
    private void generatePartial(float[][] store, int quadrantNr) {
        // quadrants:
        //  0 | 1
        // ---+---
        //  2 | 3
        // do equally sized partition to produce equal work load
        int sizeH = size/2;
        int xs = (quadrantNr == 0 || quadrantNr == 2) ? 0 : sizeH;
        int ys = (quadrantNr < 2) ? 0 : sizeH;
        int xe = xs + sizeH;
        int ye = ys + sizeH;
        for(int column = xs; column < xe; column++) {
            for(int row = ys; row < ye; row++) {
                float turbulance = NoiseUtil.fBm(
                    (shift.x * size + column)/zoom,
                    (shift.y * size + row)/zoom,
                    shift.z,
                    numOctaves, 2f, .5f);
                // convert from -1..1 to 0..1
                turbulance = (turbulance + 1f) * 0.5f;
                // alternative is turbulance2
//                float turbulance = NoiseUtil.turbulance2(
//                    (shift.x * size + column)/zoom,
//                    (shift.y * size + row)/zoom,
//                    shift.z,
//                    numOctaves);
                float height = (turbulance*255f) - cloudCover;
                height = MathExt.clamp(height, 0, 255);
                store[column][row] = height;
            }
        }
    }
    
    private class QuadrantUpdater implements Callable<Void> {
        
        private float[][] store;
        private int quadrantNr;
        
        // hand over target store by reference
        public QuadrantUpdater(float[][] store, int quadrantNr) {
            this.store = store;
            this.quadrantNr = quadrantNr;
        }
        
        @Override
        public Void call() throws Exception {
            generatePartial(store, quadrantNr);
            return null;
        }
    }
}
