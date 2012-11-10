package de.mycrobase.ssim.ed.weather;

import static org.junit.Assert.*;

import org.junit.Test;

import com.jme3.math.Vector3f;

import de.mycrobase.ssim.ed.weather.ext.EnumWeatherProperty;

public abstract class WeatherControllerTestBase {
    
    private static final float weakEps = 1e-4f;
    
    public WeatherControllerTestBase() {
    }
    
    /**
     * @return a new {@link WeatherController}
     */
    protected abstract WeatherController create(String weather, WeatherProperty[] properties);
    
    @Test
    public void testRegisterProp() {
        EnumWeatherProperty[] ps = new EnumWeatherProperty[5]; 
        ps[0] = new EnumWeatherProperty("ocean.temp", Float.class);
        ps[0].put("test", 20.5f);
        ps[1] = new EnumWeatherProperty("wind", Vector3f.class);
        ps[1].put("test", new Vector3f(1,0,0));
        ps[2] = new EnumWeatherProperty("cloud.cover", Integer.class);
        ps[2].put("test", 50);
        ps[3] = new EnumWeatherProperty("sky.turbidity", Integer[].class);
        ps[3].put("test", new Integer[] {2,3,6});
        ps[4] = new EnumWeatherProperty("sun.lensflare", Boolean.class);
        ps[4].put("test", true);
        WeatherController wc = create("test", ps);
        assertTrue(true);
    }
    
    @Test
    public void testFetchProp() {
        EnumWeatherProperty[] ps = new EnumWeatherProperty[2]; 
        ps[0] = new EnumWeatherProperty("ocean.temp", Float.class);
        ps[0].put("test", 20.5f);
        ps[1] = new EnumWeatherProperty("wind", Vector3f.class);
        ps[1].put("test", new Vector3f(1,0,0));
        WeatherController wc = create("test", ps);
        assertTrue(20.5f == wc.getFloat("ocean.temp"));
        assertTrue(wc.getVec3("wind").distance(new Vector3f(1,0,0)) < weakEps);
    }
    
    @Test
    public void testFetchMissingProp() {
        WeatherController wc = create("test", new WeatherProperty[0]);
        assertNull(wc.getFloat("foo"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testFetchWrongProp() {
        EnumWeatherProperty[] ps = new EnumWeatherProperty[1]; 
        ps[0] = new EnumWeatherProperty("ocean.temp", Float.class);
        ps[0].put("test", 20.5f);
        WeatherController wc = create("test", ps);
        wc.getInt("ocean.temp");
    }
}
