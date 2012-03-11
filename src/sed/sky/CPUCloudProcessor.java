package sed.sky;

import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledExecutorService;

import ssim.util.MathExt;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.Texture2D;
import com.jme3.util.BufferUtils;

/**
 * This {@link CloudProcessor} uses CPU-only algorithms to render the clouds
 * into the final texture. The generation of the cloud heightfield is done
 * on CPU via {@link CloudHeightField}.
 * 
 * @author cn
 */
public class CPUCloudProcessor extends CloudProcessor {
    
    // (weather) variables
    private float cloudSharpness;
    private float wayFactor;
    private Vector3f sunPosition;
    private ColorRGBA sunLightColor;
    
    private CloudHeightField cloudHeightField;
    
    private long lastRenderTime;
    
    public CPUCloudProcessor(int texSize, float updateInterval,
            ScheduledExecutorService executor)
    {
        super(texSize, updateInterval);
        
        cloudHeightField = new CloudHeightField(texSize, getNumOctaves(), executor);
    }
    
    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        super.initialize(rm, vp);
        updateAndRender();
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        cloudHeightField = null;
    }
    
    public float getCloudCover() {
        return cloudHeightField.getCloudCover();
    }

    public void setCloudCover(float cloudCover) {
        cloudHeightField.setCloudCover(cloudCover);
    }
    
    public Vector3f getShift() {
        return cloudHeightField.getShift();
    }
    
    public void setShift(Vector3f shift) {
        cloudHeightField.setShift(shift);
    }
    
    public float getZoom() {
        return cloudHeightField.getZoom();
    }
    
    public void setZoom(float zoom) {
        cloudHeightField.setZoom(zoom);
    }

    public float getCloudSharpness() {
        return cloudSharpness;
    }

    public void setCloudSharpness(float cloudSharpness) {
        this.cloudSharpness = cloudSharpness;
    }

    public float getWayFactor() {
        return wayFactor;
    }

    public void setWayFactor(float wayFactor) {
        this.wayFactor = wayFactor;
    }

    public Vector3f getSunPosition() {
        return sunPosition;
    }

    public void setSunPosition(Vector3f sunPosition) {
        this.sunPosition = sunPosition;
    }

    public ColorRGBA getSunLightColor() {
        return sunLightColor;
    }

    public void setSunLightColor(ColorRGBA sunLightColor) {
        this.sunLightColor = sunLightColor;
    }

    public long getLastGenerationTime() {
        return cloudHeightField.getLastGenerationTime();
    }
    
    public long getLastRenderTime() {
        return lastRenderTime;
    }
    
    protected void updateAndRender() {
        // generate height field on CPU
        float[][] heightField = cloudHeightField.generate();
        
        renderHeightFieldToTexture(heightField, getCloudTex());
    }
    
    private void renderHeightFieldToTexture(
        float[][] heightField, Texture2D heightFieldTexture)
    {
        int texSize = getTexSize();
        int maxSteps = getMaxSteps();
        
        // lazy allocate backing ByteBuffer
        if(heightFieldTexture.getImage().getData(0) == null) {
            heightFieldTexture.getImage().setData(
                BufferUtils.createByteBuffer(texSize * texSize * 4));
        }
        
        Vector3f v = new Vector3f();
        Vector3f vdir = new Vector3f();
        Vector3f vadd = new Vector3f();
        ColorRGBA color = new ColorRGBA();
        //float z = 255;
        //boolean breakOnCloudExit = sunPosition.z < -z || sunPosition.z > z;
        
        ByteBuffer buf = heightFieldTexture.getImage().getData(0);
        buf.rewind();
        long t0 = System.nanoTime();
        // render complete image
        for(int column = 0; column < texSize; column++) {
            for(int row = 0; row < texSize; row++) {
                // render one texel
                float alpha = heightField[column][row];
                if(alpha == 0) {
                    int index = (row*texSize + column)*4;
                    buf.put(index+0, (byte) 0); // R
                    buf.put(index+1, (byte) 0); // G
                    buf.put(index+2, (byte) 0); // B
                    buf.put(index+3, (byte) 0); // A
                    continue;
                }
                v.set(column, row, -alpha);
                vadd.set(sunPosition.x-v.x, sunPosition.y-v.y, sunPosition.z-v.z);
                vadd.multLocal(1f/maxSteps);
                float len = vadd.length();
                float wayInClouds = 0;
                //boolean lastWasInCloud = true;
                for(int k = 0; k < maxSteps; k++, v.addLocal(vadd)) {
                    //if(v.z < -z || v.z > z) break;
                    int clamped_vx = (int)(MathExt.clamp(v.x, 0, texSize-1));
                    int clamped_vy = (int)(MathExt.clamp(v.y, 0, texSize-1));
                    float talpha = heightField[clamped_vx][clamped_vy];
                    if(talpha != 0) {
                        if(-talpha <= v.z && v.z <= talpha) {
                            wayInClouds += len;
                            //lastWasInCloud = true;
                        }
                        //else {
                        //    if(lastWasInCloud) {
                        //        wayInClouds += Math.max(talpha - v.z + vadd.z, 0f);
                        //    }
                        //    lastWasInCloud = false;
                        //    if(breakOnCloudExit) break;
                        //}
                    }
                }
                //int cfinal = 255 - (int) (wayInClouds * wayFactor * 255f);
                float cfinal = (float) Math.exp(-wayFactor * wayInClouds);
                
                color.set(sunLightColor);
                color.multLocal(cfinal*255f);
                if(color.r > 255) color.r = 255;
                if(color.g > 255) color.g = 255;
                if(color.b > 255) color.b = 255;
                
                alpha = 1f - (float) Math.pow(cloudSharpness, alpha);
                alpha *= heightField[column][row]/255f;
                alpha *= 255f;
                if(alpha > 255) alpha = 255;
                
                int index = (row*texSize+ column)*4;
                buf.put(index+0, (byte) color.r); // R
                buf.put(index+1, (byte) color.g); // G
                buf.put(index+2, (byte) color.b); // B
                buf.put(index+3, (byte) alpha); // A
            }
        }
        lastRenderTime = System.nanoTime() - t0;
        heightFieldTexture.getImage().setData(0, buf);
    }
}
