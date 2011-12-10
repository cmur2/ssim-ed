package sed.weather;

public interface ChangeableWeather extends Weather {
    
    /**
     * Updates the given property (if exists) to the given value.
     * 
     * @throws UnsupportedOperationException if property doesn't exist
     * @param key used to find the value
     * @param value the new value
     */
    public void setProperty(String key, Object value);
    
}
