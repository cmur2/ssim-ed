package sed.weather;

import com.jme3.math.FastMath;

/**
 * Wind is modeled as an 3D vector always perpendicular to the Y axis
 * (straight up) so the Y component is always zero. This vector gets mainly
 * constructed from an angle denoting the direction where the wind originates
 * from. Example: "320 degrees" means wind from the NW
 * 
 * @author cn
 */
public class WindInterpolator implements WeatherInterpolator {
    
    @Override
    public Object interpolate(Object valueA, Object valueB, float ratio) {
        float a = (Float) valueA;
        float b = (Float) valueB;
        if(Math.abs(a-b) <= 180) {
            // if difference is smaller than 180 degrees (the shorter way from
            // a to b is positive-wise) do linear interpolation without wrapping
            return FastMath.interpolateLinear(ratio, a, b);
        } else {
            // here the shorter way will cross the 0/360 mark, so increase the
            // lower number by 360 which should make them <180 away positive-wise
            // than subtract 360 from the result if necessary
            if(a < b) a += 360; else b += 360;
            float t = FastMath.interpolateLinear(ratio, a, b);
            return t >= 360 ? t-360 : t;
        }
    }
}
