package de.mycrobase.ssim.ed.settings;

public interface SettingsManager extends Settings {
    
    /**
     * Tries to persist all setting's values to disk.
     */
    public void flush();
}
