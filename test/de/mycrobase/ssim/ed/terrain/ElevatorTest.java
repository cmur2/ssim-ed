package de.mycrobase.ssim.ed.terrain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jme3.asset.AssetKey;
import com.jme3.system.JmeContext.Type;

import de.mycrobase.ssim.ed.helper.Logging;
import de.mycrobase.ssim.ed.helper.SteppedSSimApplication;
import de.mycrobase.ssim.ed.helper.categories.Fast;
import de.mycrobase.ssim.ed.util.MapLoader;
import de.mycrobase.ssim.ed.util.PropertiesLoader;
import de.mycrobase.ssim.ed.util.XMLLoader;

@Category(Fast.class)
public class ElevatorTest {

    @BeforeClass
    public static void setUp() {
        Logging.require();
    }

    @Test
    public void testDefault() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        AssetKey<BinaryMap> mapKey = new AssetKey<BinaryMap>("maps/Te10d100.map");
        BinaryMap map = app.getAssetManager().loadAsset(mapKey);
        
        final float defElev = -999f;
        Elevator e = new Elevator(map, defElev);
        
        assertEquals(defElev, e.getElevation(-1, 0), 1e-4f);
        assertEquals(defElev, e.getElevation( 0,-1), 1e-4f);
        assertEquals(defElev, e.getElevation(-1,-1), 1e-4f);
        assertEquals(defElev, e.getElevation( 3, 0), 1e-4f);
        assertEquals(defElev, e.getElevation( 0, 3), 1e-4f);
        assertEquals(defElev, e.getElevation( 3, 3), 1e-4f);
        assertEquals(defElev, e.getElevation(-1, 3), 1e-4f);
        assertEquals(defElev, e.getElevation( 3,-1), 1e-4f);
        
        assertEquals(defElev, e.getElevation(-100f,   0f), 1e-4f);
        assertEquals(defElev, e.getElevation(   0f,-100f), 1e-4f);
    }

    @Test
    public void testInterpolation() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        AssetKey<BinaryMap> mapKey = new AssetKey<BinaryMap>("maps/Te10d100.map");
        BinaryMap map = app.getAssetManager().loadAsset(mapKey);
        Elevator e = new Elevator(map, 0);
        
        // hill
        assertEquals(10f, e.getElevation(50f,50f), 1e-4f);
        // slope
        assertTrue(e.getElevation(-50f,50f) < 10f);
        assertTrue(e.getElevation(-50f,50f) > 0f);
        // floor
        assertEquals(0f, e.getElevation(-150f,50f), 1e-4f);
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
