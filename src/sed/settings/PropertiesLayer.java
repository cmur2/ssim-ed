package sed.settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import sed.settings.LayeredSettingsManager.Layer;
import chlib.RuntimeIOException;

public class PropertiesLayer implements Layer {

    private Properties properties;
    
    public PropertiesLayer(Properties properties) {
        this.properties = properties;
    }
    
    @Override
    public void load() {}
    
    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    public static PropertiesLayer fromStream(InputStream stream) {
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch(IOException ex) {
            throw new RuntimeIOException(ex);
        }
        return new PropertiesLayer(properties);
    }
}
