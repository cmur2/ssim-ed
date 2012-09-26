package de.mycrobase.ssim.ed.app;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import ssim.sim.SimClock;

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
public class SimClockAppStateTest {

    @BeforeClass
    public static void setUp() {
        Logging.require();
    }

    @Test(timeout = 60*1000)
    public void testStep() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        SimClock clock = app.getStateManager().getState(SimClockAppState.class).getSimClock();
        
        // clock updates every frame
        long t0 = clock.secondTime()*1000+clock.getMillis();
        app.waitFor();
        long t1 = clock.secondTime()*1000+clock.getMillis();
        assertTrue(t1 > t0);
        
        app.stop();
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
//            stateManager.attach(new CameraAppState(1000f));
//            stateManager.attach(new WeatherAppState("clear"));
//            stateManager.attach(new SkyAppState(10000f, mission));
//            stateManager.attach(new AerialAppState());
        }
    }
}
