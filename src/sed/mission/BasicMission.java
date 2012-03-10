package sed.mission;

public class BasicMission implements Mission {
    
    private String id;
    
    private String title;
    private String description;
    private String mapFile;
    
    private float latitude;
    private float longitude;
    
    private int dayOfYear;
    private float timeOfDay;
    
    //private Condition[] winConditions;
    //private Condition[] loseConditions;
    
    public BasicMission(String id) {
        this.id = id;
    }
    
    @Override
    public String getTitle() {
        return null;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    @Override
    public String getDescription() {
        return null;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    @Override
    public int getDayOfYear() {
        return dayOfYear;
    }
    
    public void setDayOfYear(int dayOfYear) {
        this.dayOfYear = dayOfYear;
    }
    
    @Override
    public float getTimeOfDay() {
        return timeOfDay;
    }
    
    public void setTimeOfDay(float timeOfDay) {
        this.timeOfDay = timeOfDay;
    }
    
    @Override
    public String toString() {
        return String.format("Mission(%s)", id);
    }
}
