package de.mycrobase.ssim.ed.weather.ext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Element;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;

import de.mycrobase.ssim.ed.weather.PropertySet;

/**
 * Builder for a {@link PropertySet} that accept it's specification via
 * {@link XMLPropertySetBuilder#putFloat(String)} etc and reads the concrete values
 * from (optionally multiple) asset XML file(s) given by file name.
 * 
 * @author cn
 */
public class XMLPropertySetBuilder {
    
    private static final Pattern patVec3 = Pattern.compile("^\\((.+),(.+),(.+)\\)$");
    private static final Pattern patIntArray = Pattern.compile("^\\[(.*)\\]$");
    
    private Element[] weatherXml;
    private PropertySet[] result;
    
    public XMLPropertySetBuilder(AssetManager mgr, String... names) {
        weatherXml = new Element[names.length];
        result = new PropertySet[names.length];
        for(int i = 0; i < names.length; i++) {
            weatherXml[i] = mgr.loadAsset(new AssetKey<Element>(String.format("weather/%s.xml", names[i])));
            String name = weatherXml[i].getAttribute("id").getValue();
            result[i] = new PropertySet(name);
        }
    }
    
    public void putFloat(String key) {
        for(int i = 0; i < weatherXml.length; i++) {
            String data = getXMLText(i, key).trim();
            try {
                result[i].put(key, (Float) Float.valueOf(data), Float.class);
            } catch(NumberFormatException ex) {
                throw new ParseException(String.format(
                    "Property %s in %s: parsing failed", key, result[i].getName()), ex); 
            }
        }
    }
    
    public void putVec3(String key) {
        for(int i = 0; i < weatherXml.length; i++) {
            String data = getXMLText(i, key).trim();
            Matcher m = patVec3.matcher(data);
            if(m.matches()) {
                result[i].put(key, new Vector3f(
                    Float.parseFloat(m.group(1).trim()),
                    Float.parseFloat(m.group(2).trim()),
                    Float.parseFloat(m.group(3).trim())), Vector3f.class);
            } else {
                throw new ParseException(String.format(
                    "Property %s in %s: not a Vector3f", key, result[i].getName()));
            }
        }
    }
    
    public void putInt(String key) {
        for(int i = 0; i < weatherXml.length; i++) {
            String data = getXMLText(i, key).trim();
            try {
                result[i].put(key, (Integer) Integer.valueOf(data, 10), Integer.class);
            } catch(NumberFormatException ex) {
                throw new ParseException(String.format(
                    "Property %s in %s: parsing failed", key, result[i].getName()), ex);
            }
        }
    }
    
    public void putIntArray(String key) {
        for(int i = 0; i < weatherXml.length; i++) {
            String data = getXMLText(i, key).trim();
            Matcher m = patIntArray.matcher(data);
            if(m.matches()) {
                String[] array = m.group(1).split(",");
                Integer[] intArray = new Integer[array.length];
                for(int j = 0; j < array.length; j++) {
                    intArray[j] = Integer.parseInt(array[j].trim(), 10);
                }
                result[i].put(key, intArray, Integer[].class);
            } else {
                throw new ParseException(String.format(
                    "Property %s in %s: not an Integer[]", key, result[i].getName()));
            }
        }
    }
    
    public void putBool(String key) {
        for(int i = 0; i < weatherXml.length; i++) {
            String data = getXMLText(i, key).trim();
            try {
                result[i].put(key, (Boolean) Boolean.valueOf(data), Boolean.class);
            } catch(NumberFormatException ex) {
                throw new ParseException(String.format(
                    "Property %s in %s: parsing failed", key, result[i].getName()), ex); 
            }
        }
    }
    
    public PropertySet getResult() {
        return result.length == 0 ? null : result[0];
    }
    
    public PropertySet getResult(int idx) {
        return result[idx];
    }
    
    public PropertySet[] getResults() {
        return result;
    }
    
    private String getXMLText(int idx, String key) {
        Element cur = weatherXml[idx];
        String[] parts = key.split("\\.");
        for(int i = 0; i < parts.length; i++) {
//            System.out.println(childreen[i]);
            cur = cur.getChild(parts[i]);
            if(cur == null) {
                throw new ParseException(String.format(
                    "Property %s in %s: no XML data not found!", key, result[i].getName()));
            }
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
