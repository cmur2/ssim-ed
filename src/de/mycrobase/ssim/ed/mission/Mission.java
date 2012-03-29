package de.mycrobase.ssim.ed.mission;

public interface Mission {
    
    public String getTitle();
    public String getDescription();
    public String getMapFile();
    public float getLatitude();
    public float getLongitude();
    public int getDayOfYear();
    public float getTimeOfDay();
}
