package de.mycrobase.ssim.ed.input;

import org.apache.log4j.Logger;
import org.jdom.Element;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;

public class MappingsParser {
    
    private static final Logger logger = Logger.getLogger(MappingsParser.class);
    
    public static MappingSet load(AssetManager mgr, String name) {
        Element mappingsXml = mgr.loadAsset(new AssetKey<Element>(name));
        
        MappingSet m = new MappingSet(mappingsXml.getAttributeValue("id"));
        
        for(Object o : mappingsXml.getChildren("map")) {
            Element map = (Element) o;
            
            String action = map.getAttributeValue("action");
            Trigger trigger = null;
            
            if(map.getAttribute("key") != null) {
                trigger = getKeyTrigger("KEY_" + map.getAttributeValue("key"));
            } else if(map.getAttribute("button") != null) {
                trigger = getMouseButtonTrigger("BUTTON_" + map.getAttributeValue("button"));
            }
            
            if(trigger != null) {
                m.put(trigger, action);
            } else {
                logger.error(String.format(
                    "Encountered <map> statement without trigger in %s for action %s!",
                    name, action));
            }
        }
        
        return m;
    }
    
    private static KeyTrigger getKeyTrigger(String key) {
        try {
            int keyCode = (Integer) KeyInput.class.getDeclaredField(key).get(Integer.class);
            return new KeyTrigger(keyCode);
        } catch(IllegalAccessException ex) {
            logger.error(String.format("No key code constant found for %s!", key), ex);
            return null;
        } catch(NoSuchFieldException ex) {
            logger.error(String.format("No key code constant found for %s!", key), ex);
            return null;
        }
    }
    
    private static MouseButtonTrigger getMouseButtonTrigger(String button) {
        try {
            int mouseButton = (Integer) MouseInput.class.getDeclaredField(button).get(Integer.class);
            return new MouseButtonTrigger(mouseButton);
        } catch(IllegalAccessException ex) {
            logger.error(String.format("No mouse button code constant found for %s!", button), ex);
            return null;
        } catch(NoSuchFieldException ex) {
            logger.error(String.format("No mouse button code constant found for %s!", button), ex);
            return null;
        }
    }
}
