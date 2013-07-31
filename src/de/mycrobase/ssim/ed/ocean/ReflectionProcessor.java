package de.mycrobase.ssim.ed.ocean;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Plane;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.texture.Texture2D;

import de.mycrobase.ssim.ed.util.TempVars;

public class ReflectionProcessor implements SceneProcessor {
    
    private int texSize;
    private Spatial reflectionScene;
    private Spatial ignoreScene;
    
    // state
    private boolean init = false;
    
    private RenderManager rm;
    private ViewPort vp;
    private ViewPort reflectionView;
    private FrameBuffer reflectionBuffer;
    private Camera reflectionCam;
    private Texture2D reflectionTexture;
    
    private Vector3f targetLocation = new Vector3f();
    
    private Ray ray = new Ray();
    
    private Plane plane = new Plane(Vector3f.UNIT_Y, Vector3f.ZERO.dot(Vector3f.UNIT_Y));
    private float reflectionClippingOffset = -5f;
    private Plane reflectionClipPlane;
    
    private float savedTpf;
    
    public ReflectionProcessor(Spatial reflectionScene, int texSize) {
        this.reflectionScene = reflectionScene;
        this.texSize = texSize;
        
        reflectionTexture = new Texture2D(getTexSize(), getTexSize(), Format.RGBA8);
        
        reflectionClipPlane = plane.clone();
        reflectionClipPlane.setConstant(reflectionClipPlane.getConstant() + reflectionClippingOffset);
    }
    
    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        this.rm = rm;
        this.vp = vp;
        
        reflectionCam = new Camera(getTexSize(), getTexSize());
        
        reflectionView = new ViewPort("Reflection-View", reflectionCam);
        reflectionView.setClearFlags(true, true, true);
        reflectionView.setBackgroundColor(ColorRGBA.Black);
        
        reflectionBuffer = new FrameBuffer(getTexSize(), getTexSize(), 1);
        reflectionBuffer.setDepthBuffer(Format.Depth);
        reflectionBuffer.setColorTexture(getReflectionTexture());

        reflectionView.setOutputFrameBuffer(reflectionBuffer);
        reflectionView.addProcessor(new com.jme3.water.ReflectionProcessor(reflectionCam, reflectionBuffer, reflectionClipPlane));
        reflectionView.attachScene(reflectionScene);
        
        init = true;
    }
    
    @Override
    public boolean isInitialized() {
        return init;
    }
    
    @Override
    public void preFrame(float tpf) {
        savedTpf = tpf;
    }
    
    @Override
    public void postQueue(RenderQueue rq) {
        Camera sceneCam = rm.getCurrentCamera();
        ray.setOrigin(sceneCam.getLocation());
        ray.setDirection(sceneCam.getDirection());
        
        boolean inv = false;
        if (!ray.intersectsWherePlane(plane, targetLocation)) {
            ray.setDirection(ray.getDirection().negateLocal());
            ray.intersectsWherePlane(plane, targetLocation);
            inv = true;
        }
        Vector3f loc = plane.reflect(sceneCam.getLocation(), new Vector3f());
        reflectionCam.setLocation(loc);
        reflectionCam.setFrustum(sceneCam.getFrustumNear(),
                sceneCam.getFrustumFar(),
                sceneCam.getFrustumLeft(),
                sceneCam.getFrustumRight(),
                sceneCam.getFrustumTop(),
                sceneCam.getFrustumBottom());
        reflectionCam.setParallelProjection(false);
        
        TempVars vars = TempVars.get();
        
        vars.vect1.set(sceneCam.getLocation()).addLocal(sceneCam.getUp());
        float planeDistance = plane.pseudoDistance(vars.vect1);
        vars.vect2.set(plane.getNormal()).multLocal(planeDistance * 2.0f);
        vars.vect3.set(vars.vect1.subtractLocal(vars.vect2)).subtractLocal(loc).normalizeLocal().negateLocal();
        
        reflectionCam.lookAt(targetLocation, vars.vect3);
        if (inv) {
            reflectionCam.setAxes(reflectionCam.getLeft().negateLocal(), reflectionCam.getUp(), reflectionCam.getDirection().negateLocal());
        }
        
        vars.release();
        
        // disable cull hint
        CullHint oldCullHint = null;
        if(ignoreScene != null) {
            oldCullHint = ignoreScene.getCullHint();
            ignoreScene.setCullHint(CullHint.Always);
        }
        
        rm.renderViewPort(reflectionView, savedTpf);
        rm.getRenderer().setFrameBuffer(vp.getOutputFrameBuffer());
        rm.setCamera(sceneCam, false);
        
        if(oldCullHint != null) {
            ignoreScene.setCullHint(oldCullHint);
        }
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
        init = false;
        
        //rm.removePreView(reflectionView);
        
        // TODO: do clean up
    }
    
    public int getTexSize() {
        return texSize;
    }
    
    public Texture2D getReflectionTexture() {
        return reflectionTexture;
    }
    
    /**
     * Can be invoked after ReflectionProcessor creation to set a partial scene
     * that should be ignore while rendering the reflection (cull hint will be
     * {@link CullHint#Always}; original cull hint will be preserved). This is
     * useful for disabling the designated mirror surface.
     * 
     * @param ignoreScene a {@link Spatial} to be ignored
     */
    public void setIgnoreScene(Spatial ignoreScene) {
        this.ignoreScene = ignoreScene;
    }
}
