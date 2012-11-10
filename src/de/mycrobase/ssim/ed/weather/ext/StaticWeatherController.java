package de.mycrobase.ssim.ed.weather.ext;

import de.mycrobase.ssim.ed.weather.BasicWeatherController;
import de.mycrobase.ssim.ed.weather.PropertySet;
import de.mycrobase.ssim.ed.weather.WeatherController;
import de.mycrobase.ssim.ed.weather.WeatherProperty;

/**
 * The simplest {@link WeatherController} controller possible. It just
 * accepts an initial state and applies it, nothing more.
 * 
 * @author cn
 */
public class StaticWeatherController extends BasicWeatherController {

    public StaticWeatherController(String weatherName, WeatherProperty[] properties) {
        for(WeatherProperty p : properties) {
            state.put(p.getKey(), p.getValue(weatherName), p.getType());
        }
    }
    
    @Override
    public void update(float dt) {
        // do nothing since we're static
    }
}
