package sed.settings;

public interface Settings {
    
    /**
     * Queries the value of the given property key.
     * 
     * @throws UnsupportedOperationException if property doesn't exist
     * @param key used to find the value
     * @return the value
     */
    public String getString(String key);
    
    /**
     * Tries to persist the given value for given property key.
     * 
     * @throws UnsupportedOperationException if persistence is not supported
     * @param key under which the value is filed
     * @param value the value
     */
    public void setString(String key, String value);
    
    /**
     * Queries the value of the given property key.
     * 
     * @throws UnsupportedOperationException if property doesn't exist
     * @param key used to find the value
     * @return the value
     */
    public float getFloat(String key);
    
    /**
     * Tries to persist the given value for given property key.
     * 
     * @throws UnsupportedOperationException if persistence is not supported
     * @param key under which the value is filed
     * @param value the value
     */
    public void setFloat(String key, float value);
    
    /**
     * Queries the value of the given property key.
     * 
     * @throws UnsupportedOperationException if property doesn't exist
     * @param key used to find the value
     * @return the value
     */
    public int getInteger(String key);
    
    /**
     * Tries to persist the given value for given property key.
     * 
     * @throws UnsupportedOperationException if persistence is not supported
     * @param key under which the value is filed
     * @param value the value
     */
    public void setInteger(String key, int value);
    
    /**
     * Queries the value of the given property key.
     * 
     * @throws UnsupportedOperationException if property doesn't exist
     * @param key used to find the value
     * @return the value
     */
    public boolean getBoolean(String key);
    
    /**
     * Tries to persist the given value for given property key.
     * 
     * @throws UnsupportedOperationException if persistence is not supported
     * @param key under which the value is filed
     * @param value the value
     */
    public void setBoolean(String key, boolean value);
}
