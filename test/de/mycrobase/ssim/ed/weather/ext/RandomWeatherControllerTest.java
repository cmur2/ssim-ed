package de.mycrobase.ssim.ed.weather.ext;

import org.junit.experimental.categories.Category;

import de.mycrobase.ssim.ed.helper.categories.Fast;
import de.mycrobase.ssim.ed.weather.WeatherController;
import de.mycrobase.ssim.ed.weather.WeatherControllerTestBase;
import de.mycrobase.ssim.ed.weather.WeatherProperty;

@Category(Fast.class)
public class RandomWeatherControllerTest extends WeatherControllerTestBase {
    
    public RandomWeatherControllerTest() {
    }
    
    @Override
    protected WeatherController create(String weather, WeatherProperty[] properties) {
        return new RandomWeatherController(1f, new String[] {weather}, properties);
    }
    
}
