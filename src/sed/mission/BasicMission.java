package sed.mission;

public class BasicMission implements Mission {
    
    //private Condition[] winConditions;
    //private Condition[] loseConditions;
    private String mapFile;
    
    private float latitude;
    private float longitude;
    
    public BasicMission(String mapFile, float latitude, float longitude) {
        this.mapFile = mapFile;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public BasicMission() {
    }
    
    @Override
    public String getMapFile() {
        return mapFile;
    }
    
    public void setMapFile(String mapFile) {
        this.mapFile = mapFile;
    }
    
    @Override
    public float getLatitude() {
        return latitude;
    }
    
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }
    
    @Override
    public float getLongitude() {
        return longitude;
    }
    
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }
}
