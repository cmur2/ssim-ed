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
    
    private boolean init = false;
    private float time = 0;
    
    private AssetManager assetManager;
    private RenderManager renderManager;
    private ViewPort viewPort;
    
    private ViewPort tempVP;
    
    private Texture2D heightFieldTex;
    private Texture2D cloudTex;
    
    public CloudProcessor(AssetManager assetManager) {
        this.assetManager = assetManager;
        
        heightFieldTex = new Texture2D(TexSize, TexSize, Format.RGBA8);
        cloudTex = new Texture2D(TexSize, TexSize, Format.RGBA8);
    }
    
    // create heightfield (different algos: CPU GPU)
    // create pre-texture (different algos: CPU GPU [in mini scene with pre-appearance])
    // create final appearance
    
    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        renderManager = rm;
        viewPort = vp;
        
        Camera cam = new Camera(TexSize, TexSize);
        
        tempVP = new ViewPort("blub", cam);
        tempVP.setClearFlags(true, true, true);
        tempVP.setBackgroundColor(ColorRGBA.Black);
        
        FrameBuffer fb = new FrameBuffer(TexSize, TexSize, 1);
        fb.setColorTexture(cloudTex);
        
        // --- mini scene ---
        Picture quad = new Picture("bluba");
        quad.setPosition(0, 0);
        quad.setWidth(TexSize);
        quad.setHeight(TexSize);
        
        // this shader does the real work... later
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        mat.setColor("Color", ColorRGBA.Orange.mult(new ColorRGBA(1f, 1f, 1f, 0.5f)));
        mat.setTexture("ColorMap", heightFieldTex);
        quad.setMaterial(mat);
        quad.updateGeometricState();
        
        tempVP.attachScene(quad);
        tempVP.setOutputFrameBuffer(fb);
        // --- end ---
        
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
        tempVP.clearScenes();
        tempVP.setOutputFrameBuffer(null);
        tempVP = null;
        cloudTex = null;
    }
    
    public Texture2D getCloudTex() {
        return cloudTex;
    }
    
    private void updateAndRender() {
        // This uses CPU-only algorithms for now, our FrameBuffer is unneeded atm
        CloudHeightField chf = new CloudHeightField(TexSize, 8);
        chf.setZoom(48);
        chf.setShift(0);
        chf.setCloudCover(50);
        float[][] heightField = chf.generate();
        
        //opCopyHeightField2Texture(heightField, heightFieldTex);
        opRenderHeightField2Texture(heightField, cloudTex);
        
        //renderManager.renderViewPort(tempVP, 0);
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
//                int index = (row*TexSize + column)*4;
                if((column == 23 && row == 0) || (column == 117 && row == 0) || (column == 42 && row == 42)) {
                    buf.put(index+0, (byte) 255); // R
                    buf.put(index+1, (byte) 128); // G
                    buf.put(index+2, (byte) 0); // B
                } else {
                    buf.put(index+0, (byte) alpha); // R
                    buf.put(index+1, (byte) alpha); // G
                    buf.put(index+2, (byte) alpha); // B
                }
                buf.put(index+3, (byte) 255); // A
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
        
        final Vector3f sunPos = new Vector3f(TexSize/2, TexSize/2, 5000);
        final int size = TexSize;
        final int maxSteps = 10;
        final float wayFactor = 0.0005f;
        final float cloudSharpness = 0.96f;
        
        Vector3f v = new Vector3f(), vadd = new Vector3f();
        float z = 255;
        boolean breakOnCloudExit = sunPos.z < -z || sunPos.z > z;
        
        ByteBuffer buf = heightFieldTexture.getImage().getData(0);
        buf.rewind();
        // render complete image
        for(int column = 0; column < size; column++) {
            for(int row = 0; row < size; row++) {
                // render one texel
                float alpha = heightField[column][row];
                if(alpha == 0) continue;
                boolean lastWasInCloud = true;
                float wayInClouds = 0;
                v.set(column, row, -alpha);
                vadd.set(sunPos.x-v.x, sunPos.y-v.y, sunPos.z-v.z);
                vadd.mult(1f/maxSteps);
                float length = vadd.length();
                for(int k = 0; k < maxSteps; k++, v.add(vadd)) {
                    if(v.z < -z || v.z > z) break;
                    int clamped_vx = (int)(MathExt.clamp(v.x, 0, size-1));
                    int clamped_vy = (int)(MathExt.clamp(v.y, 0, size-1));
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
                int index = column*size*4 + row*4;
                buf.put(index+0, (byte) color); // R
                buf.put(index+1, (byte) color); // G
                buf.put(index+2, (byte) color); // B
                buf.put(index+3, (byte) alpha); // A
            }
        }
        heightFieldTexture.getImage().setData(0, buf);
    }
}
