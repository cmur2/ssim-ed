package ssim.sim;

import java.awt.*;

/**
 *
 * @author Ch. Nicolai
 */
public final class SimConst {
    
    private SimConst() {}
    
    /**
     * Konstante der Fallbeschleunigung auf der Erde, g = 9,81 m/(s*s).
     */
    public static final float g = 9.81f;
    /**
     * Gravitationskonstante G, G = 6,67428e-11 m*m / (kg * s*s)
     */
    public static final float G = 6.67428e-11f;
    /**
     * Umrechungszahl (Multiplikation) von Seemeilen in Meter.
     * 1 Seemeile (sm) * sm_to_m = 1852.2 Meter (m)
     */
    public static final float sm_to_m = 1852.2f;
    public static final float m_to_sm = 1/sm_to_m;
    public static final float sm_to_km = sm_to_m/1000;
    public static final float km_to_sm = 1/sm_to_km;
    
    public static final float kmh_to_ms = 1000f/3600;
    public static final float ms_to_kmh = 1/kmh_to_ms;
    public static final float knt_to_kmh = sm_to_km;
    public static final float kmh_to_knt = 1/knt_to_kmh;
    public static final float ms_to_knt = ms_to_kmh*kmh_to_knt;
    public static final float knt_to_ms = 1/ms_to_knt;

    public static final float m_to_km = 1000f;
    public static final float km_to_m = 1/m_to_km;
    
    /**
     * Dichte von Wasser in kg/(m*m)
     */
    public static final float simWaterDensity = 1000; // in kg/(m*m)
    /**
     * Dichte von Luft in kg/(m*m)
     */
    public static final float simAirDensity = 1.293f; // in kg/(m*m)
}
