package sed.weather;

public class StaticWeatherControllerTest extends WeatherControllerTestBase {
    
    public StaticWeatherControllerTest() {
    }
    
    @Override
    protected WeatherController create() {
        return new StaticWeatherController();
    }
    
}
