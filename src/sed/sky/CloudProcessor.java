package sed.sky;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;

/**
 * This processor hooks into the render process to use preFrame to render
 * the clouds.
 * 
 * @author cn
 */
public class CloudProcessor implements SceneProcessor {

    private static final int TexSize = 512;
    
    private boolean init = false;
    private float time = 0;
    
    private AssetManager assetManager;
    private RenderManager renderManager;
    private ViewPort viewPort;
    
    private ViewPort tempVP;
    private Texture2D tex;
    
    public CloudProcessor(AssetManager assetManager) {
        this.assetManager = assetManager;
        
        tex = new Texture2D(TexSize, TexSize, Format.RGBA8);
    }
    
    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        renderManager = rm;
        viewPort = vp;
        
        Camera cam = new Camera(TexSize, TexSize);
        
        tempVP = new ViewPort("blub", cam);
        tempVP.setClearFlags(true, true, true);
        tempVP.setBackgroundColor(ColorRGBA.Black);
        
        FrameBuffer fb = new FrameBuffer(TexSize, TexSize, 1);
        fb.setColorTexture(tex);
        
        // --- mini scene ---
        Picture quad = new Picture("bluba");
        quad.setPosition(0, 0);
        quad.setWidth(TexSize);
        quad.setHeight(TexSize);
        
        // this shader does the real work... later
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Orange.mult(new ColorRGBA(1f, 1f, 1f, 0.5f)));
        quad.setMaterial(mat);
        quad.updateGeometricState();
        
        tempVP.attachScene(quad);
        tempVP.setOutputFrameBuffer(fb);
        // --- end ---
        
        renderManager.renderViewPort(tempVP, 0);
        
        init = true;
    }

    @Override
    public boolean isInitialized() {
        return init;
    }

    @Override
    public void preFrame(float tpf) {
        if(time > 1) {
            time = 0;
            renderManager.renderViewPort(tempVP, 0);
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
        tex = null;
    }
    
    public Texture2D getTex() {
        return tex;
    }
}
