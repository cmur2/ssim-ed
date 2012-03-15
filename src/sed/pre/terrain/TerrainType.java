package sed.pre.terrain;

public enum TerrainType {
    
    /**
     * Fallback type for every area that is unspecified.
     */
    Default(0),
    
    /**
     * Green shallow grass lands.
     */
    Gras(1),
    
    /**
     * High mountains.
     */
    Mountain(2);
    
    private int id;
    
    TerrainType(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }
}