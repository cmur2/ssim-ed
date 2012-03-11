package sed.sky;

import java.nio.ByteBuffer;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Texture2D;
import com.jme3.texture.Image.Format;
import com.jme3.ui.Picture;
import com.jme3.util.BufferUtils;

/**
 * This {@link CloudProcessor} uses the GPU to render the clouds into a
 * {@link FrameBuffer} that is used to back the final texture. The generation
 * of the cloud heightfield is done on CPU via {@link CloudHeightField}.
 * 
 * @author cn
 */
public class GPUCloudProcessor extends CloudProcessor {
    
    private AssetManager assetManager;
    private RenderManager renderManager;
    private ViewPort mainViewPort;
    
    // mini scene components
    private Texture2D heightFieldTex;
    private Material tempMat;
    private ViewPort tempViewPort;
    
    // (weather) variables
    private float cloudSharpness;
    private float wayFactor;
    private Vector3f sunPosition;
    private ColorRGBA sunLightColor;
    
    private CloudHeightField cloudHeightField;

    public GPUCloudProcessor(AssetManager assetManager, int texSize, float updateInterval) {
        super(texSize, updateInterval);
        this.assetManager = assetManager;
        
        heightFieldTex = new Texture2D(getTexSize(), getTexSize(), Format.RGBA8);
        
        cloudHeightField = new CloudHeightField(texSize, getNumOctaves());
    }
    
    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        renderManager = rm;
        mainViewPort = vp;
        
        Camera cam = new Camera(getTexSize(), getTexSize());
        
        tempViewPort = new ViewPort("Cloud-Render-ViewPort", cam);
        tempViewPort.setClearFlags(true, true, true);
        tempViewPort.setBackgroundColor(ColorRGBA.Black);
        
        FrameBuffer fb = new FrameBuffer(getTexSize(), getTexSize(), 1);
        fb.setColorTexture(getCloudTex());
        
        // --- mini scene ---
        Picture quad = new Picture("Cloud-Render-Target");
        quad.setPosition(0, 0);
        quad.setWidth(getTexSize());
        quad.setHeight(getTexSize());
        
        //Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //mat.setColor("Color", ColorRGBA.Orange.mult(new ColorRGBA(1f, 1f, 1f, 0.5f)));
        //mat.setTexture("ColorMap", heightFieldTex);
        tempMat = new Material(assetManager, "shaders/CloudRender.j3md");
        tempMat.setFloat("ImageSize", getTexSize());
        tempMat.setFloat("MaxSteps", getMaxSteps());
        tempMat.setFloat("CloudSharpness", cloudSharpness);
        tempMat.setFloat("WayFactor", wayFactor);
        tempMat.setVector3("SunPosition", sunPosition);
        tempMat.setColor("SunLightColor", sunLightColor);
        tempMat.setTexture("HeightField", heightFieldTex);
        quad.setMaterial(tempMat);
        quad.updateGeometricState();
        
        tempViewPort.attachScene(quad);
        tempViewPort.setOutputFrameBuffer(fb);
        // --- end ---
        
        super.initialize(rm, vp);
        updateAndRender();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        tempViewPort.clearScenes();
        tempViewPort.setOutputFrameBuffer(null);
        tempViewPort = null;
        heightFieldTex = null;
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
        if(isInitialized()) {
            tempMat.setFloat("CloudSharpness", cloudSharpness);
        }
    }

    public float getWayFactor() {
        return wayFactor;
    }

    public void setWayFactor(float wayFactor) {
        this.wayFactor = wayFactor;
        if(isInitialized()) {
            tempMat.setFloat("WayFactor", wayFactor);
        }
    }

    public Vector3f getSunPosition() {
        return sunPosition;
    }

    public void setSunPosition(Vector3f sunPosition) {
        this.sunPosition = sunPosition;
        if(isInitialized()) {
            tempMat.setVector3("SunPosition", sunPosition);
        }
    }

    public ColorRGBA getSunLightColor() {
        return sunLightColor;
    }

    public void setSunLightColor(ColorRGBA sunLightColor) {
        this.sunLightColor = sunLightColor;
        if(isInitialized()) {
            tempMat.setColor("SunLightColor", sunLightColor);
        }
    }

    protected void updateAndRender() {
        // generate height field on CPU
        float[][] heightField = cloudHeightField.generate();
        copyHeightFieldToTexture(heightField, heightFieldTex);
        renderManager.renderViewPort(tempViewPort, 0);
    }

    private void copyHeightFieldToTexture(
        float[][] heightField, Texture2D heightFieldTexture)
    {
        int texSize = getTexSize();
        
        // lazy allocate backing ByteBuffer
        if(heightFieldTexture.getImage().getData(0) == null) {
            heightFieldTexture.getImage().setData(
                BufferUtils.createByteBuffer(texSize * texSize * 4));
        }
        // copy alpha to texel
        ByteBuffer buf = heightFieldTexture.getImage().getData(0);
        buf.rewind();
        for(int column = 0; column < texSize; column++) {
            for(int row = 0; row < texSize; row++) {
                float alpha = heightField[column][row];
                int index = (row*texSize + column)*4;
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
            }
        }
        heightFieldTexture.getImage().setData(0, buf);
    }
}
