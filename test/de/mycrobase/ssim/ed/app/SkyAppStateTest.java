package de.mycrobase.ssim.ed.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
import de.mycrobase.ssim.ed.sky.SkyGradient;
import de.mycrobase.ssim.ed.util.MapLoader;
import de.mycrobase.ssim.ed.util.PropertiesLoader;
import de.mycrobase.ssim.ed.util.XMLLoader;

@Category(Slow.class)
public class SkyAppStateTest {

    @BeforeClass
    public static void setUp() {
        Logging.require();
    }
    
    @Test(timeout = 60*1000)
    public void testCleanup() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        assertNotNull(app.getStateManager().getState(SkyAppState.class));
        assertNotNull(app.getRootNode().getChild("SkyNode"));
        
        app.getStateManager().detach(app.getStateManager().getState(SkyAppState.class));
        app.waitFor();
        
        assertNull(app.getStateManager().getState(SkyAppState.class));
        assertNull(app.getRootNode().getChild("SkyNode"));
        
        app.stop();
    }
    
    @Test(timeout = 60*1000)
    public void testAPI() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        SkyAppState state = app.getStateManager().getState(SkyAppState.class);
        
        assertEquals(app.getRootNode().getChild("SkyNode"), state.getSkyNode());
        assertNotNull(state.getSun());
        assertNotNull(state.getSkyGradient());
        assertEquals(10000f, state.getHemisphereRadius(), 1e-4f);
        assertEquals(SkyGradient.NightThetaMax, state.getNightThetaMax(), 1e-4f);
        assertEquals(SkyGradient.NightSunColor, state.getNightSunColor());
        
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
