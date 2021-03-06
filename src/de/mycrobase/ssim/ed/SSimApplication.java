package de.mycrobase.ssim.ed;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.audio.Listener;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;

import de.mycrobase.ssim.ed.app.screen.LoadingScreenAppState;
import de.mycrobase.ssim.ed.mission.Mission;
import de.mycrobase.ssim.ed.settings.SettingsManager;

public interface SSimApplication {
    
    public AssetManager getAssetManager();
    public Camera getCamera();
    public FlyByCamera getFlyByCamera();
    public ViewPort getGuiViewPort();
    public ViewPort getViewPort();
    public Node getGuiNode();
    public Node getRootNode();
    public InputManager getInputManager();
    public AudioRenderer getAudioRenderer();
    public Listener getListener();
    public void stop();
    public <V> Future<V> enqueue(Callable<V> callable);
    
    public ScheduledExecutorService getExecutor();
    public SettingsManager getSettingsManager();
    public float getSpeed();
    public void setSpeed(float speed);
    
    public GameMode getCurrentMode();
    public void addGameModeListener(GameModeListener lis);
    public void removeGameModeListener(GameModeListener lis);
    public void doGameInit(Mission mission);
    public void doGameInitDone();
    public void doGamePause();
    public void doGameResume();
    public void doGameExit();
    
}
