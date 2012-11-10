package de.mycrobase.ssim.ed.weather.ext;

import java.util.HashMap;
import java.util.Map;

import de.mycrobase.ssim.ed.weather.WeatherProperty;

public class EnumWeatherProperty implements WeatherProperty {
    
    private String key;
    private Class<?> type;
    private Map<String, Object> values = new HashMap<String, Object>();
    
    public EnumWeatherProperty(String key, Class<?> type) {
        this.key = key;
        this.type = type;
    }
    
    @Override
    public String getKey() {
        return key;
    }
    
    @Override
    public Object getValue(String weather) {
        return values.get(weather);
    }
    
    @Override
    public Class<?> getType() {
        return type;
    }
    
    public void put(String weather, Object value) {
        values.put(weather, value);
    }
}
