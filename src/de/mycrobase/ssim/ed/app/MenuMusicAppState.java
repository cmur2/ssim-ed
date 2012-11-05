package de.mycrobase.ssim.ed.app;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioNode;

import de.mycrobase.ssim.ed.GameMode;
import de.mycrobase.ssim.ed.GameModeListener;

public class MenuMusicAppState extends BasicAppState implements GameModeListener {
    
    private static final float UpdateInterval = 0.1f; // in seconds
    
    // exists only while AppState is attached
    private AudioNode menuMusic;
    
    public MenuMusicAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        menuMusic = new AudioNode(getApp().getAssetManager(), "audio/09.AlisterFlint-UnderTheSurface.ogg", true);
        menuMusic.setPositional(false);
        //menuMusic.setLooping(true);
        menuMusic.setTimeOffset(5f);
        getApp().getRootNode().attachChild(menuMusic);
        
        // manual call to avoid code duplication
        gameModeChanged(null, getApp().getCurrentMode());
        
        getApp().addGameModeListener(this);
    }
    
    @Override
    protected void intervalUpdate(float dt) {
        // adjust volume
        menuMusic.setVolume(getApp().getSettingsManager().getFloat("sound.music.volume"));
        
        if(menuMusic.getStatus() == AudioNode.Status.Stopped) {
            // play again! (needed since menu music is streamed)
            menuMusic.play();
        }
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        menuMusic.stop();
        getApp().getRootNode().detachChild(menuMusic);
        
        menuMusic = null;
    }
    
    @Override
    public void gameModeChanged(GameMode oldMode, GameMode newMode) {
        if(newMode == GameMode.Stopped) {
            menuMusic.play();
        } else {
            menuMusic.stop();
        }
    }
}
