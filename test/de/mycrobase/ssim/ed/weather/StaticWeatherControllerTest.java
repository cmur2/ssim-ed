package de.mycrobase.ssim.ed.weather;

public class StaticWeatherControllerTest extends WeatherControllerTestBase {
    
    public StaticWeatherControllerTest() {
    }
    
    @Override
    protected WeatherController create(PropertySet init) {
        return new StaticWeatherController(init);
    }
    
}
