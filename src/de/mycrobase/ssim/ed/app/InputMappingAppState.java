package de.mycrobase.ssim.ed.app;

import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;

import de.mycrobase.ssim.ed.input.MappingSet;
import de.mycrobase.ssim.ed.input.MappingsParser;

public class InputMappingAppState extends BasicAppState {

    private static final Logger logger = Logger.getLogger(InputMappingAppState.class);
    private static final float UpdateInterval = 1f; // in seconds
    
    // exists only while AppState is attached
    private HashMap<String, MappingSet> mappingSets;
    private MappingSet currentMappingSet;
    
    public InputMappingAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        mappingSets = new HashMap<String, MappingSet>();
        
        // simply load into mappingSets
        loadMappings("default");
        loadMappings("de");
        loadMappings("us");
        
        currentMappingSet = getCurrentMappingSet();
        logger.info("Using mapping set: " + currentMappingSet.getId());
        
        switchMappings(mappingSets.get("default"), currentMappingSet);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        // switch back to default mappings
        switchMappings(currentMappingSet, mappingSets.get("default"));
        
        mappingSets = null;
        currentMappingSet = null;
    }
    
    private void loadMappings(String name) {
        MappingSet m = MappingsParser.load(getApp().getAssetManager(), String.format("input/keys_%s.xml", name));
        
        if(!m.getId().equals(name)) {
            logger.warn("Mapping set ID does not match file name! "+name);
        }
        
        mappingSets.put(m.getId(), m);
    }
    
    private MappingSet getCurrentMappingSet() {
        String name = "default";
        if(Locale.getDefault().getCountry().length() > 0) {
            name = Locale.getDefault().getCountry().toLowerCase();
        }
        if(!mappingSets.containsKey(name)) {
            name = "default";
        }
        // TODO: select default after locale, allow change via setting, allow user specific
        return mappingSets.get(name);
    }
    
    private void switchMappings(MappingSet oldMap, MappingSet newMap) {
        oldMap.revertAt(getApp().getInputManager());
        newMap.applyAt(getApp().getInputManager());
    }
}
