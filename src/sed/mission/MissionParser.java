package sed.mission;

import org.jdom.DataConversionException;
import org.jdom.Element;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;

public class MissionParser {
    
    //public MissionParser(AssetManager mgr, String name) {
    //    Element missionXml = mgr.loadAsset(new AssetKey<Element>(name));
    //}
    
    public static Mission load(AssetManager mgr, String name) {
        Element missionXml = mgr.loadAsset(new AssetKey<Element>(name));
        
        BasicMission m = new BasicMission();
        
        m.setMapFile(missionXml.getChild("map").getAttribute("file").getValue());
        try {
            m.setLatitude(missionXml.getChild("location").getAttribute("latitude").getFloatValue());
            m.setLongitude(missionXml.getChild("location").getAttribute("longitude").getFloatValue());
        } catch(DataConversionException ex) {
            ex.printStackTrace();
        }
        
        return m;
    }
}
