package de.mycrobase.ssim.ed.weather.ext;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom.Element;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;

import de.mycrobase.ssim.ed.weather.PropertySet;
import de.mycrobase.ssim.ed.weather.WeatherProperty;
import de.mycrobase.ssim.ed.weather.WeatherPropertyGenerator;

/**
 * Builder for a {@link PropertySet} that accept it's specification via
 * {@link XMLPropertySetBuilder#putFloat(String)} etc and reads the concrete values
 * from (optionally multiple) asset XML file(s) given by file name.
 * 
 * @author cn
 */
public class XMLPropertySetBuilder {
    
    private static final Logger logger = Logger.getLogger(XMLPropertySetBuilder.class);
    
    private static final Pattern patVec3 = Pattern.compile("^\\((.+),(.+),(.+)\\)$");
    private static final Pattern patIntArray = Pattern.compile("^\\[(.*)\\]$");
    
    private Element[] weatherXml;
    private String[] weatherNames;
    private ArrayList<WeatherProperty> properties;
    
    public XMLPropertySetBuilder(AssetManager mgr, String... setNames) {
        weatherXml = new Element[setNames.length];
        weatherNames = new String[setNames.length];
        properties = new ArrayList<WeatherProperty>();
        for(int i = 0; i < setNames.length; i++) {
            weatherXml[i] = mgr.loadAsset(new AssetKey<Element>(String.format("weather/%s.xml", setNames[i])));
            weatherNames[i] = weatherXml[i].getAttribute("id").getValue();
            
            // file should be named like included weather
            if(!setNames[i].equals(weatherNames[i])) {
                logger.warn(String.format(
                    "Filename and weather name does not match: weather/%s.xml -> %s",
                    setNames[i], weatherNames[i]));
            }
        }
    }
    
    public void put(String key, Class<?> type, WeatherPropertyGenerator gen) {
        GeneratedWeatherProperty p = new GeneratedWeatherProperty(key, type, gen);
        put(p, key, type);
        properties.add(p);
    }
    
    public void put(String key, Class<?> type) {
        EnumWeatherProperty p = new EnumWeatherProperty(key, type);
        put(p, key, type);
        properties.add(p);
    }
    
    private void put(EnumWeatherProperty p, String key, Class<?> type) {
        if(type == Float.class) {
            putFloat(p, key);
        } else if(type == Vector3f.class) {
            putVec3(p, key);
        } else if(type == Integer.class) {
            putInt(p, key);
        } else if(type == Integer[].class) {
            putIntArray(p, key);
        } else if(type == Boolean.class) {
            putBool(p, key);
        }
    }
    
    private void putFloat(EnumWeatherProperty p, String key) {
        for(int i = 0; i < weatherXml.length; i++) {
            String data = getXMLText(i, key).trim();
            try {
                p.put(weatherNames[i], Float.valueOf(data));
            } catch(NumberFormatException ex) {
                throw new ParseException(String.format(
                    "Property %s in %s: parsing failed", key, weatherNames[i]), ex); 
            }
        }
    }
    
    private void putVec3(EnumWeatherProperty p, String key) {
        for(int i = 0; i < weatherXml.length; i++) {
            String data = getXMLText(i, key).trim();
            Matcher m = patVec3.matcher(data);
            if(m.matches()) {
                p.put(weatherNames[i], new Vector3f(
                    Float.parseFloat(m.group(1).trim()),
                    Float.parseFloat(m.group(2).trim()),
                    Float.parseFloat(m.group(3).trim())));
            } else {
                throw new ParseException(String.format(
                    "Property %s in %s: not a Vector3f", key, weatherNames[i]));
            }
        }
    }
    
    private void putInt(EnumWeatherProperty p, String key) {
        for(int i = 0; i < weatherXml.length; i++) {
            String data = getXMLText(i, key).trim();
            try {
                p.put(weatherNames[i], Integer.valueOf(data, 10));
            } catch(NumberFormatException ex) {
                throw new ParseException(String.format(
                    "Property %s in %s: parsing failed", key, weatherNames[i]), ex);
            }
        }
    }
    
    private void putIntArray(EnumWeatherProperty p, String key) {
        for(int i = 0; i < weatherXml.length; i++) {
            String data = getXMLText(i, key).trim();
            Matcher m = patIntArray.matcher(data);
            if(m.matches()) {
                String[] array = m.group(1).split(",");
                Integer[] intArray = new Integer[array.length];
                for(int j = 0; j < array.length; j++) {
                    intArray[j] = Integer.parseInt(array[j].trim(), 10);
                }
                p.put(weatherNames[i], intArray);
            } else {
                throw new ParseException(String.format(
                    "Property %s in %s: not an Integer[]", key, weatherNames[i]));
            }
        }
    }
    
    private void putBool(EnumWeatherProperty p, String key) {
        for(int i = 0; i < weatherXml.length; i++) {
            String data = getXMLText(i, key).trim();
            try {
                p.put(weatherNames[i], Boolean.valueOf(data));
            } catch(NumberFormatException ex) {
                throw new ParseException(String.format(
                    "Property %s in %s: parsing failed", key, weatherNames[i]), ex); 
            }
        }
    }
    
    public String[] getWeatherNames() {
        return weatherNames;
    }
    
    public WeatherProperty[] getProperties() {
        return properties.toArray(new WeatherProperty[0]);
    }
    
    private String getXMLText(int idx, String key) {
        Element cur = weatherXml[idx];
        String[] parts = key.split("\\.");
        for(int i = 0; i < parts.length; i++) {
//            System.out.println(childreen[i]);
            cur = cur.getChild(parts[i]);
            if(cur == null) {
                throw new ParseException(String.format(
                    "Property %s in %s: no XML data not found!", key, weatherNames[i]));
            }
        }
        return cur.getText();
    }
    
    @SuppressWarnings("serial")
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
