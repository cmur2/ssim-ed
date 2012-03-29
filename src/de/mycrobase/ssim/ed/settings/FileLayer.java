package de.mycrobase.ssim.ed.settings;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import chlib.RuntimeIOException;
import chlib.streams.FileHandler;
import de.mycrobase.ssim.ed.settings.LayeredSettingsManager.WritableLayer;

public class FileLayer implements WritableLayer {
    
    private static final Logger logger = Logger.getLogger(FileLayer.class);
    
    private FileHandler fileHandler;
    private boolean writable;
    
    private Properties properties;
    
    public FileLayer(File file, boolean writable) {
        this.fileHandler = new FileHandler(file);
        this.writable = writable;
    }
    
    @Override
    public void load() {
        properties = new Properties();
        File file = fileHandler.getFile();
        if(file.exists()) {
            logger.info(String.format("Loading file %s", file.getAbsolutePath()));
            try {
                properties.load(fileHandler.getReadStream());
                fileHandler.closeRead();
            } catch(IOException ex) {
                throw new RuntimeIOException(ex);
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
            properties.store(fileHandler.getWriteStream(), "");
            fileHandler.closeWrite();
        } catch(IOException ex) {
            throw new RuntimeIOException(ex);
        }
    }
    
    @Override
    public String toString() {
        return String.format("FileLayer(%s)", fileHandler.getFile().getAbsolutePath());
    }
}
