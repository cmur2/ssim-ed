package de.mycrobase.ssim.ed;

import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.renderer.Camera;

/**
 * This custom implementation does not try to manage input mappings on it's own
 * but rather integrates well into the InputMappingAppState-controlled system.
 * 
 * @author cn
 */
public class CustomFlyByCamera extends FlyByCamera {

    public CustomFlyByCamera(Camera cam) {
        super(cam);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @Override
    public void setDragToRotate(boolean dragToRotate) {
        // ignore
    }
    
    @Override
    public void registerWithInput(InputManager inputManager) {
        this.inputManager = inputManager;
        
        inputManager.addListener(this,
            "FLYCAM_Left", "FLYCAM_Right", "FLYCAM_Up", "FLYCAM_Down",
            "FLYCAM_StrafeLeft", "FLYCAM_StrafeRight", "FLYCAM_Forward", "FLYCAM_Backward",
            "FLYCAM_ZoomIn", "FLYCAM_ZoomOut",
            "FLYCAM_Rise", "FLYCAM_Lower");
    }
    
    @Override
    public void unregisterInput() {
        inputManager.removeListener(this);
    }
}
