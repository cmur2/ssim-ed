package sed.weather;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Element;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;

public class PropertySetBuilder {
    
    // TODO: how to test jME dependent classes?
    
    private static final Pattern patVec3 = Pattern.compile("^\\((.+), (.+), (.+)\\)$");
    private static final Pattern patIntArray = Pattern.compile("^\\[(.*)\\]$");
    
    private Element weatherXml;
    private PropertySet result;
    
    public PropertySetBuilder(AssetManager mgr, String name) {
        weatherXml = mgr.loadAsset(new AssetKey<Element>(String.format("weather/%s.xml", name)));
        result = new PropertySet(name);
    }
    
    public void put(String key, Class<?> clazz) {
        String data = getText(key);
        if(data == null) {
            throw new ParseException(String.format("Property %s: no XML data not found!", key));
        }
        boolean success;
        try {
            success = parse(key, data, clazz);
        } catch(Exception ex) {
            throw new ParseException(String.format("Property %s: parsing failed", key), ex); 
        }
        if(!success) {
            throw new ParseException(String.format(
                "Property %s: no method known for data of type %s!", key,
                clazz.getSimpleName()));
        }
    }
    
    public PropertySet getResult() {
        return result;
    }
    
    private boolean parse(String key, String data, Class<?> clazz) {
        boolean found = true;
        if(clazz == Float.class) {
            result.put(key, (Float) Float.valueOf(data), Float.class);
        } else if(clazz == Vector3f.class) {
            Matcher m = patVec3.matcher(data);
            if(m.matches()) {
                result.put(key, new Vector3f(
                    Float.parseFloat(m.group(1)),
                    Float.parseFloat(m.group(2)),
                    Float.parseFloat(m.group(3))), Vector3f.class);
            } else {
                throw new NumberFormatException("Not a Vector3f: "+data);
            }
        } else if(clazz == Integer.class) {
            result.put(key, (Integer) Integer.valueOf(data, 10), Integer.class);
        } else if(clazz == Integer[].class) {
            Matcher m = patIntArray.matcher(data);
            if(m.matches()) {
                String[] array = m.group(1).split(", ");
                Integer[] intArray = new Integer[array.length];
                for(int i = 0; i < array.length; i++) {
                    intArray[i] = Integer.parseInt(array[i].trim(), 10);
                }
                result.put(key, intArray, Integer[].class);
            } else {
                throw new NumberFormatException("Not an Integer[]: "+data);
            }
        } else if(clazz == Boolean.class) {
            result.put(key, (Boolean) Boolean.valueOf(data), Boolean.class);
        } else {
            found = false;
        }
        return found;
    }
    
    private String getText(String key) {
        Element cur = weatherXml;
        String[] parts = key.split("\\.");
        for(int i = 0; i < parts.length; i++) {
//            System.out.println(childreen[i]);
            cur = cur.getChild(parts[i]);
            if(cur == null) return null;
        }
        return cur.getText();
    }
    
    public static class ParseException extends RuntimeException {

        public ParseException() {
            super();
        }

        public ParseException(String message, Throwable cause) {
            super(message, cause);
        }

        public ParseException(String message) {
            super(message);
        }

        public ParseException(Throwable cause) {
            super(cause);
        }
    }
}
