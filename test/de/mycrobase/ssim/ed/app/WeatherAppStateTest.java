package de.mycrobase.ssim.ed.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext.Type;

import de.mycrobase.ssim.ed.helper.Logging;
import de.mycrobase.ssim.ed.helper.SteppedSSimApplication;
import de.mycrobase.ssim.ed.helper.categories.Slow;
import de.mycrobase.ssim.ed.mission.Mission;
import de.mycrobase.ssim.ed.mission.MissionParser;
import de.mycrobase.ssim.ed.util.MapLoader;
import de.mycrobase.ssim.ed.util.PropertiesLoader;
import de.mycrobase.ssim.ed.util.XMLLoader;
import de.mycrobase.ssim.ed.weather.Weather;

@Category(Slow.class)
public class WeatherAppStateTest {

    @BeforeClass
    public static void setUp() {
        Logging.require();
    }
    
    @Test(timeout = 60*1000)
    public void testSingleWeather() {
        HelperSingle app = new HelperSingle();
        app.start(Type.Headless);
        app.waitFor();
        
        Weather w = app.getStateManager().getState(WeatherAppState.class).getWeather();
        assertEquals(2f, (float) w.getFloat("air.turbidity"), 1e-4f);
        assertEquals(new Vector3f(.3f, .3f, .3f), w.getVec3("sky.light"));
        assertEquals(true, (boolean) w.getBool("sun.lensflare.enabled"));
        assertEquals(0, (int) w.getInt("precipitation.form"));
        assertNull(w.getBool("not.found"));
        
        app.stop();
    }
    
    @Ignore
    @Test(timeout = 60*1000)
    public void testMultiWeather() {
        // TODO: need to control WeatherController
    }

    private static class HelperSingle extends SteppedSSimApplication {
        
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
            stateManager.attach(new WeatherAppState("test"));
//            stateManager.attach(new SkyAppState(10000f, mission));
//            stateManager.attach(new AerialAppState());
        }
    }
}
