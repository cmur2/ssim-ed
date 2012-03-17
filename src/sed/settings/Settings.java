package sed.settings;

/**
 * This interface provides a simple view on the settings for all components
 * unrelated to settings management - just getter and setter and integrated
 * type conversion for standard types.
 * 
 * Settings are assumed to exist (initialized centrally e.g. in a
 * {@link SettingsManager} and at least with a default value) so no default
 * values are spread over the program. 
 * 
 * @author cn
 */
public interface Settings {
    
    /**
     * Queries the value of the given property key.
     * 
     * @throws UnsupportedOperationException if property doesn't exist or
     *                                       conversion failed
     * @param key used to find the value
     * @return the value
     */
    public String getString(String key);
    
    /**
     * Saves the given value for given property key.
     * 
     * @throws UnsupportedOperationException if persistence is not supported
     * @param key under which the value is filed
     * @param value the value
     */
    public void setString(String key, String value);
    
    /**
     * Queries the value of the given property key.
     * 
     * @throws UnsupportedOperationException if property doesn't exist or
     *                                       conversion failed
     * @param key used to find the value
     * @return the value
     */
    public float getFloat(String key);
    
    /**
     * Saves the given value for given property key.
     * 
     * @throws UnsupportedOperationException if persistence is not supported
     * @param key under which the value is filed
     * @param value the value
     */
    public void setFloat(String key, float value);
    
    /**
     * Queries the value of the given property key.
     * 
     * @throws UnsupportedOperationException if property doesn't exist or
     *                                       conversion failed
     * @param key used to find the value
     * @return the value
     */
    public int getInteger(String key);
    
    /**
     * Saves the given value for given property key.
     * 
     * @throws UnsupportedOperationException if persistence is not supported
     * @param key under which the value is filed
     * @param value the value
     */
    public void setInteger(String key, int value);
    
    /**
     * Queries the value of the given property key.
     * 
     * @throws UnsupportedOperationException if property doesn't exist or
     *                                       conversion failed
     * @param key used to find the value
     * @return the value
     */
    public boolean getBoolean(String key);
    
    /**
     * Saves the given value for given property key.
     * 
     * @throws UnsupportedOperationException if persistence is not supported
     * @param key under which the value is filed
     * @param value the value
     */
    public void setBoolean(String key, boolean value);
}
