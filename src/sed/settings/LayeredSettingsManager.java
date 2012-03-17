package sed.settings;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * Uses {@link Layer}s to implement a fall back mechanism where the highest
 * (added most recently via {@link #addLayer(Layer)} layer is queried first and
 * if he contains no definition for a property the next lower one is queried etc.
 * A layer may be writable (but don't have to) so one can select which layer's
 * implementation is be responsible for persisting changed settings from the user.
 * The highest writable layer will be used then.
 * 
 * @author cn
 */
public class LayeredSettingsManager implements SettingsManager {

    private static final Logger logger = Logger.getLogger(LayeredSettingsManager.class);
    
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
                logger.info(String.format("Flushing old writable layer %s", writableLayer));
                writableLayer.store();
            }
            logger.info(String.format("Using %s as writable layer", layer));
            writableLayer = (WritableLayer) layer;
        }
    }
    
    public void flush() {
        writableLayer.store();
    }
    
    public interface Layer {
        /**
         * Allow the layer (e.g. file-based) to do some loading. After this
         * call the properties have to be accessible via
         *  {@link #getProperty(String)}.
         */
        public void load();
        
        /**
         * When this is true, the layer has to implement {@link WritableLayer}.
         * 
         * @return whether this layer may be used for persistence or not
         */
        public boolean isWritable();
        
        /**
         * Queries the value of the given property key.
         * 
         * @param key used to find the value
         * @return the value or {@code null} if property doesn't exist
         */
        public String getProperty(String key);
    }
    
    public interface WritableLayer extends Layer {
        
        /**
         * Saves the given value for given property key.
         * 
         * @param key under which the value is filed
         * @param value the value
         */
        public void setProperty(String key, String value);
        
        /**
         * Force the layer to persist all properties to disk.
         */
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
        try {
            return Float.parseFloat(getString(key));
        } catch(NumberFormatException ex) {
            throw new UnsupportedOperationException(ex);
        }
    }

    @Override
    public void setFloat(String key, float value) {
        setString(key, Float.toString(value));
    }

    @Override
    public int getInteger(String key) {
        try {
            return Integer.parseInt(getString(key));
        } catch(NumberFormatException ex) {
            throw new UnsupportedOperationException(ex);
        }
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
