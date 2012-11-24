package de.mycrobase.ssim.ed.input;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.system.JmeContext.Type;

import de.mycrobase.ssim.ed.helper.Logging;
import de.mycrobase.ssim.ed.helper.SteppedSSimApplication;
import de.mycrobase.ssim.ed.helper.categories.Fast;
import de.mycrobase.ssim.ed.mission.Mission;
import de.mycrobase.ssim.ed.mission.MissionParser;
import de.mycrobase.ssim.ed.util.MapLoader;
import de.mycrobase.ssim.ed.util.PropertiesLoader;
import de.mycrobase.ssim.ed.util.XMLLoader;

@Category(Fast.class)
public class MappingsParserTest {

    @BeforeClass
    public static void setUp() {
        Logging.require();
    }

    @Test
    public void testLoad() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        MappingSet m = MappingsParser.load(app.getAssetManager(), "input/keys_test.xml");
        
        assertEquals("test", m.getId());
        // only 3 valid mappings found!
        assertEquals(4, m.getMappings().size());
        
        assertTrue(containsTrigger(m, new KeyTrigger(KeyInput.KEY_A)));
        assertTrue(containsTrigger(m, new KeyTrigger(KeyInput.KEY_B)));
        assertFalse(containsTrigger(m, new KeyTrigger(KeyInput.KEY_C)));
        
        assertTrue(containsTrigger(m, new MouseButtonTrigger(MouseInput.BUTTON_LEFT)));
        assertFalse(containsTrigger(m, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT)));
        
        assertTrue(containsTrigger(m, new MouseAxisTrigger(MouseInput.AXIS_X, false)));
        assertFalse(containsTrigger(m, new MouseAxisTrigger(MouseInput.AXIS_Y, false)));
        assertFalse(containsTrigger(m, new MouseAxisTrigger(MouseInput.AXIS_Y, true)));
        
        assertEquals("TEST_a", getMapping(m, new KeyTrigger(KeyInput.KEY_A)));
        assertEquals("TEST_b", getMapping(m, new KeyTrigger(KeyInput.KEY_B)));
        assertEquals("TEST_left", getMapping(m, new MouseButtonTrigger(MouseInput.BUTTON_LEFT)));
        assertEquals("TEST_x", getMapping(m, new MouseAxisTrigger(MouseInput.AXIS_X, false)));
    }

    private static boolean containsTrigger(MappingSet m, Trigger trig) {
        for(Map.Entry<Trigger, String> map : m.getMappings().entrySet()) {
            Trigger trigger = map.getKey();
            if(trigger.triggerHashCode() == trig.triggerHashCode()) {
                return true;
            }
        }
        return false;
    }

    private static String getMapping(MappingSet m, Trigger trig) {
        for(Map.Entry<Trigger, String> map : m.getMappings().entrySet()) {
            Trigger trigger = map.getKey();
            if(trigger.triggerHashCode() == trig.triggerHashCode()) {
                return map.getValue();
            }
        }
        return null;
    }
    
    private static class Helper extends SteppedSSimApplication {
        
        @Override
        public void simpleInitApp() {
            assetManager.registerLoader(XMLLoader.class, "xml");
            assetManager.registerLoader(MapLoader.class, "map");
            assetManager.registerLoader(PropertiesLoader.class, "properties");
        }
    }
}
