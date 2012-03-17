package sed.settings;

import java.util.ArrayList;

public class LayeredSettingsManager implements Settings {

    private ArrayList<Layer> layers = new ArrayList<Layer>();
    
    private WritableLayer writableLayer;
    
    public LayeredSettingsManager() {
    }
    
    public void addLayer(Layer layer) {
        layer.load();
        layers.add(layer);
        if(layer.isWritable()) {
            if(writableLayer != null) {
                // we have already an existing writable layer
                writableLayer.store();
            }
            writableLayer = (WritableLayer) layer;
        }
    }
    
    public void flush() {
        writableLayer.store();
    }
    
    public interface Layer {
        
        public void load();
        
        public boolean isWritable();
        
        public String getProperty(String key);
    }
    
    public interface WritableLayer extends Layer {

        public void setProperty(String key, String value);
        
        public void store();
    }
    
    @Override
    public String getString(String key) {
        for(int i = layers.size()-1; i >= 0; i--) {
            String value = layers.get(i).getProperty(key);
            if(value != null) {
                return value;
            }
        }
        throw new UnsupportedOperationException("Unable to find property: "+key);
    }

    @Override
    public void setString(String key, String value) {
        if(writableLayer == null) {
            throw new UnsupportedOperationException("No writable layer found!");
        } else {
            writableLayer.setProperty(key, value);
        }
    }

    @Override
    public float getFloat(String key) {
        return Float.parseFloat(getString(key));
    }

    @Override
    public void setFloat(String key, float value) {
        setString(key, Float.toString(value));
    }

    @Override
    public int getInteger(String key) {
        return Integer.parseInt(getString(key));
    }

    @Override
    public void setInteger(String key, int value) {
        setString(key, Integer.toString(value));
    }

    @Override
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }

    @Override
    public void setBoolean(String key, boolean value) {
        setString(key, Boolean.toString(value));
    }
}
