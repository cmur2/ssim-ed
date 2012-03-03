package sed.app;

import sed.Main;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;

/**
 * Base class for all other {@linkplain AppState}s that provides a reference
 * to the {@link Main} app and, more important, one to the
 * {@link AppStateManager} which is kept internally to provide
 * {@link #getState(Class)} which is the basic dependency mechanism for all
 * AppStates.
 * 
 * Additionally it implements a basic interval timer for the
 * {@link #update(float)} loop that performs calls to {@link #intervalUpdate()}
 * after the specified {@link #intervalTime} passed.
 * 
 * @author cn
 */
public class BasicAppState extends AbstractAppState {
    
    private AppStateManager stateManager;
    private Main app;

    /**
     * Time (in seconds) that must pass to execute another {@link #intervalUpdate()}
     */
    private float intervalTime;
    /**
     * Time (in seconds) that is passed since the last {@link #intervalUpdate()}
     */
    private float passedTime;

    public BasicAppState() {
        this.intervalTime = 60f; // seconds
    }
    
    public BasicAppState(float intervalTime) {
        this.intervalTime = intervalTime;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        // keep references of state manager and the main app 
        this.stateManager = stateManager;
        this.app = (Main) app;
        
        passedTime = 0;
    }
    
    @Override
    public void update(float dt) {
        super.update(dt);
        
        if(passedTime >= intervalTime) {
            passedTime -= intervalTime;
            intervalUpdate();
        }
        
        passedTime += dt;
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
    }
    
    // internal API
    
    /**
     * Method invoked from {@link #update(float)} after a certain
     * ({@link #intervalTime}) amount of time passed.
     * Designed to be overridden. 
     */
    protected void intervalUpdate() {
    }
    
    protected float getIntervalTime() {
        return intervalTime;
    }
    
    protected void setIntervalTime(float intervalTime) {
        this.intervalTime = intervalTime;
    }
    
    protected Main getApp() {
        return app;
    }
    
    protected <T extends AppState> T getState(Class<T> stateClass) {
        return stateManager.getState(stateClass);
    }
}
