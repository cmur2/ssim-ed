package de.mycrobase.ssim.ed.weather;

import java.util.HashMap;
import java.util.Map;

import com.jme3.math.Vector3f;


public abstract class BasicWeatherController implements WeatherController {
    
    // current state from which property queries are answered
    protected PropertySet state = new PropertySet("state");
    
    // interpolators for either whole classes of entries or (overriding)
    // a specific entry
    private Map<Class<?>, WeatherInterpolator> classInterpolators =
        new HashMap<Class<?>, WeatherInterpolator>();
    private Map<String, WeatherInterpolator> entryInterpolators =
        new HashMap<String, WeatherInterpolator>();

    public BasicWeatherController() {
    }
    
    // query interface
    
    /** {@inheritDoc} */
    @Override
    public Float getFloat(String key) {
        return state.getAs(key, Float.class);
    }
    
    /** {@inheritDoc} */
    @Override
    public Vector3f getVec3(String key) {
        return state.getAs(key, Vector3f.class);
    }
    
    /** {@inheritDoc} */
    @Override
    public Integer getInt(String key) {
        return state.getAs(key, Integer.class);
    }
    
    /** {@inheritDoc} */
    @Override
    public Integer[] getIntArray(String key) {
        return state.getAs(key, Integer[].class);
    }
    
    /** {@inheritDoc} */
    @Override
    public Boolean getBool(String key) {
        return state.getAs(key, Boolean.class);
    }
    
    /** {@inheritDoc} */
    @Override
    public <T> void registerInterpolator(WeatherInterpolator interpolator,
            Class<T> clazz) {
        classInterpolators.put(clazz, interpolator);
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerInterpolator(WeatherInterpolator interpolator,
            String key) {
        entryInterpolators.put(key, interpolator);
    }
    
    /**
     * Retrieves the interpolator for a given property. This method respects
     * the {@link BasicWeatherController#entryInterpolators} before querying
     * the {@link BasicWeatherController#classInterpolators}.
     * 
     * @param key used to find the value
     * @return a responsible {@link WeatherInterpolator} or {@code null}
     */
    protected WeatherInterpolator getInterpolator(String key) {
        if(entryInterpolators.containsKey(key)) {
            return entryInterpolators.get(key);
        }
        return classInterpolators.get(state.getClassOf(key));
    }
}
