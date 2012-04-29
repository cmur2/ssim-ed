package de.mycrobase.ssim.ed.input;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.jme3.input.InputManager;
import com.jme3.input.controls.Trigger;

public class MappingSet {
    
    private static final Logger logger = Logger.getLogger(MappingSet.class);
    
    private String id;
    
    /**
     * Maps triggers to actions.
     */
    private HashMap<Trigger, String> mappings = new HashMap<Trigger, String>();
    
    public MappingSet(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }

    public void put(Trigger trigger, String action) {
        mappings.put(trigger, action);
    }

    public void applyAt(InputManager inputManager) {
        logger.info("Applying mapping set: " + getId());
        
        for(Map.Entry<Trigger, String> map : mappings.entrySet()) {
            String mapping = map.getValue();
            Trigger trigger = map.getKey();
            logger.debug(String.format("add %s -> %s", mapping, trigger.getName()));
            inputManager.addMapping(mapping, trigger);
        }
    }
    
    public void revertAt(InputManager inputManager) {
        logger.info("Reverting mapping set: " + getId());
        
        for(Map.Entry<Trigger, String> map : mappings.entrySet()) {
            String mapping = map.getValue();
            Trigger trigger = map.getKey();
            logger.debug(String.format("remove %s -> %s", mapping, trigger.getName()));
            inputManager.deleteTrigger(mapping, trigger);
        }
    }
}
