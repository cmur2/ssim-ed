package de.mycrobase.ssim.ed.mission;

import org.apache.log4j.Logger;
import org.jdom.DataConversionException;
import org.jdom.Element;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;

public class MissionParser {
    
    private static final Logger logger = Logger.getLogger(MissionParser.class);
    
    private MissionParser() {}
    
    public static Mission load(AssetManager mgr, String name) {
        Element missionXml = mgr.loadAsset(new AssetKey<Element>(name));
        
        BasicMission m = new BasicMission(missionXml.getAttributeValue("id"));
        
        m.setDescription(missionXml.getChild("description").getText());
        m.setTitle(missionXml.getChild("title").getText());
        m.setMapFile(missionXml.getChild("map").getAttributeValue("file"));
        try {
            m.setLatitude(missionXml.getChild("location").getAttribute("latitude").getFloatValue());
            m.setLongitude(missionXml.getChild("location").getAttribute("longitude").getFloatValue());
            m.setDayOfYear(missionXml.getChild("datetime").getAttribute("dayOfYear").getIntValue());
            m.setTimeOfDay(missionXml.getChild("datetime").getAttribute("timeOfDay").getFloatValue());
        } catch(DataConversionException ex) {
            logger.error("Could not parse mission file:", ex);
            return null;
        }
        
        return m;
    }
}
