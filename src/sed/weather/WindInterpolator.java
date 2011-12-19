package sed.weather;

import com.jme3.math.Vector3f;

/**
 * Wind is modeled as an 3D vector always perpendicular to the Y axis
 * (straight up) so the Y component is always zero. This is interpreted
 * the direction of the wind where e.g. (1.0, 0, 1.0) means "north east".
 * 
 * @author cn
 */
public class WindInterpolator implements WeatherInterpolator {
    
    @Override
    public Object interpolate(Object valueA, Object valueB, float ratio) {
        Vector3f a = (Vector3f) valueA;
        Vector3f b = (Vector3f) valueB;
        // TODO: wind interpolation
        return null;
    }
}
