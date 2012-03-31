package de.mycrobase.ssim.ed.weather.ext;

import de.mycrobase.ssim.ed.weather.WeatherController;
import de.mycrobase.ssim.ed.weather.WeatherControllerTestBase;

public class StaticWeatherControllerTest extends WeatherControllerTestBase {
    
    public StaticWeatherControllerTest() {
    }
    
    @Override
    protected WeatherController create(PropertySet init) {
        return new StaticWeatherController(init);
    }
    
}
