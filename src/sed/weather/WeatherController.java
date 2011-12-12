package sed.weather;

public interface WeatherController extends ChangeableWeather {
    
    /**
     * Registers a new property under the given key with the given initial
     * value of given type.
     * 
     * @param <T> type
     * @param key the key, e.g. "ocean.temperature"
     * @param value the initial value
     * @param clazz the type of the value
     */
    public <T> void registerProperty(String key, T value, Class<T> clazz);
    
    /**
     * Registers a given {@link WeatherInterpolator} for properties of given
     * type.
     * 
     * @param <T> type
     * @param interpolator the interpolator to use
     * @param clazz for such kind of properties
     */
    public <T> void registerInterpolator(WeatherInterpolator<T> interpolator, Class<T> clazz);
    
    /**
     * Registers a given {@link WeatherInterpolator} for the given property only.
     * 
     * @param <T> type
     * @param interpolator the interpolator to use
     * @param key for this single property
     */
    public <T> void registerInterpolator(WeatherInterpolator<T> interpolator, String key);
    
    /**
     * Request the {@link WeatherController} to update it's logic.
     * 
     * @param dt time passed since last invocation
     */
    public void update(float dt);
}