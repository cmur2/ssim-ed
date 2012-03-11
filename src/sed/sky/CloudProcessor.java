package sed.sky;

import org.apache.log4j.Logger;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;

/**
 * This processor hooks into the render process to use preFrame to render
 * the clouds.
 * 
 * @author cn
 */
public abstract class CloudProcessor implements SceneProcessor {

    private static final Logger logger = Logger.getLogger(CloudProcessor.class);
    
    private static final int MaxSteps = 30;
    private static final int NumOctaves = 8;
    
    // state
    private boolean init = false;
    private float time = 0;
    private int texSize;
    private float updateInterval; // in seconds
    
    private Texture2D cloudTex;
    
    public CloudProcessor(int texSize, float updateInterval) {
        this.texSize = texSize;
        this.updateInterval = updateInterval;
        
        cloudTex = new Texture2D(getTexSize(), getTexSize(), Format.RGBA8);
    }
    
    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        init = true;
    }

    @Override
    public boolean isInitialized() {
        return init;
    }

    @Override
    public void preFrame(float tpf) {
        if(time >= updateInterval) {
            time -= updateInterval;
            updateAndRender();
        }
        time += tpf;
    }

    @Override
    public void postQueue(RenderQueue rq) {
        // nothing
    }

    @Override
    public void postFrame(FrameBuffer out) {
        // nothing
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
        // nothing
    }

    @Override
    public void cleanup() {
        cloudTex = null;
    }
    
    public abstract float getCloudCover();
    public abstract void setCloudCover(float cloudCover);
    
    public abstract Vector3f getShift();
    public abstract void setShift(Vector3f shift);
    
    public abstract float getZoom();
    public abstract void setZoom(float zoom);
    
    public abstract float getCloudSharpness();
    public abstract void setCloudSharpness(float cloudSharpness);
    
    public abstract float getWayFactor();
    public abstract void setWayFactor(float wayFactor);
    
    public abstract Vector3f getSunPosition();
    public abstract void setSunPosition(Vector3f sunPosition);
    
    public abstract ColorRGBA getSunLightColor();
    public abstract void setSunLightColor(ColorRGBA sunLightColor);
    
    
    public int getTexSize() {
        return texSize;
    }
    
    public int getMaxSteps() {
        return MaxSteps;
    }
    
    public int getNumOctaves() {
        return NumOctaves;
    }
    
    /**
     * @return the final texture containing the rendered cloud image  
     */
    public Texture2D getCloudTex() {
        return cloudTex;
    }
    
    public abstract long getLastGenerationTime();
    public abstract long getLastRenderTime();
    
    protected abstract void updateAndRender();
    
    /**
     * @param data values in [0,255] to be converted into bytes
     * @return ByteBuffer with rows-first data inserted
     */
//    private ByteBuffer generateByteBuffer(float[][] data) {
//        ByteBuffer bb = BufferUtils.createByteBuffer(TexSize * TexSize * 4);
//        for(int column = 0; column < TexSize; column++) {
//            for(int row = 0; row < TexSize; row++) {
//                int index = (row*TexSize + column) * 4;
//                float alpha = MathExt.clamp(data[column][row], 0, 255);
//                bb.put(index+0, (byte) (alpha)); // A
//                bb.put(index+1, (byte) (255)); // B
//                bb.put(index+2, (byte) (255)); // G
//                bb.put(index+3, (byte) (255)); // R
//            }
//        }
//        return bb;
//    }
}
