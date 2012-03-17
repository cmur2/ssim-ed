package sed.settings;

public interface Settings {
    
    public String getString(String key);
    
    public void setString(String key, String value);
    
    public float getFloat(String key);
    
    public void setFloat(String key, float value);
    
    public int getInteger(String key);
    
    public void setInteger(String key, int value);
    
    public boolean getBoolean(String key);
    
    public void setBoolean(String key, boolean value);
}
