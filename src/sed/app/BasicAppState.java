package sed.app;

import sed.Main;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;

public class BasicAppState extends AbstractAppState {
    
    private AppStateManager stateManager;
    private Main app;
    
    public BasicAppState() {
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        // keep references of state manager and the main app 
        this.stateManager = stateManager;
        this.app = (Main) app;
    }
    
    protected Main getApp() {
        return app;
    }
    
    protected <T extends AppState> T getState(Class<T> stateClass) {
        return stateManager.getState(stateClass);
    }
}
