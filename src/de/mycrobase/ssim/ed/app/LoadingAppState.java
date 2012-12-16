package de.mycrobase.ssim.ed.app;

import java.util.List;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;

import de.mycrobase.ssim.ed.app.screen.LoadingScreenAppState;

public class LoadingAppState extends BasicAppState {
    
    private List<LoadStep> loadSteps;
    private int stepCounter;
    private float progressCounter;
    private float weightSum;
    
    // exists only while AppState is attached
    
    public LoadingAppState(List<LoadStep> loadSteps) {
        this.loadSteps = loadSteps;
        
        stepCounter = 0;
        progressCounter = 0f;
        
        weightSum = 0f;
        for(LoadStep loadStep : loadSteps) {
            weightSum += loadStep.getWeight();
        }
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
    }
    
    @Override
    public void update(float dt) {
        super.update(dt);
        
        if(stepCounter < loadSteps.size()) {
            LoadStep loadStep = loadSteps.get(stepCounter);
            // set message and attach all states of this step
            getScreen().setStatusTextKey(loadStep.getMessage());
            for(AppState state : loadStep.getStates()) {
                getStateManager().attach(state);
            }
        } else if(stepCounter == loadSteps.size()) {
            // don't exit here because progress isn't yet at 100% 
        } else {
            // detach ourself since loading is complete
            getStateManager().detach(this);
            // callback the app after finished loading
            getApp().doGameInitDone();
        }
        
        stepCounter++;
        
        // increment progress for last loaded step
        if(stepCounter <= loadSteps.size()) {
            progressCounter += loadSteps.get(stepCounter-1).getWeight() / weightSum;
        }
        getScreen().setProgress(progressCounter);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
    }
    
    private LoadingScreenAppState getScreen() {
        return getState(LoadingScreenAppState.class);
    }
    
    private void doingThings() {
        try { Thread.sleep(500); } catch(InterruptedException ex) { ex.printStackTrace(); }
    }
    
    public static class LoadStep {
        
        private String message;
        private float weight;
        private AppState[] states;
        
        public LoadStep(String message, float weight, AppState... states) {
            this.message = message;
            this.weight = weight;
            this.states = states;
        }
        
        public String getMessage() {
            return message;
        }
        
        public float getWeight() {
            return weight;
        }
        
        public AppState[] getStates() {
            return states;
        }
    }
}
