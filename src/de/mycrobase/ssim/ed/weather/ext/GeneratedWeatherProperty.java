package de.mycrobase.ssim.ed.weather.ext;

import de.mycrobase.ssim.ed.weather.WeatherPropertyGenerator;

public class GeneratedWeatherProperty extends EnumWeatherProperty {

    private WeatherPropertyGenerator generator;
    
    public GeneratedWeatherProperty(String key, Class<?> type, WeatherPropertyGenerator gen) {
        super(key, type);
        this.generator = gen;
    }
    
    @Override
    public Object getValue(String weather) {
        Object valueFromConf = super.getValue(weather);
        // ignore it for now
        return generator.generate();
    }
}
