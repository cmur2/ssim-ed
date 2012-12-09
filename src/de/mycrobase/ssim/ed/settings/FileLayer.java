package de.mycrobase.ssim.ed.settings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.mycrobase.ssim.ed.settings.LayeredSettingsManager.WritableLayer;

public class FileLayer implements WritableLayer {
    
    private static final Logger logger = Logger.getLogger(FileLayer.class);
    
    private File file;
    private boolean writable;
    
    private Properties properties;
    
    public FileLayer(File file, boolean writable) {
        this.file = file;
        this.writable = writable;
    }
    
    @Override
    public void load() {
        properties = new Properties();
        if(file.exists()) {
            logger.info(String.format("Loading file %s", file.getAbsolutePath()));
            try {
                FileReader r = new FileReader(file);
                properties.load(r);
                r.close();
            } catch(IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            logger.info(String.format("File %s does not exist.", file.getAbsolutePath()));
        }
    }
    
    @Override
    public boolean isWritable() {
        return writable;
    }
    
    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    @Override
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    @Override
    public void store() {
        if(!writable) {
            throw new UnsupportedOperationException("This layer is not writable!");
        }
        try {
            FileWriter w = new FileWriter(file);
            properties.store(w, "");
            w.close();
        } catch(IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public String toString() {
        return String.format("FileLayer(%s)", file.getAbsolutePath());
    }
}
