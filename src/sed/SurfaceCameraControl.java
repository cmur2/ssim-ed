package sed;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.CameraControl;
import com.jme3.scene.control.Control;

/**
 * This control keeps the {@link Spatial} to control in sync with the given
 * {@link Camera}. In contrast to {@link CameraControl} it only updates
 * (overwrites) the location and there only x and z component.
 * So the controlled Spatial will follow the camera on surface (y=0) niveau
 * but not rotate with it.
 * 
 * @author cn
 */
public class SurfaceCameraControl extends AbstractControl {

    private Camera camera;
    
    private Vector3f loc = new Vector3f();
    
    public SurfaceCameraControl(Camera camera) {
        this.camera = camera;
    }
    
    public void setCamera(Camera camera) {
        this.camera = camera;
    }
    
    public Camera getCamera() {
        return camera;
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if(spatial != null && camera != null) {
            loc = spatial.getLocalTranslation();
            loc.x = camera.getLocation().x;
            loc.z = camera.getLocation().z;
            spatial.setLocalTranslation(loc);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }
    
    @Override
    public Control cloneForSpatial(Spatial newSpatial) {
        SurfaceCameraControl control = new SurfaceCameraControl(camera);
        control.setSpatial(newSpatial);
        control.setEnabled(isEnabled());
        return control;
    }
}
