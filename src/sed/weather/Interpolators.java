package sed.weather;

import com.jme3.math.FastMath;

public class Interpolators {
    
    public static class FloatInterpolator implements WeatherInterpolator {
        @Override
        public Object interpolate(Object valueA, Object valueB, float ratio) {
            float a = (Float) valueA;
            float b = (Float) valueB;
            return FastMath.interpolateLinear(ratio, a, b);
        }
    }

}
