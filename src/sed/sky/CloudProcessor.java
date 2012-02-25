package sed.sky;

import java.nio.ByteBuffer;

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

    private static final int TexSize = 256;
    private static final int MaxSteps = 10;
    
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
    private Vector3f sunLightColor;
    
    public CloudProcessor(Mode mode, AssetManager assetManager) {
        this.mode = mode;
        this.assetManager = assetManager;
        
        heightFieldTex = new Texture2D(TexSize, TexSize, Format.RGBA8);
        cloudTex = new Texture2D(TexSize, TexSize, Format.RGBA8);
        
        cloudHeightField = new CloudHeightField(TexSize, 8);
        cloudHeightField.setZoom(48);
        cloudHeightField.setShift(0);
        cloudHeightField.setCloudCover(50);
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
            
            // this shader does the real work... later
            //Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            //mat.setColor("Color", ColorRGBA.Orange.mult(new ColorRGBA(1f, 1f, 1f, 0.5f)));
            //mat.setTexture("ColorMap", heightFieldTex);
            Material mat = new Material(assetManager, "shaders/CloudRender.j3md");
            mat.setFloat("ImageSize", TexSize);
            mat.setFloat("MaxSteps", MaxSteps);
            mat.setFloat("CloudSharpness", cloudSharpness);
            mat.setFloat("WayFactor", wayFactor);
            mat.setVector3("SunPosition", sunPosition);
            mat.setVector3("SunLightColor", sunLightColor);
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
        if(time > 10) {
            time = 0;
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
        // nothing
        tempViewPort.clearScenes();
        tempViewPort.setOutputFrameBuffer(null);
        tempViewPort = null;
        cloudTex = null;
    }
    
    public float getCloudCover() {
        return cloudHeightField.getCloudCover();
    }

    public void setCloudCover(float cloudCover) {
        cloudHeightField.setCloudCover(cloudCover);
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

    public Vector3f getSunLightColor() {
        return sunLightColor;
    }

    public void setSunLightColor(Vector3f sunLightColor) {
        this.sunLightColor = sunLightColor;
    }

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
        
        Vector3f v = new Vector3f(), vadd = new Vector3f();
        float z = 255;
        boolean breakOnCloudExit = sunPosition.z < -z || sunPosition.z > z;
        
        ByteBuffer buf = heightFieldTexture.getImage().getData(0);
        buf.rewind();
        // render complete image
        for(int column = 0; column < TexSize; column++) {
            for(int row = 0; row < TexSize; row++) {
                // render one texel
                float alpha = heightField[column][row];
                if(alpha == 0) continue;
                boolean lastWasInCloud = true;
                float wayInClouds = 0;
                v.set(column, row, -alpha);
                vadd.set(sunPosition.x-v.x, sunPosition.y-v.y, sunPosition.z-v.z);
                vadd.mult(1f/MaxSteps);
                float length = vadd.length();
                for(int k = 0; k < MaxSteps; k++, v.add(vadd)) {
                    if(v.z < -z || v.z > z) break;
                    int clamped_vx = (int)(MathExt.clamp(v.x, 0, TexSize-1));
                    int clamped_vy = (int)(MathExt.clamp(v.y, 0, TexSize-1));
                    float talpha = heightField[clamped_vx][clamped_vy];
                    if(v.z <= talpha) {
                        wayInClouds += length;
                        lastWasInCloud = true;
                    } else {
                        if(lastWasInCloud) {
                            wayInClouds += Math.max(talpha - v.z + vadd.z, 0f);
                        }
                        lastWasInCloud = false;
                        if(breakOnCloudExit) break;
                    }
                }
                int color = 255 - (int) (wayInClouds * wayFactor * 255f);
                alpha = 1f - (float) Math.pow(cloudSharpness, alpha);
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
                int index = column*TexSize*4 + row*4; // TODO:
                buf.put(index+0, (byte) color); // R
                buf.put(index+1, (byte) color); // G
                buf.put(index+2, (byte) color); // B
                buf.put(index+3, (byte) alpha); // A
            }
        }
        heightFieldTexture.getImage().setData(0, buf);
    }
}
