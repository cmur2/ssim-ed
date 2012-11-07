package de.mycrobase.ssim.ed.weather.ext;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.jme3.math.Vector3f;
import com.jme3.system.JmeContext.Type;

import de.mycrobase.ssim.ed.helper.InMemoryLocator;
import de.mycrobase.ssim.ed.helper.Logging;
import de.mycrobase.ssim.ed.helper.SteppedSSimApplication;
import de.mycrobase.ssim.ed.helper.categories.Fast;
import de.mycrobase.ssim.ed.util.MapLoader;
import de.mycrobase.ssim.ed.util.PropertiesLoader;
import de.mycrobase.ssim.ed.util.XMLLoader;
import de.mycrobase.ssim.ed.weather.PropertySet;

@Category(Fast.class)
public class XMLPropertySetBuilderTest {

    @BeforeClass
    public static void setUp() {
        Logging.require();
    }
    
    @Test
    public void testResults() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        registerWeatherXML("testResults1", "");
        registerWeatherXML("testResults2", "");
        
        XMLPropertySetBuilder builder = new XMLPropertySetBuilder(app.getAssetManager());
        assertNull(builder.getResult());
        assertEquals(0, builder.getResults().length);
        
        XMLPropertySetBuilder builder1 = new XMLPropertySetBuilder(app.getAssetManager(), "testResults1");
        assertNotNull(builder1.getResult());
        assertNotNull(builder1.getResult(0));
        assertEquals(1, builder1.getResults().length);
        
        XMLPropertySetBuilder builder12 = new XMLPropertySetBuilder(app.getAssetManager(), "testResults1", "testResults2");
        assertNotNull(builder12.getResult(0));
        assertNotNull(builder12.getResult(1));
        assertEquals(2, builder12.getResults().length);
    }
    
    @Test
    public void testLoad() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        registerWeatherXML("testLoad",
            "<i>42</i>" +
            "<f>23.0</f>" +
            "<v>(1.0,2.0,3.0)</v>" +
            "<b>true</b>" +
            "<ia>[100,101,102]</ia>"
        );
        XMLPropertySetBuilder builder = new XMLPropertySetBuilder(app.getAssetManager(), "testLoad");
        builder.putInt("i");
        builder.putFloat("f");
        builder.putVec3("v");
        builder.putBool("b");
        builder.putIntArray("ia");
        PropertySet set = builder.getResults()[0];
        
        assertEquals(42, (int) (Integer) set.get("i"));
        assertEquals(23.0f, (float) (Float) set.get("f"), 1e-4f);
        assertEquals(new Vector3f(1,2,3), set.get("v"));
        assertTrue((Boolean)set.get("b"));
        assertEquals(101, (int) ((Integer[]) set.get("ia"))[1]);
        assertNull(set.get("not.found"));
    }

    @Test(expected = XMLPropertySetBuilder.ParseException.class)
    public void testFloatParse() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        registerWeatherXML("testFloatParse", "");
        XMLPropertySetBuilder builder = new XMLPropertySetBuilder(app.getAssetManager(), "testFloatParse");
        builder.putFloat("not.found");
    }

    @Test(expected = XMLPropertySetBuilder.ParseException.class)
    public void testVec3Parse() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        registerWeatherXML("testVec3Parse", "");
        XMLPropertySetBuilder builder = new XMLPropertySetBuilder(app.getAssetManager(), "testVec3Parse");
        builder.putFloat("not.found");
    }

    @Test(expected = XMLPropertySetBuilder.ParseException.class)
    public void testIntParse() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        registerWeatherXML("testIntParse", "");
        XMLPropertySetBuilder builder = new XMLPropertySetBuilder(app.getAssetManager(), "testIntParse");
        builder.putFloat("not.found");
    }

    @Test(expected = XMLPropertySetBuilder.ParseException.class)
    public void testIntArrayParse() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        registerWeatherXML("testIntArrayParse", "");
        XMLPropertySetBuilder builder = new XMLPropertySetBuilder(app.getAssetManager(), "testIntArrayParse");
        builder.putFloat("not.found");
    }

    @Test(expected = XMLPropertySetBuilder.ParseException.class)
    public void testBoolParse() {
        Helper app = new Helper();
        app.start(Type.Headless);
        app.waitFor();
        
        registerWeatherXML("testBoolParse", "");
        XMLPropertySetBuilder builder = new XMLPropertySetBuilder(app.getAssetManager(), "testBoolParse");
        builder.putFloat("not.found");
    }
    
    private static void registerWeatherXML(String name, String body) {
        InMemoryLocator.registerFile(
            String.format("/weather/%s.xml", name),
            String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<sed:weather id=\"%s\" xmlns:sed=\"http://mycrobase.de/sed-weather-1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://mycrobase.de/sed-weather-1.0 sed-weather-1.0.xsd\">" +
                    "%s" +
                    "</sed:weather>", name, body));
    }

    private static class Helper extends SteppedSSimApplication {
        
        @Override
        public void simpleInitApp() {
            assetManager.registerLoader(XMLLoader.class, "xml");
            assetManager.registerLoader(MapLoader.class, "map");
            assetManager.registerLoader(PropertiesLoader.class, "properties");
            
            assetManager.unregisterLocator("/", com.jme3.asset.plugins.ClasspathLocator.class);
            assetManager.registerLocator("/", InMemoryLocator.class);
        }
    }
}
