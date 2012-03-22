package sed.pre.terrain;

public enum TerrainType {
    
    /**
     * Fallback type for every area that is unspecified.
     */
    Default(0),
    
    /**
     * Floor of shallow waters.
     */
    LakeFloor(1),
    
    /**
     * Floor of deep waters.
     */
    OceanFloor(2),
    
    /**
     * Shallow shore.
     */
    Shore(3),
    
    /**
     * Steep coast.
     */
    Cliff(4),
    
    /**
     * Flat plains.
     */
    Plain(5),
    
    /**
     * High flat plains.
     */
    HighPlain(6),
    
    /**
     * Small hills.
     */
    Hill(7),
    
    /**
     * Flat mountain tops.
     */
    MountainTop(8),
    
    /**
     * High mountains.
     */
    Mountains(9),
    
    /**
     * Very high mountains.
     */
    HighMountains(10);
    
    private int id;
    
    TerrainType(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }
}