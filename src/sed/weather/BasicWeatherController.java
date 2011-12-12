package sed.weather;

import java.util.HashMap;
import java.util.Map;

import com.jme3.math.Vector3f;

public abstract class BasicWeatherController implements ChangeableWeather,
        WeatherController {
    
    // TODO: TriggerWeatherController -> trigger subsystem
    
    private Map<String, Entry> entries = new HashMap<String, Entry>();
    private Map<Class, WeatherInterpolator> classInterpolators =
        new HashMap<Class, WeatherInterpolator>();
    private Map<String, WeatherInterpolator> entryInterpolators =
        new HashMap<String, WeatherInterpolator>();

    public BasicWeatherController() {
    }
    
    /** {@inheritDoc} */
    @Override
    public Float getFloat(String key) {
        return getProp(key, Float.class);
    }
    
    /** {@inheritDoc} */
    @Override
    public Vector3f getVec3(String key) {
        return getProp(key, Vector3f.class);
    }
    
    /** {@inheritDoc} */
    @Override
    public Integer getInt(String key) {
        return getProp(key, Integer.class);
    }
    
    /** {@inheritDoc} */
    @Override
    public Integer[] getIntArray(String key) {
        return getProp(key, Integer[].class);
    }
    
    /** {@inheritDoc} */
    @Override
    public Boolean getBool(String key) {
        return getProp(key, Boolean.class);
    }
    
    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    private <T> T getProp(String key, Class<T> clazz) {
        Entry e = entries.get(key);
        if(e == null) {
            return null;
        }
        if(e.clazz != clazz) {
            throw new IllegalArgumentException(String.format(
                    "Property %s is not a %s!", key, clazz.getSimpleName()));
        }
        return (T) e.getValue();
    }
    
    /** {@inheritDoc} */
    @Override
    public void setProperty(String key, Object value) {
        Entry e = entries.get(key);
        if(e == null) {
            throw new UnsupportedOperationException(String.format(
                    "Property %s does not exist!", key));
        }
        e.setValue(value);
    }
    
    /** {@inheritDoc} */
    @Override
    public <T> void registerProperty(String key, T value, Class<T> clazz) {
        entries.put(key, new Entry(key, value, clazz));
    }
    
    /** {@inheritDoc} */
    @Override
    public <T> void registerInterpolator(WeatherInterpolator<T> interpolator,
            Class<T> clazz) {
        classInterpolators.put(clazz, interpolator);
    }
    
    /** {@inheritDoc} */
    @Override
    public <T> void registerInterpolator(WeatherInterpolator<T> interpolator,
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
        return classInterpolators.get(entries.get(key).getClazz());
    }
    
    @SuppressWarnings("rawtypes")
    static class Entry {
        private String key;
        private Object value;
        private Class clazz;
        
        public Entry(String key, Object value, Class clazz) {
            this.key = key;
            this.value = value;
            this.clazz = clazz;
        }

        public String getKey() {
            return key;
        }
        
        public Object getValue() {
            return value;
        }
        
        public void setValue(Object value) {
            this.value = value;
        }
        
        public Class getClazz() {
            return clazz;
        }
    }
}