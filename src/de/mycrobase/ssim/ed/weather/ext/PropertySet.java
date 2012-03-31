package de.mycrobase.ssim.ed.weather.ext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Encloses an {@link Iterable} set of properties (stored as {@link Entry})
 * each comprised of an identifying name, a value and a {@link Class}
 * representing the value's type. PropertySets are read-only.
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
    
    public <T> void put(String key, T value, Class<T> clazz) {
        entries.put(key, new Entry(key, value, clazz));
    }
    
    public Object get(String key) {
        Entry e = entries.get(key);
        if(e == null) {
            return null;
        }
        return e.getValue();
    }
    
    @Override
    public Iterator<Entry> iterator() {
        return entries.values().iterator();
    }
    
    @SuppressWarnings("rawtypes")
    public static class Entry {

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

        public Class getClazz() {
            return clazz;
        }
    }
}
