package de.mycrobase.ssim.ed.weather.ext;

import de.mycrobase.ssim.ed.weather.BasicWeatherController;
import de.mycrobase.ssim.ed.weather.WeatherController;

/**
 * The simplest {@link WeatherController} controller possible. It just
 * accepts an initial state and applies it, nothing more.
 * 
 * @author cn
 */
public class StaticWeatherController extends BasicWeatherController {

    @SuppressWarnings("unchecked")
    public StaticWeatherController(PropertySet init) {
        for(PropertySet.Entry e : init) {
            registerProperty(e.getKey(), e.getValue(), e.getClazz());
        }
    }
    
    @Override
    public void update(float dt) {
        // do nothing since we're static
    }
    
}
