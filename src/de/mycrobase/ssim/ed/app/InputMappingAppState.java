package de.mycrobase.ssim.ed.app;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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
        
        // undo the default mappings
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
        LinkedList<String> nameFallbacks = new LinkedList<String>();
        // default is default
        nameFallbacks.add("default");
        // eval default Locale
        {
            String name = Locale.getDefault().getCountry().toLowerCase();
            if(name.length() > 0) {
                logger.info("Deriving mapping set from default Locale: " + name);
                nameFallbacks.add(name);
            }
        }
        // eval locale.input setting
        {
            String name = getApp().getSettingsManager().getString("locale.input").toLowerCase();
            if(!name.equals("auto")) {
                logger.info("Deriving mapping set from locale.input setting: " + name);
                nameFallbacks.add(name);
            } else {
                logger.info("locale.input setting is 'auto'");
            }
        }
        // reject unknown mapping sets
        for(Iterator<String> iter = nameFallbacks.iterator(); iter.hasNext(); ) {
            String name = iter.next();
            if(!mappingSets.containsKey(name)) {
                logger.info("Removing unknown mapping set name from fallback list: " + name);
                iter.remove();
            }
        }
        // return best survivor (default at least)
        return mappingSets.get(nameFallbacks.getLast());
    }
    
    private void switchMappings(MappingSet oldMap, MappingSet newMap) {
        oldMap.revertAt(getApp().getInputManager());
        newMap.applyAt(getApp().getInputManager());
    }
}
