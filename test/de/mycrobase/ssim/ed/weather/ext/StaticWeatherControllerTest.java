package de.mycrobase.ssim.ed.weather.ext;

import org.junit.experimental.categories.Category;

import de.mycrobase.ssim.ed.helper.categories.Fast;
import de.mycrobase.ssim.ed.weather.PropertySet;
import de.mycrobase.ssim.ed.weather.WeatherController;
import de.mycrobase.ssim.ed.weather.WeatherControllerTestBase;

@Category(Fast.class)
public class StaticWeatherControllerTest extends WeatherControllerTestBase {
    
    public StaticWeatherControllerTest() {
    }
    
    @Override
    protected WeatherController create(PropertySet init) {
        return new StaticWeatherController(init);
    }
    
}
