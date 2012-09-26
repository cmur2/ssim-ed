package de.mycrobase.ssim.ed.helper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.jme3.app.SimpleApplication;

import de.mycrobase.ssim.ed.GameMode;
import de.mycrobase.ssim.ed.GameModeListener;
import de.mycrobase.ssim.ed.SSimApplication;
import de.mycrobase.ssim.ed.mission.Mission;
import de.mycrobase.ssim.ed.settings.SettingsManager;

public abstract class SteppedSSimApplication extends SimpleApplication implements SSimApplication {
    
    private Thread caller;
    
    private ScheduledExecutorService executor;
    
    public SteppedSSimApplication() {
        caller = Thread.currentThread();
    }
    
    @Override
    public void simpleInitApp() {
        executor = Executors.newScheduledThreadPool(2);
    }
    
    @Override
    public void simpleUpdate(float tpf) {
        super.simpleUpdate(tpf);
        
        pause();
    }

    @Override
    public void destroy() {
        super.destroy();
        
        // shutdown all (non daemon) worker pool threads
        executor.shutdown();
    }

    @Override
    public ScheduledExecutorService getExecutor() {
        return executor;
    }
    
    @Override
    public SettingsManager getSettingsManager() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public float getSpeed() {
        return speed;
    }
    
    @Override
    public void setSpeed(float speed) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public GameMode getCurrentMode() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void addGameModeListener(GameModeListener lis) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void removeGameModeListener(GameModeListener lis) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void doGameInit(Mission mission) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void doGamePause() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void doGameResume() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void doGameExit() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Gets called from controller/JUnit thread!
     */
    public void waitFor() {
        speed = 1;
        try {
            while(true) { Thread.sleep(1000); }
        } catch(InterruptedException ex) {
            // OK let's continue
        }
    }
    
    private void pause() {
        speed = 0;
        // and notify the boss
        caller.interrupt();
    }
}
