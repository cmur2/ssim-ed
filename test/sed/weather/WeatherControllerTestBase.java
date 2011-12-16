package sed.weather;

import static org.junit.Assert.*;

import org.junit.Test;

import com.jme3.math.Vector3f;

public abstract class WeatherControllerTestBase {
    
    private static final float weakEps = 1e-4f;
    
    public WeatherControllerTestBase() {
    }
    
    /**
     * @return a new {@link WeatherController}
     */
    protected abstract WeatherController create(PropertySet init);
    
    @Test
    public void testRegisterProp() {
        PropertySet ps = new PropertySet("test");
        ps.put("ocean.temp", 20.5f, Float.class);
        ps.put("wind", new Vector3f(1,0,0), Vector3f.class);
        ps.put("cloud.cover", 50, Integer.class);
        ps.put("sky.turbidity", new Integer[] {2,3,6}, Integer[].class);
        ps.put("sun.lensflare", true, Boolean.class);
        WeatherController wc = create(ps);
        assertTrue(true);
    }
    
    @Test
    public void testFetchProp() {
        PropertySet ps = new PropertySet("test");
        ps.put("ocean.temp", 20.5f, Float.class);
        ps.put("wind", new Vector3f(1,0,0), Vector3f.class);
        WeatherController wc = create(ps);
        assertTrue(20.5f == wc.getFloat("ocean.temp"));
        assertTrue(wc.getVec3("wind").distance(new Vector3f(1,0,0)) < weakEps);
    }
    
    @Test
    public void testFetchMissingProp() {
        WeatherController wc = create(new PropertySet("test"));
        assertNull(wc.getFloat("foo"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testFetchWrongProp() {
        PropertySet ps = new PropertySet("test");
        ps.put("ocean.temp", 20.5f, Float.class);
        WeatherController wc = create(ps);
        wc.getInt("ocean.temp");
    }
}
