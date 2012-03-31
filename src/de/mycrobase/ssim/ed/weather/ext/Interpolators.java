package de.mycrobase.ssim.ed.weather.ext;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;

import de.mycrobase.ssim.ed.weather.WeatherInterpolator;

public class Interpolators {
    
    public static class FloatInterpolator implements WeatherInterpolator {
        @Override
        public Object interpolate(Object valueA, Object valueB, float ratio) {
            float a = (Float) valueA;
            float b = (Float) valueB;
            return FastMath.interpolateLinear(ratio, a, b);
        }
    }
    
    public static class Vec3Interpolator implements WeatherInterpolator {
        private Vector3f store = new Vector3f();
        
        @Override
        public Object interpolate(Object valueA, Object valueB, float ratio) {
            Vector3f a = (Vector3f) valueA;
            Vector3f b = (Vector3f) valueB;
            return FastMath.interpolateLinear(ratio, a, b, store);
        }
    }
    
    public static class IntInterpolator implements WeatherInterpolator {
        @Override
        public Object interpolate(Object valueA, Object valueB, float ratio) {
            int a = (Integer) valueA;
            int b = (Integer) valueB;
            return (int) FastMath.interpolateLinear(ratio, a, b);
        }
    }
    
    /**
     * Does nothing!
     * 
     * @author cn
     */
    public static class IntArrayInterpolator implements WeatherInterpolator {
        @Override
        public Object interpolate(Object valueA, Object valueB, float ratio) {
            return valueA;
        }
    }

    public static class DiscreteValueInterpolator implements WeatherInterpolator {
        @Override
        public Object interpolate(Object valueA, Object valueB, float ratio) {
            return ratio < 0.5f ? valueA : valueB;
        }
    }
}
