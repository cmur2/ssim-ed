package sed.weather;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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