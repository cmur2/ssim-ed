package sed.pre.terrain;

public enum TerrainType {
    
    /**
     * Fallback type for every area that is unspecified.
     */
    Default(0),
    
    /**
     * Shallow beach.
     */
    Beach(1),
    
    /**
     * Green shallow grass lands.
     */
    Gras(2),
    
    /**
     * High mountains.
     */
    Mountain(3),
    
    /**
     * Floor of shallow waters.
     */
    LakeFloor(8),
    
    /**
     * Floor of deep waters.
     */
    OceanFloor(9);
    
    private int id;
    
    TerrainType(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }
}