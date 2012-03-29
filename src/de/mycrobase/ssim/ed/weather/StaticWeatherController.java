package de.mycrobase.ssim.ed.weather;

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
