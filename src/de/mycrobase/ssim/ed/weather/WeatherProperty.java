package de.mycrobase.ssim.ed.weather;

public interface WeatherProperty {
    
    public String getKey();
    public Object getValue(String weather);
    public Class getType();
}
