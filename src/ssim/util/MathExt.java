/*
 * SSim - a realtime Submarine Simulator
 *
 * Copyright (C) 2006-2011  Ch. Nicolai
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3 only,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ssim.util;

import java.util.Random;
import javax.vecmath.*;

/**
 *
 * @author Ch. Nicolai
 */
public final class MathExt {

    private static final Random random = new Random();

    private MathExt() {}

    /**
     * This constant provides the Math.PI value in float precision.
     */
    public static final float PI = (float)Math.PI;
    /**
     * This constant provides the inverse square root of two in float
     * precision which is equal to the sine of 45 degrees.
     */
    public static final float INV_SQRT_TWO = (float)(1d/Math.sqrt(2));

    /**
     * Determines if a given number is a power of 2.
     *
     * @param x input
     * @return true if x is a power two
     */
    public static boolean isPowerOfTwo(double x) {
        double log = Math.log10(x)/Math.log10(2);
        return (int)log == log;
//        while(x > 1) {
//            x = x / 2;
//            if(x == 1) return true;
//        }
//        return false;
    }

    /**
     * Tests if a given value lies in a specific neighborhood around
     * a reference value: [reference-eps, reference+eps].
     *
     * @param testValue the test value
     * @param reference the reference value
     * @param eps the neighborhood's size, must be positive!
     * @return true if testValue is in [reference-eps, reference+eps]
     */
    public static boolean epsilonEquals(
        double testValue, double reference, double eps)
    {
        return reference-eps <= testValue && testValue <= reference+eps;
    }

    /**
     * Tests if a given value lies in a specific neighborhood around
     * a reference value: [reference-eps, reference+eps].
     *
     * @param testValue the test value
     * @param reference the reference value
     * @param eps the neighborhood's size, must be positive!
     * @return true if testValue is in [reference-eps, reference+eps]
     */
    public static boolean epsilonEquals(
        float testValue, float reference, float eps)
    {
        return reference-eps <= testValue && testValue <= reference+eps;
    }

    /**
     * Tests if a given value lies NOT in a specific neighborhood around
     * a reference value: [reference-eps, reference+eps].
     *
     * @param testValue the test value
     * @param reference the reference value
     * @param eps the neighborhood's size, must be positive!
     * @return true if testValue is NOT in [reference-eps, reference+eps]
     */
    public static boolean notEpsilonEquals(
            double testValue, double reference, double eps)
    {
        return testValue < reference-eps || testValue > reference+eps;
    }

    /**
     * Tests if a given value lies NOT in a specific neighborhood around
     * a reference value: [reference-eps, reference+eps].
     *
     * @param testValue the test value
     * @param reference the reference value
     * @param eps the neighborhood's size, must be positive!
     * @return true if testValue is NOT in [reference-eps, reference+eps]
     */
    public static boolean notEpsilonEquals(
            float testValue, float reference, float eps)
    {
        return testValue < reference-eps || testValue > reference+eps;
    }

    /**
     * A simplified function for
     * {@link #epsilonEquals(float, float, float)} with fixed reference
     * value ZERO.
     * 
     * @param testValue the test value
     * @param eps the neighborhood's size, must be positive!
     * @return true if testValue is in [-eps, +eps]
     */
    public static boolean epsilonEqualsZero(float testValue, float eps) {
        return -eps <= testValue && testValue <= eps;
    }

    public static double wrapByMax(double value, double max) {
        return value-(int)Math.floor(value/max)*max;
    }

    public static float wrapByMax(float value, float max) {
        return value-(int)Math.floor(value/max)*max;
        //return modByMax(value, max); // wrappt negative Zahlen nicht wie erwuenscht
    }

    public static int wrapByMax(int value, int max) {
        return value-(int)Math.floor((float)value/max)*max;
        //return modByMax(value, max); // wrappt negative Zahlen nicht wie erwuenscht
        //return (max+value) % max; // faster (!) but fails if value < -2*max
    }

    public static double modByMax(double val, double max) {
        return val % max;
    }

    public static float modByMax(float val, float max) {
        return val % max;
    }

    public static int modByMax(int val, int max) {
        return val % max;
    }

    /**
     * Generates nearly random integers in range 0 to given limit - 1.<br/>
     * <br/>
     * This is useful for randomized array index creation.
     *
     * @param max the upper boundary &gt; 0
     * @return a random integer between zero and max
     */
    public static int randInt(int max) {
        return (int) (random.nextFloat()*(max-1));
    }

    /**
     * Wrapper for {@link Math#ceil(double)} converting data types.
     *
     * @param x input
     * @return Math.ceil(x)
     */
    public static int ceil(float x) {
        return (int) Math.ceil(x);
    }

    /**
     * Wrapper for {@link Math#floor(double)} converting data types.
     *
     * @param x input
     * @return Math.floor(x)
     */
    public static int floor(float x) {
        return (int) Math.floor(x);
    }

    /**
     * Tests if a given value x lies in range a to b. This works normally
     * if a &lt; b and if a &gt; b the following rule applies:
     * <code>isInRange(x, b, a)</code>
     *
     * @param x value to be tested
     * @param a first bound
     * @param b second bound
     * @return true, if x lies between a and b, thus if x is in [a,b]
     */
    public static boolean isInRange(double x, double a, double b) {
        if(a < b) {
            return a <= x && x <= b;
        } else {
            return b <= x && x <= a;
        }
    }

    /**
     * This function does a 1-dimensional linear interpolation between
     * given values a and b based on x.<br/>
     * It is the fastest interpolation method.
     *
     * @param a first value
     * @param b second value
     * @param x value in [0,1] controlling how a and b are combined
     * @return a, if x equals 0 <i>or</i><br/>
     *         b, if x equals 1 <i>or</i><br/>
     *         else a value between a and b linearly interpolated by x
     */
    public static double interpolateLinear(double a, double b, double x) {
        return a + (b-a)*x; // = (1-x)*a + x*b;
    }

    /**
     * This function does a 1-dimensional cosine interpolation between
     * given values a and b based on x.<br/>
     * This method gives a much smother curve than linear interpolation
     * but it is slower (one call to {@link Math#cos(double)}).
     *
     * @see #interpolateLinear(double, double, double)
     * @param a first value
     * @param b second value
     * @param x value in [0,1] controlling how a and b are combined
     * @return a, if x equals 0 <i>or</i><br/>
     *         b, if x equals 1 <i>or</i><br/>
     *         else a cosine interpolated value between a and b
     */
    public static double interpolateCosine(double a, double b, double x) {
        double f = (1-Math.cos(x*Math.PI))*0.5;
        return  a*(1-f) + b*f;
    }
    
    /**
     * This function does a 1-dimensional cubic interpolation between
     * given v1 (as "a", see linear interpolation) and v2 (as "b")
     * based on auxiliary values v0, v3 and x.<br/>
     * This method gives a much smother curve than linear interpolation
     * but it is slower.
     *
     * @see #interpolateLinear(double, double, double)
     * @param v0 the point before a
     * @param v1 the point a
     * @param v2 the point b
     * @param v3 the point after b
     * @param x value in [0,1] controlling how v0, v1, v2 and v3
     *          are combined
     * @return the cubic interpolated value
     *         (<b>roughly</b> between v1 and v2, without guarantee!)
     */
    public static double interpolateCubic(
            double v0, double v1, double v2, double v3, double x)
    {
        double P = v0-v1;
        double Q = (v3-v2) - P;
        return Q*x*x*x + (P-Q)*x*x + (v2-v0)*x + v1;
    }
    
    /**
     * Corrects a number in degree to it's equivalent in the range 0-360
     * degrees.
     *
     * @param d value to be corrected
     * @return correct value (in range <i>[0,360[</i> degrees)
     */
    public static double normDeg(double d) {
//        double rv = d;
//        while(rv < 0) rv += 360;
//        while(rv > 359) rv -= 360;
        return wrapByMax(d, 360d);
    }

    /**
     * Corrects a number in degree to it's equivalent in the range 0-360
     * degrees.
     *
     * @param d value to be corrected
     * @return correct value (in range <i>[0,360[</i> degrees)
     */
    public static float normDeg(float d) {
        return wrapByMax(d, 360f);
    }

    /**
     * Corrects a number in radians to it's equivalent in the range -PI
     * to +PI.
     *
     * @param d value to be corrected
     * @return correct value (-PI to +PI)
     */
//    public static double normRad(double d) {
//        //double rv = d;
//        //while(rv < -PI) rv += PI*2;
//        //while(rv > PI) rv -= PI*2;
//        return wrapByMax(d, 2*PI);
//    }
    
    
    /*public static final double getDistanceAt2DLine(Position p1, Position p2) {
        double xdiff = p2.x-p1.x, ydiff = p2.y-p1.y;
        return Math.sqrt((ydiff*ydiff)+(xdiff*xdiff));
    }*/
    /**
     * Calculates distance between to given points on the XZ-plane.
     *
     * @param p1 point 1
     * @param p2 point 2
     * @return the distance
     */
    public static float getDistanceAtXZLine(Tuple3f p1, Tuple3f p2) {
        float xdiff = p2.x-p1.x, ydiff = p2.z-p1.z;
        return (float) Math.sqrt((ydiff*ydiff)+(xdiff*xdiff));
    }

    /**
     * Determines the biggest number in given array.
     *
     * @param values an array of numbers
     * @throws IllegalArgumentException if the array length is zero
     * @return the biggest number
     */
    public static int getMaxValue(int[] values) {
        if(values.length > 0) {
            double maxd = values[0];
            int num = 0;
            for(int i = 1; i < values.length; i++) {
                double a = values[i];
                if(a > maxd) {
                    maxd = a;
                    num = i;
                }
            }
            return values[num];
        } else {
            throw new IllegalArgumentException("Zero sized array!");
        }
    }

    /**
     * Determines the biggest number in given array.
     *
     * @param values an array of numbers
     * @throws IllegalArgumentException if the array length is zero
     * @return the index of the biggest found number
     */
    public static int getMaxValueIndex(int[] values) {
        if(values.length > 0) {
            int maxd = values[0];
            int num = 0;
            for(int i = 1; i < values.length; i++) {
                int a = values[i];
                if(a > maxd) {
                    maxd = a;
                    num = i;
                }
            }
            return num;
        } else {
            throw new IllegalArgumentException("Zero sized array!");
        }
    }

    /**
     * Returns fraction of given value: frac(5.111) == 0.111 and so on.
     *
     * @param x input
     * @return fraction of x
     */
    public static double frac(double x) {
        return modByMax(x, 1.0);
    }

    /**
     * Returns fraction of given value: frac(5.111) == 0.111 and so on.
     * 
     * @param x input
     * @return fraction of x
     */
    public static float frac(float x) {
        return modByMax(x, 1.0f);
    }

    public static double clamp(double v, double a, double b) {
        if(v < a) { return a; }
        if(v > b) { return b; }
        return v;
    }
    
    public static float clamp(float v, float a, float b) {
        if(v < a) { return a; }
        if(v > b) { return b; }
        return v;
    }
    
    public static float clamp01(float v) {
        if(v < 0) { return 0; }
        if(v > 1) { return 1; }
        return v;
    }
    
    public static double clampNeg1ToPos1(double v) {
        if(v < -1) { return -1; }
        if(v > 1) { return 1; }
        return v;
    }
    public static float clampNeg1ToPos1(float v) {
        if(v < -1) { return -1; }
        if(v > 1) { return 1; }
        return v;
    }
    
    /*public static final double getKurs(Position cur, Position other) {
        double d = MathExt.getDistanceAt2DLine(cur, other);
        double ydiff = other.y-cur.y;
        double xdiff = other.x-cur.x;
        return normDeg((xdiff < 0 ? 180 : 0)+
            Math.signum(xdiff)*Math.toDegrees(Math.asin(ydiff/d)));
    }*/

    /**
     * Berechnet den <u>Kurs</u> bezogen auf Position <b>cur</b> zu Position <b>other</b>.
     * @param cur Bezugspunkt
     * @param other Zielpunkt
     * @return den Kurs <i>(in degrees)</i>
     */
    public static float getKurs(Point3f cur, Point3f other) {
        float d = MathExt.getDistanceAtXZLine(cur, other);
        if(d < 1e-8) { return 0; }
        float xdiff = other.x-cur.x;
        float zdiff = -other.z-(-cur.z);
        float a = (float) Math.toDegrees(Math.acos(zdiff/d));
        if(xdiff < 0) {
            return 360f-a;
        } else {
            return a;
        }
    }

    /**
     * Calculates slope from first to second given position.
     * 
     * @param cur point of reference
     * @param other 'target'
     * @return the slope (in degrees)
     */
    public static float getSlope(Point3f cur, Point3f other) {
        float d = MathExt.getDistanceAtXZLine(cur, other);
        if(d < 1e-8) { return 0; }
        float ydiff = other.y-cur.y;
        return (float) Math.toDegrees(Math.atan(ydiff/d));
    }
    
    /**
     * Berechnet die <u>Differenz</u> zwischen beiden Kursen.
     * @param curkurs erster Kurs <i>(in degrees)</i>
     * @param otherkurs zweiter Kurs <i>(in degrees)</i>
     * @return die Differenz <i>(in degrees)</i>
     */
    public static double getAbsKursDiff(double curkurs, double otherkurs) {
        double kursdiffa = MathExt.normDeg(curkurs-otherkurs);
        double kursdiffb = MathExt.normDeg(otherkurs-curkurs);
        return kursdiffa < kursdiffb ? kursdiffa : kursdiffb;
    }
    public static float getAbsKursDiff(float curkurs, float otherkurs) {
        float kursdiffa = MathExt.normDeg(curkurs-otherkurs);
        float kursdiffb = MathExt.normDeg(otherkurs-curkurs);
        return kursdiffa < kursdiffb ? kursdiffa : kursdiffb;
    }
    
    // returns the diff from testKurs relative to baseKurs_ - ^= right, + ^= left dir
    public static float getRelativeKursDiff(float baseKurs, float testKurs) {
        float diff = baseKurs-testKurs;
        if(diff > 180) { diff -= 360; }
        if(diff < -180) { diff += 360; }
        return diff;
    }
    
    /**
     * Calculates angle named <b>gamma</b> or <b>psi</b> in a <u>skydome</u>
     * between two vectors coming from the skydome's origin which are defined
     * by <b>theta1</b>/<b>phi1</b> and <b>theta2</b>/<b>phi2</b>.
     * 
     * @param theta1 theta for vector one <i>(in radians)</i>
     * @param phi1 phi for vector one <i>(in radians)</i>
     * @param theta2 theta for vector <i>(in radians)</i>
     * @param phi2 phi for vector two <i>(in radians)</i>
     * @return angle named <b>gamma</b> or <b>psi</b> with float precision <i>(in radians)</i>
     */
    public static float getAngleBetween(float theta1, float phi1, float theta2, float phi2) {
        double cospsi = Math.sin(theta1)*Math.sin(theta2)*Math.cos(phi2-phi1) + Math.cos(theta1)*Math.cos(theta2);
        if(cospsi > 1.0) { return 0f; }
        else if(cospsi < -1.0) { return PI; }
        return (float)Math.acos(cospsi);
    }
    
    /**
     * Function describing the <u>five parameter model</u> by <i>R. Perez et al.</i>
     * to calculate the <u>sky luminance</u> <b>Y</b> at a Point <b>P</b>
     * lying on a <u>skydome</u> where <b>P</b> is pointed by a vector <b>V</b>
     * from the skydome's origin.<br>
     * <br>
     * <b>V</b> is defined by <b>theta</b> and <b>gamma</b> where <b>theta</b>
     * is the angle between <b>V</b> and a axis orthogonal to the skydome's ground
     * in the center of the skydome.<br>
     * The angle <b>gamma</b> is between <b>V</b> and a vector <b>Vs</b> which points
     * from the skydome's origin to the current position of the sun.<br>
     * <br>
     * Perez <u>five parameter model</u>:<br>
     * F(theta, gamma) = (1 + A * e^(B/cos(theta)) * (1 + C * e^(D*gamma) + E * cos^2(gamma))
     * @param coeffs array containing the five parameters of Perez' model
     * @param theta angle describing <b>V</b> <i>(in radians)</i>
     * @param gamma angle describing <b>V</b> <i>(in radians)</i>
     * @param lvz sky luminance <b>Yz</b> at the zenith
     * @param thetaS angle describing <b>Vs</b> <i>(in radians)</i>
     * @return the sky luminance with float precision
     */
    public static float calcPerezFunction(float[] coeffs, float theta, float gamma, float lvz, float thetaS) {
        double A = coeffs[0], B = coeffs[1], C = coeffs[2], D = coeffs[3], E = coeffs[4];
        // num = perezF(coeffs, theta, gamma);
        // denum = perezF(coeffs, 0, thetaS);
        double num = (1 + A * Math.exp(B / Math.cos(theta))) * (1 + C * Math.exp(D * gamma) + E * Math.cos(gamma)*Math.cos(gamma));
        double den = (1 + A * Math.exp(B)) * (1 + C * Math.exp(D * thetaS) + E * Math.cos(thetaS)*Math.cos(thetaS));
        // return Yz * num / denum;
        return (float)(lvz * num / den);
    }

    private static double perezF(float[] coeffs, float theta, float gamma) {
        double A = coeffs[0], B = coeffs[1], C = coeffs[2], D = coeffs[3], E = coeffs[4];
        return (1 + A * Math.exp(B / Math.cos(theta))) * (1 + C * Math.exp(D * gamma) + E * Math.cos(gamma)*Math.cos(gamma));
    }

    /**
     * Function describing the <u>analytical semi-empirical model</u> called
     * <u>Phillips spectrum</u> which is a useful model for <u>wind-driven</u>
     * waves larger than capillary waves in a fully developed sea.<br/>
     * 
     * The Phillips spectrum has one parameter: the two-dimensional vector <b>k</b>
     * with the components (<b>kx</b>,<b>ky</b>).<br/>
     * <b>kx</b> is defined by 2*PI*<b>x</b>/<b>Lx</b> where <b>Lx</b> is the
     * <u>maximum x</u> in the wave height field and <b>x</b> is the <u>x-component</u>
     * of the current position in the wave height field in range (-<b>Lx</b>/2
     * to <b>Lx</b>/2).<br/>
     * <b>ky</b> is defined similar to <b>kx</b> but with the <u>maximum y</u>
     * and the <u>y-component</u>.
     * 
     * @param a the Phillips constant <i>(with a value around 0.0008)</i>
     * @param K <b>Vector2f</b> representing the one parameter the Phillips spectrum has
     * @param k the length of K
     * @param wind <b>Vector2f</b> containing wind direction and speed
     * @param convergence a factor which determines how height the waves are
     * @return a positive value indicating a "height"
     */
    public static double calcPhillipsFunction(float a, Vector2f K, float k, Vector2f wind, float convergence) {
        float k2 = K.lengthSquared();
        if(k2 == 0) {
            return 0;
        }
        float w2 = wind.lengthSquared();
        float L = w2/9.81f;
        // the factor *exp(-sqrt(k2)*1.0) can get rid of small waves by increasing 1.0
        float dot = K.dot(wind);
        double ret = a *
            (Math.exp(-1.0f/((k*L)*(k*L))) / (k*k*k*k)) *
            ((dot*dot) / (k2*w2)) *
            Math.exp(-k*convergence) *
            Math.exp(-k2*w2);
            //1;
//        double ret = a *
//            (Math.exp(-1/((kz*L)*(kz*L))) / (kz*kz*kz*kz)) *
//            ((k.x*wind.x+k.y*wind.y)*(k.x*wind.x+k.y*wind.y) / (kz*wz)) *
//            Math.exp(-Math.sqrt(kz)*convergence);
//            //Math.exp(-(kz*kz)*(wz*wz));
//            //1;
        // this value must be positive since we take the square root later
        return ret;
    }
}
