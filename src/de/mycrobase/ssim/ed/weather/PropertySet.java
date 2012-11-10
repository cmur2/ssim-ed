package de.mycrobase.ssim.ed.weather;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Encloses an {@link Iterable} set of properties (stored as {@link Entry})
 * each comprised of an identifying name, a value and a {@link Class}
 * representing the value's type.
 * 
 * @author cn
 */
public class PropertySet implements Iterable<PropertySet.Entry> {
    
    private String name;
    private Map<String, Entry> entries = new HashMap<String, Entry>();
    
    public PropertySet(String name) {
        super();
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Registers a new property under the given key with the given initial
     * value of given type. Needs to be called once before being able to
     * set/get the newly created property.
     * 
     * @param key the key, e.g. "ocean.temperature"
     * @param value the initial value
     * @param clazz the type of the value
     */
    public void put(String key, Object value, Class<?> clazz) {
        entries.put(key, new Entry(key, value, clazz));
    }
    
    /**
     * Retrieves the raw value of a given property.
     * 
     * @param key used to find the value
     * @return the value or {@code null} when key does not exists
     */
    public Object get(String key) {
        Entry e = entries.get(key);
        if(e == null) {
            return null;
        }
        return e.getValue();
    }

    /**
     * Retrieves the casted value of a given property.
     * 
     * @param key used to find the value
     * @param clazz expected type
     * @return the casted value or {@code null} when key does not exists
     */
    @SuppressWarnings("unchecked")
    public <T> T getAs(String key, Class<T> clazz) {
        Entry e = entries.get(key);
        if(e == null) {
            return null;
        }
        if(e.clazz != clazz) {
            throw new IllegalArgumentException(String.format(
                "Property %s is not a %s but a %s!", key, clazz.getSimpleName(), e.clazz.getSimpleName()));
        }
        return (T) e.getValue();
    }
    
    /**
     * Retrieves the type of a given property.
     * 
     * @param key used to find the value
     * @return the class or {@code null} when key does not exists
     */
    @SuppressWarnings("rawtypes")
    public Class getClassOf(String key) {
        Entry e = entries.get(key);
        if(e == null) {
            return null;
        }
        return e.getClazz();
    }
    
    /**
     * Sets the raw value of a given property (if exists) to the given data.
     * 
     * @param key used to find the value
     * @param value new value
     */
    public void set(String key, Object value) {
        Entry e = entries.get(key);
        if(e != null) {
            e.setValue(value);
        }
    }
    
    @Override
    public Iterator<Entry> iterator() {
        return entries.values().iterator();
    }
    
    public static class Entry {

        private String key;
        private Object value;
        private Class<?> clazz;
        
        public Entry(String key, Object value, Class<?> clazz) {
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

        public Class<?> getClazz() {
            return clazz;
        }
    }
}
