package de.mycrobase.ssim.ed.app;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jme3.system.JmeContext.Type;

import de.mycrobase.ssim.ed.helper.Logging;
import de.mycrobase.ssim.ed.helper.SteppedSSimApplication;
import de.mycrobase.ssim.ed.helper.categories.Slow;
import de.mycrobase.ssim.ed.mission.Mission;
import de.mycrobase.ssim.ed.mission.MissionParser;
import de.mycrobase.ssim.ed.util.MapLoader;
import de.mycrobase.ssim.ed.util.PropertiesLoader;
import de.mycrobase.ssim.ed.util.XMLLoader;

@Category(Slow.class)
public class AerialAppStateTest {

    @BeforeClass
    public static void setUp() {
        Logging.require();
    }

    @Test(timeout = 60*1000)
    public void testAPI() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        AerialAppState state = app.getStateManager().getState(AerialAppState.class);
        
        assertNotNull(state.getFogColor());
        assertTrue(state.getFogDensity() >= 0f);
        
        app.stop();
    }
    
    @Ignore
    @Test(timeout = 60*1000)
    public void testUpdate() {
    }
    
    private static class Helper extends SteppedSSimApplication {
        
        @Override
        public void simpleInitApp() {
            super.simpleInitApp();
            
            assetManager.registerLoader(XMLLoader.class, "xml");
            assetManager.registerLoader(MapLoader.class, "map");
            assetManager.registerLoader(PropertiesLoader.class, "properties");
            
            Mission mission = MissionParser.load(getAssetManager(), "missions/test_mission.xml");
            
            // AppState base layer:
            // these serve as a common base for the higher AppStates
            stateManager.attach(new SimClockAppState(mission));
            stateManager.attach(new CameraAppState(1000f));
            stateManager.attach(new WeatherAppState("clear"));
            stateManager.attach(new SkyAppState(10000f, mission));
            stateManager.attach(new AerialAppState());
        }
    }
}
