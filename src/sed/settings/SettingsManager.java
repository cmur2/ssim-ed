package sed.settings;

public interface SettingsManager extends Settings {
    
    /**
     * Tries to persist all setting's values to disk.
     */
    public void flush();
}
