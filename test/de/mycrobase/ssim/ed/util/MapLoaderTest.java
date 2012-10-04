package de.mycrobase.ssim.ed.util;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jme3.asset.AssetKey;
import com.jme3.system.JmeContext.Type;

import de.mycrobase.ssim.ed.helper.Logging;
import de.mycrobase.ssim.ed.helper.SteppedSSimApplication;
import de.mycrobase.ssim.ed.helper.categories.Fast;
import de.mycrobase.ssim.ed.terrain.BinaryMap;

@Category(Fast.class)
public class MapLoaderTest {

    @BeforeClass
    public static void setUp() {
        Logging.require();
    }
    
    @Test
    public void testLoad() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        AssetKey<BinaryMap> mapKey = new AssetKey<BinaryMap>("maps/Te10d100.map");
        BinaryMap map = app.getAssetManager().loadAsset(mapKey);
        
        float e = 10f; // elevation
        float d = 100f; // distance
        assertEquals("Te10d100", map.name);
        assertEquals(d, map.nsDiff, 1e-4f);
        assertEquals(d, map.weDiff, 1e-4f);
        // check dimensions
        assertEquals(2, map.nsNum);
        assertEquals(2, map.weNum);
        assertEquals(2, map.elevs.length);
        assertEquals(2, map.elevs[0].length);
        // check contents
        assertEquals(e, map.elevs[0][0], 1e-4f);
        assertEquals(e, map.elevs[0][1], 1e-4f);
        assertEquals(e, map.elevs[1][0], 1e-4f);
        assertEquals(e, map.elevs[1][1], 1e-4f);
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
