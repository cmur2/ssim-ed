package sed.weather;

public interface WeatherInterpolator<T> {
    
    /**
     * Request the {@link WeatherInterpolator} to update the given property.
     * 
     * @param weather
     * @param key
     */
    public void update(ChangeableWeather weather, String key);
    
}
