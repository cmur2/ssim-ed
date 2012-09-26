package de.mycrobase.ssim.ed.mission;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jme3.system.JmeContext.Type;

import de.mycrobase.ssim.ed.helper.Logging;
import de.mycrobase.ssim.ed.helper.SteppedSSimApplication;
import de.mycrobase.ssim.ed.helper.categories.Fast;
import de.mycrobase.ssim.ed.util.MapLoader;
import de.mycrobase.ssim.ed.util.PropertiesLoader;
import de.mycrobase.ssim.ed.util.XMLLoader;

@Category(Fast.class)
public class MissionParserTest {

    @BeforeClass
    public static void setUp() {
        Logging.require();
    }

    @Test
    public void testLoad() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        Mission m = MissionParser.load(app.getAssetManager(), "missions/test_mission.xml");
        
        assertEquals("test_mission.title", m.getTitle());
        assertEquals("test_mission.description", m.getDescription());
        assertEquals(0f, m.getLatitude(), 1e-4f);
        assertEquals(1f, m.getLongitude(), 1e-4f);
        assertEquals(23, m.getDayOfYear());
        assertEquals(8.00f, m.getTimeOfDay(), 1e-4f);
        assertEquals("Test.map", m.getMapFile());
    }

    @Test
    public void testLoadBroken() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        assertNull(MissionParser.load(
            app.getAssetManager(), "missions/test_mission_broken.xml"));
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
