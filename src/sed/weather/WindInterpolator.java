package sed.weather;

import com.jme3.math.FastMath;

/**
 * Wind is modeled as an 3D vector always perpendicular to the Y axis
 * (straight up) so the Y component is always zero. This is interpreted
 * the direction of the wind where e.g. (1.0, 0, 1.0) means "from north east".
 * 
 * @author cn
 */
public class WindInterpolator implements WeatherInterpolator {
    
    public static void main(String[] args) {
        WindInterpolator w = new WindInterpolator();
        System.out.println(w.interpolate(40f, 320f, 0.1f));
    }
    
    @Override
    public Object interpolate(Object valueA, Object valueB, float ratio) {
        float a = (Float) valueA;
        float b = (Float) valueB;
        if(Math.abs(a-b) <= 180) {
            return FastMath.interpolateLinear(ratio, a, b);
        } else {
            if(a < b) a += 360; else b += 360;
            float t = FastMath.interpolateLinear(ratio, a, b);
            return t >= 360 ? t-360 : t;
        }
        // TODO: wind interpolation
    }
}
