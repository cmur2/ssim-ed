package de.mycrobase.ssim.ed.app;

import ssim.sim.SimClock;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;

import de.mycrobase.ssim.ed.mission.Mission;

public class SimClockAppState extends BasicAppState {
    
    private Mission mission;
    
    // exists only while AppState is attached
    private SimClock simClock;
    
    public SimClockAppState(Mission mission) {
        this.mission = mission;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        simClock = SimClock.createClock(mission.getTimeOfDay());
        
        if(simClock == null) {
            throw new UnsupportedOperationException("Couldn't initialize SimClock!");
        }
    }
    
    @Override
    public void update(float dt) {
        // super.update(dt);
        
        simClock.step(dt);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        simClock = null;
    }
    
    // public API
    
    public SimClock getSimClock() {
        return simClock;
    }
}
