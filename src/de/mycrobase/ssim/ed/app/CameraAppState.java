package de.mycrobase.ssim.ed.app;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.collision.MotionAllowedListener;
import com.jme3.input.FlyByCamera;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import de.mycrobase.ssim.ed.CustomFlyByCamera;
import de.mycrobase.ssim.ed.GameMode;
import de.mycrobase.ssim.ed.GameModeListener;

/**
 * <b>Base layer</b> {@link AppState} providing camera handling.
 * 
 * @author cn
 */
public class CameraAppState extends BasicAppState implements GameModeListener {
    
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
        
        flyCam = new CustomFlyByCamera(cam);
        flyCam.setMoveSpeed(3e2f);
        flyCam.registerWithInput(getApp().getInputManager());
        
        // let the user never go below y=5 plane
        flyCam.setMotionAllowedListener(new MotionAllowedListener() {
            @Override
            public void checkMotionAllowed(Vector3f position, Vector3f velocity) {
                position.addLocal(velocity);
                position.y = Math.max(position.y, 5f);
            }
        });
        
        cam.setLocation(new Vector3f(0, 450f, 0));
        //cam.setRotation(new Quaternion(new float[] {-90*FastMath.DEG_TO_RAD,0,0}));
        //cam.lookAtDirection(Vector3f.UNIT_Y, Vector3f.UNIT_Z);
        //cam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Z);
        cam.setFrustumPerspective(60f, (float)cam.getWidth() / cam.getHeight(), 1f, maxVisibility);
        
        // manual call to avoid code duplication
        gameModeChanged(null, getApp().getCurrentMode());
        
        getApp().addGameModeListener(this);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        getApp().removeGameModeListener(this);
        
        flyCam.unregisterInput();
        
        cam = null;
        flyCam = null;
    }

    @Override
    public void gameModeChanged(GameMode oldMode, GameMode newMode) {
        if(newMode == GameMode.Running) {
            flyCam.setEnabled(true);
            getApp().getInputManager().setCursorVisible(false);
        } else {
            flyCam.setEnabled(false);
            getApp().getInputManager().setCursorVisible(true);
        }
    }
    
    // public API
    
    public float getMaxVisibility() {
        return maxVisibility;
    }
}
