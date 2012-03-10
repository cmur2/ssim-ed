package sed.sky;

import sed.mission.Mission;
import ssim.sim.SimClock;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

/**
 * 
 * @author cn
 */
public class Sun {
    
    private SimClock simClock;
    private Mission mission;
    
    /**
     * x: sunPhiAngle, y: sunThetaAngle
     */
    private Vector2f sunAngles;
    
    public Sun(SimClock simClock, Mission mission) {
        this.simClock = simClock;
        this.mission = mission;
    }
    
    /**
     * @param store a Vector3f to store the result
     * @return sun position on a sky dome hemisphere in J3D coordinates
     */
    public Vector3f getSunPosition(Vector3f store) {
        if(store == null) {
            store = new Vector3f();
        }
        return store.set(
            (float) (Math.sin(sunAngles.y) * Math.cos(sunAngles.x)),
            (float) (Math.cos(sunAngles.y)),
            (float) (-Math.sin(sunAngles.y) * Math.sin(sunAngles.x))
            ).normalizeLocal();
    }
    
    /**
     * @param store a Vector3f to store the result
     * @return tupel with sun's phi and theta angle in radians
     */
    public Vector2f getSunAngles(Vector2f store) {
        if(store == null) {
            store = new Vector2f();
        }
        return store.set(sunAngles);
    }
    
    public void update() {
        float timeOfDay = simClock.hourTime();
        sunAngles = calcSunPosition(
            timeOfDay,
            mission.getDayOfYear(),
            mission.getLatitude(),
            mission.getLongitude(),
            sunAngles);
    }
    
    /**
     * Derives the sun's position on a sky dome hemisphere from the time,
     * date and position (on earth). The position is returned as tuple
     * of phi angle and theta angle.
     * <p>
     * Theta angle:
     *   if sun is at zenith this will be 0 degrees,
     *   if sun is at horizon this will be 90 degrees.
     * <p>
     * Phi angle:
     *   if sun is in the north this will be 0 degrees,
     *   if sun is in the south (more common) this will be 180 degrees.
     * 
     * @param timeOfDay floating point hours,
     *          see {@link ssim.sim.SimClock#hourTime()}
     * @param dayOfYear day of year <i>(0-365)</i>
     * @param latitude the first location component (N-S) in degrees
     * @param longitude the second location component (E-W) in degrees
     * @param store a Vector3f to store the result
     * @return tuple with sun's phi and theta angle in radians
     */
    private static Vector2f calcSunPosition(
            double timeOfDay, int dayOfYear,
            double latitude, double longitude, Vector2f store)
    {
        if(store == null) {
            store = new Vector2f();
        }
        // derive standard meridian from longitude
        int standardMeridian = (int) (longitude / 15f);
        // solarTime in hours
        double solarTime = timeOfDay +
            (0.170*Math.sin(4d*Math.PI*(dayOfYear-80d)/373d) - 0.129*Math.sin(2d*Math.PI*(dayOfYear-8d)/355d)) +
            (standardMeridian-longitude)/15d;
        // "(standardMeridian-longitude)/15d":
        //   time difference between 2 meridians is 4min, 15 meridians -> 1h
        double solarDeclination = 0.4093*Math.sin(2d*Math.PI*(dayOfYear-81d)/368d);
        latitude = Math.toRadians(latitude);
        double temp = Math.cos(solarDeclination) * Math.cos(Math.PI * solarTime/12d);
        double opp = -(Math.cos(solarDeclination) * Math.sin(Math.PI * solarTime/12d));
        double adj = -(Math.cos(latitude) * Math.sin(solarDeclination) + Math.sin(latitude) * temp);
        // solarAzimuth = Math.atan2(opp, adj)
        float sunPhiAngle = -(float) Math.atan2(opp, adj)-FastMath.HALF_PI; 
        // height angle
        float solarAltitude = (float) Math.asin(Math.sin(latitude)*Math.sin(solarDeclination)-Math.cos(latitude)*temp);
        // zenith angle
        float sunThetaAngle = FastMath.HALF_PI-solarAltitude;
        store.set(sunPhiAngle, sunThetaAngle);
        return store;
    }
}
