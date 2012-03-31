package de.mycrobase.ssim.ed.weather.ext;

/**
 * A mapping from the precipitation types defined in the weather XSD (as integers)
 * into a more usable form of an {@link Enum}. It supports the conversion from
 * IDs by using {@link #fromId(int)} that returns {@link #Unknown} for unknown
 * IDs. 
 * 
 * @author cn
 */
public enum PrecipitationType {
    
    Unknown(-1),
    None(0),
    Rain(1),
    IcePellets(2),
    Snow(3);
    
    private int id;
    
    PrecipitationType(int id) {
        this.id = id;
    }
    
    public static PrecipitationType fromId(int id) {
        for(PrecipitationType t : values()) {
            if(t.id == id) {
                return t;
            }
        }
        return Unknown;
    }
}
