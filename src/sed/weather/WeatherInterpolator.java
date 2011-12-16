package sed.weather;

public interface WeatherInterpolator {
    
    /**
     * Request the {@link WeatherInterpolator} to update the given property
     * for the given time ratio passed.
     * 
     * @param valueA the "old" value
     * @param valueB the "new" value
     * @param ratio interpolation factor in [0.0, 1.0]
     * @return valueA if ratio equals 0.0, valueB if ratio equals 1.0,
     *         or a value in [valueA, valueB] for any other ratio
     */
    public Object interpolate(Object valueA, Object valueB, float ratio);
    
}
