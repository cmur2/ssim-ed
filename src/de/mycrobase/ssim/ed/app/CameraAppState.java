package de.mycrobase.ssim.ed.app;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.FlyByCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

/**
 * <b>Base layer</b> {@link AppState} providing ...
 * 
 * @author cn
 */
public class CameraAppState extends BasicAppState {
    
    private float maxVisibility;
    
    // exists only while AppState is attached
    private Camera cam;
    private FlyByCamera flyCam;
    
    public CameraAppState(float maxVisibility) {
        this.maxVisibility = maxVisibility;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);

        cam = getApp().getCamera();
        flyCam  = getApp().getFlyByCamera();
        
        flyCam.setMoveSpeed(3e2f);
        //flyCam.setDragToRotate(true);
        
        cam.setLocation(new Vector3f(0, -450f, 0));
        //cam.setRotation(new Quaternion(new float[] {-90*FastMath.DEG_TO_RAD,0,0}));
        cam.lookAtDirection(Vector3f.UNIT_Y, Vector3f.UNIT_Z);
        //cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Z);
        cam.setFrustumPerspective(60f, (float)cam.getWidth() / cam.getHeight(), 1f, maxVisibility);
        
        getApp().getInputManager().deleteTrigger("FLYCAM_Lower", new KeyTrigger(KeyInput.KEY_Z));
        getApp().getInputManager().addMapping("FLYCAM_Lower", new KeyTrigger(KeyInput.KEY_Y));
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        cam = null;
        flyCam = null;
    }
}
