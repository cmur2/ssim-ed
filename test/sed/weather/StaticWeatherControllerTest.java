package sed.weather;

import static org.junit.Assert.*;

import org.junit.Test;

import com.jme3.math.Vector3f;

public class StaticWeatherControllerTest {
    
    private static final float weakEps = 1e-4f;
    
    public StaticWeatherControllerTest() {
    }
    
    @Test
    public void testRegisterProp() {
        WeatherController wc = new StaticWeatherController();
        wc.registerProperty("ocean.temp", 20.5f, Float.class);
        wc.registerProperty("wind", new Vector3f(1,0,0), Vector3f.class);
        wc.registerProperty("cloud.cover", 50, Integer.class);
        wc.registerProperty("sky.turbidity", new Integer[] {2,3,6}, Integer[].class);
        wc.registerProperty("sun.lensflare", true, Boolean.class);
        assertTrue(true);
    }
    
    @Test
    public void testFetchProp() {
        WeatherController wc = new StaticWeatherController();
        wc.registerProperty("ocean.temp", 20.5f, Float.class);
        wc.registerProperty("wind", new Vector3f(1,0,0), Vector3f.class);
        assertTrue(20.5f == wc.getFloat("ocean.temp"));
        assertTrue(wc.getVec3("wind").distance(new Vector3f(1,0,0)) < weakEps);
    }
    
    @Test
    public void testFetchMissingProp() {
        WeatherController wc = new StaticWeatherController();
        assertNull(wc.getFloat("foo"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testFetchWrongProp() {
        WeatherController wc = new StaticWeatherController();
        wc.registerProperty("ocean.temp", 20.5f, Float.class);
        wc.getInt("ocean.temp");
    }
    
    @Test
    public void testSetProp() {
        WeatherController wc = new StaticWeatherController();
        wc.registerProperty("ocean.temp", 20.5f, Float.class);
        wc.setProperty("ocean.temp", 21.0f);
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void testSetMissingProp() {
        WeatherController wc = new StaticWeatherController();
        wc.setProperty("foo", "hehe");
    }
}
