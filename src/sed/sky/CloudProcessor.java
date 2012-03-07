package sed.sky;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import ssim.util.MathExt;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import com.jme3.util.BufferUtils;

/**
 * This processor hooks into the render process to use preFrame to render
 * the clouds.
 * 
 * @author cn
 */
public class CloudProcessor implements SceneProcessor {

    private static final Logger logger = Logger.getLogger(CloudProcessor.class);
    
    private static final int TexSize = 256;
    private static final int MaxSteps = 30;
    private static final int NumOctaves = 8;
    
    public enum Mode {
        /**
         * HeightField generation and rendering are done on CPU
         * (slow, fall back)
         */
        AllCPU,
        /**
         * HeightField generation is done on CPU, but rendering will be GPU task
         * (faster)
         */
        RenderGPU;
    }
    
    // state
    private boolean init = false;
    private float time = 0;
    private Mode mode;
    private float updateInterval; // in seconds
    
    // main part of final scene
    private AssetManager assetManager;
    private RenderManager renderManager;
    private ViewPort mainViewPort;
    
    // view ports
    private ViewPort tempViewPort;
    
    // textures
    private Texture2D heightFieldTex;
    private Texture2D cloudTex;
    
    // height field
    private CloudHeightField cloudHeightField;
    
    // (weather) variables
    private float cloudSharpness;
    private float wayFactor;
    private Vector3f sunPosition;
    private ColorRGBA sunLightColor;
    
    public CloudProcessor(AssetManager assetManager, Mode mode, float updateInterval) {
        this.assetManager = assetManager;
        this.mode = mode;
        this.updateInterval = updateInterval;
        
        heightFieldTex = new Texture2D(TexSize, TexSize, Format.RGBA8);
        cloudTex = new Texture2D(TexSize, TexSize, Format.RGBA8);
        
        cloudHeightField = new CloudHeightField(TexSize, NumOctaves);
    }
    
    // create heightfield (different algos: CPU GPU)
    // create pre-texture (different algos: CPU GPU [in mini scene with pre-appearance])
    // create final appearance -> done by user of this processor?
    
    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        renderManager = rm;
        mainViewPort = vp;
        
        if(mode == Mode.AllCPU) {
            // nothing
        } else {
            Camera cam = new Camera(TexSize, TexSize);
            
            tempViewPort = new ViewPort("Cloud-Render-ViewPort", cam);
            tempViewPort.setClearFlags(true, true, true);
            tempViewPort.setBackgroundColor(ColorRGBA.Black);
            
            FrameBuffer fb = new FrameBuffer(TexSize, TexSize, 1);
            fb.setColorTexture(cloudTex);
            
            // --- mini scene ---
            Picture quad = new Picture("Cloud-Render-Target");
            quad.setPosition(0, 0);
            quad.setWidth(TexSize);
            quad.setHeight(TexSize);
            
            //Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            //mat.setColor("Color", ColorRGBA.Orange.mult(new ColorRGBA(1f, 1f, 1f, 0.5f)));
            //mat.setTexture("ColorMap", heightFieldTex);
            Material mat = new Material(assetManager, "shaders/CloudRender.j3md");
            mat.setFloat("ImageSize", TexSize);
            mat.setFloat("MaxSteps", MaxSteps);
            mat.setFloat("CloudSharpness", cloudSharpness);
            mat.setFloat("WayFactor", wayFactor);
            mat.setVector3("SunPosition", sunPosition);
            mat.setColor("SunLightColor", sunLightColor);
            mat.setTexture("HeightField", heightFieldTex);
            quad.setMaterial(mat);
            quad.updateGeometricState();
            
            tempViewPort.attachScene(quad);
            tempViewPort.setOutputFrameBuffer(fb);
            // --- end ---
        }
        
        updateAndRender();
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
        if(mode == Mode.AllCPU) {
            // nothing
        } else {
            tempViewPort.clearScenes();
            tempViewPort.setOutputFrameBuffer(null);
            tempViewPort = null;
        }
        heightFieldTex = null;
        cloudTex = null;
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

    /**
     * @return the final texture containing the rendered cloud image  
     */
    public Texture2D getCloudTex() {
        return cloudTex;
    }
    
    private void updateAndRender() {
        // generate height field on CPU
        float[][] heightField = cloudHeightField.generate();
        
        if(mode == Mode.AllCPU) {
            opRenderHeightField2Texture(heightField, cloudTex);
        } else {
            opCopyHeightField2Texture(heightField, heightFieldTex);
            renderManager.renderViewPort(tempViewPort, 0);
        }
    }
    
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
    
    private static void opCopyHeightField2Texture(float[][] heightField, Texture2D heightFieldTexture) {
        // lazy allocate backing ByteBuffer
        if(heightFieldTexture.getImage().getData(0) == null) {
            heightFieldTexture.getImage().setData(BufferUtils.createByteBuffer(TexSize * TexSize * 4));
        }
        // copy alpha to texel
        ByteBuffer buf = heightFieldTexture.getImage().getData(0);
        buf.rewind();
        for(int column = 0; column < TexSize; column++) {
            for(int row = 0; row < TexSize; row++) {
                float alpha = heightField[column][row];
                int index = (row*TexSize + column)*4;
//                if((column == 23 && row == 0) || (column == 117 && row == 0) || (column == 42 && row == 42)) {
//                    buf.put(index+0, (byte) 255); // R
//                    buf.put(index+1, (byte) 128); // G
//                    buf.put(index+2, (byte)   0); // B
//                    buf.put(index+3, (byte) 255); // A
//                } else {
                    buf.put(index+0, (byte) alpha); // R
                    buf.put(index+1, (byte) alpha); // G
                    buf.put(index+2, (byte) alpha); // B
                    buf.put(index+3, (byte) 255); // A
//                }
//                buf.put((byte) alpha); // R
//                buf.put((byte) alpha); // G
//                buf.put((byte) alpha); // B
//                buf.put((byte) 255); // A
            }
        }
        heightFieldTexture.getImage().setData(0, buf);
    }
    
    private void opRenderHeightField2Texture(float[][] heightField, Texture2D heightFieldTexture) {
        // lazy allocate backing ByteBuffer
        if(heightFieldTexture.getImage().getData(0) == null) {
            heightFieldTexture.getImage().setData(BufferUtils.createByteBuffer(TexSize * TexSize * 4));
        }
        
        Vector3f v = new Vector3f();
        Vector3f vdir = new Vector3f();
        Vector3f vadd = new Vector3f();
        //float z = 255;
        //boolean breakOnCloudExit = sunPosition.z < -z || sunPosition.z > z;
        
        ByteBuffer buf = heightFieldTexture.getImage().getData(0);
        buf.rewind();
        // render complete image
        for(int column = 0; column < TexSize; column++) {
            for(int row = 0; row < TexSize; row++) {
                // render one texel
                float alpha = heightField[column][row];
                if(alpha == 0) {
                    int index = (row*TexSize + column)*4;
                    buf.put(index+0, (byte) 0); // R
                    buf.put(index+1, (byte) 0); // G
                    buf.put(index+2, (byte) 0); // B
                    buf.put(index+3, (byte) 0); // A
                    continue;
                }
                v.set(column, row, -alpha);
                vadd.set(sunPosition.x-v.x, sunPosition.y-v.y, sunPosition.z-v.z);
                vadd.multLocal(1f/MaxSteps);
                float len = vadd.length();
                float wayInClouds = 0;
                //boolean lastWasInCloud = true;
                for(int k = 0; k < MaxSteps; k++, v.addLocal(vadd)) {
                    //if(v.z < -z || v.z > z) break;
                    int clamped_vx = (int)(MathExt.clamp(v.x, 0, TexSize-1));
                    int clamped_vy = (int)(MathExt.clamp(v.y, 0, TexSize-1));
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
                //int color = 255 - (int) (wayInClouds * wayFactor * 255f);
                int color = (int) (Math.exp(-wayFactor * wayInClouds) * 255f);
                alpha = 1f - (float) Math.pow(cloudSharpness, alpha);
                alpha *= heightField[column][row]/255f;
                //alpha *= ALPHA_FACTOR;
                if(alpha > 1f) alpha = 1f;
                //float sdiff = 0.5f - (column/(float)size);
                //float tdiff = 0.5f - (row/(float)size);
                //float diff = (float) Math.sqrt(sdiff*sdiff + tdiff*tdiff);
                //if(diff > CLOUD_CLIPPING_DISTANCE) alpha -= (diff-CLOUD_CLIPPING_DISTANCE)*CLOUD_CLIPPING_FACTOR;
                alpha *= 255f;
                //if(alpha < 0) alpha = 0;
                if(color > 255) color = 255;
                //else if(color < MIN_COLOR*255) color = (int) (MIN_COLOR*255);
                int index = (row*TexSize + column)*4;
                buf.put(index+0, (byte) color); // R
                buf.put(index+1, (byte) color); // G
                buf.put(index+2, (byte) color); // B
                buf.put(index+3, (byte) alpha); // A
            }
        }
        heightFieldTexture.getImage().setData(0, buf);
    }
}
