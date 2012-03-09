package sed.sky;

import sed.TempVars;
import sed.Util;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

public class SkyGradient {

    /*private final Matrix3f XYZtoRGBMatrix =
    new Matrix3f( 3.240479f,-1.537150f,-0.498535f,
                 -0.969256f, 1.875992f, 0.041556f,
                  0.055648f,-0.204043f, 1.057311f);*/
    
    // night sky blend constants
    public static final float NightThetaRange = 6f; // in degrees
    public static final float NightThetaMin = 100f; // in degrees
    public static final float NightThetaMax = NightThetaMin + NightThetaRange; // in degrees
    
    // a dark blue for the sky
    public static final ColorRGBA NightSkyColor = new ColorRGBA(0f, 0f, 0.08f, 1f);
    
    // since the sun is below the horizon it should actually be black
    public static final ColorRGBA NightSunColor = ColorRGBA.Black;
    
    private Sun sun;
    
    /**
     * x: sunPhiAngle, y: sunThetaAngle
     */
    private Vector2f sunAngles;
    
    /**
     * meanings:
     *   1 - full night sky color
     *   0 - full Perez colors (day light)
     */
    private float linearNightBlendFactor;
    
    private float turbidity;
    private float exposureExponent = 0.05f; // std: 1/25
    
    private float zenithx, zenithy, zenithY; // the values at the suns position
    private float[] perezx, perezy, perezY;
    
    private Vector3f gammaCorrection = new Vector3f(1f, 0f, 1f);
    
    public SkyGradient(Sun sun) {
        this.sun = sun;
    }
    
    public void setTurbidity(float turbidity) {
        this.turbidity = turbidity;
    }
    
    public float getTurbidity() {
        return turbidity;
    }
    
    public void setExposureExponent(float exposureExponent) {
        this.exposureExponent = exposureExponent;
    }
    
    public float getExposureExponent() {
        return exposureExponent;
    }
    
    public void update() {
        sunAngles = sun.getSunAngles(sunAngles);
        
        // extension to allow blend to night sky color
        float thetaDeg = (float) Math.toDegrees(sunAngles.y);
        if(thetaDeg < NightThetaMin) {
            linearNightBlendFactor = 0f;
        } else if(thetaDeg > NightThetaMax) {
            linearNightBlendFactor = 1f;
        } else {
            linearNightBlendFactor = (thetaDeg-NightThetaMin)/NightThetaRange;
        }
        //System.out.println(linearNightBlendFactor);
        
        // @formatter:off
        float t = turbidity;
        perezY = new float[] {
             0.17872f * t -1.46303f,
            -0.35540f * t +0.42749f,
            -0.02266f * t +5.32505f,
             0.12064f * t -2.57705f,
            -0.06696f * t +0.37027f
        };
        perezx = new float[] {
            -0.01925f * t -0.25922f,
            -0.06651f * t +0.00081f,
            -0.00041f * t +0.21247f,
            -0.06409f * t -0.89887f,
            -0.00325f * t +0.04517f
        };
        perezy = new float[] {
            -0.01669f * t -0.26078f,
            -0.09495f * t +0.00921f,
            -0.00792f * t +0.21023f,
            -0.04405f * t -1.65369f,
            -0.01092f * t +0.05291f
        };
        
        float theta2 = sunAngles.y * sunAngles.y;
        float theta3 = theta2 * sunAngles.y;
        double chi = (4f/9f - t/120f) * (FastMath.PI - 2f*sunAngles.y);
        zenithY = (float) ((4.0453*t - 4.9710) * Math.tan(chi) - 0.2155*t + 2.4192); // in Kcd/m^2
        // looks good even without correction:
        //zenithY *= 1000; // conversion from kcd/m^2 to cd/m^2
        zenithx = ( 0.00165f*theta3 - 0.00375f*theta2 + 0.00209f*sunAngles.y + 0.00000f)*t*t +
                  (-0.02903f*theta3 + 0.06377f*theta2 - 0.03202f*sunAngles.y + 0.00394f)*t +
                  ( 0.11693f*theta3 - 0.21196f*theta2 + 0.06052f*sunAngles.y + 0.25886f);
        zenithy = ( 0.00275f*theta3 - 0.00610f*theta2 + 0.00317f*sunAngles.y + 0.00000f)*t*t +
                  (-0.04214f*theta3 + 0.08970f*theta2 - 0.04153f*sunAngles.y + 0.00516f)*t +
                  ( 0.15346f*theta3 - 0.26756f*theta2 + 0.06670f*sunAngles.y + 0.26688f);
        // @formatter:on
    }
    
    /**
     * Calculates the color of the sun light (= color of the point on the
     * hemisphere where the sun is located) via
     * {@link #getSkycolor(float[], double, double, double)} and brightens it
     * proportional so that the highest component will be 1.0.
     * 
     * @param store a {@link ColorRGBA} to store the result
     * @return the sun light color
     */
    public ColorRGBA getSunLightColor(ColorRGBA store) {
        if(store == null) {
            store = new ColorRGBA();
        }
        
        if(linearNightBlendFactor >= 1f) {
            store.set(NightSunColor);
            return store;
        }
        
        TempVars vars = TempVars.get();
        
        Vector3f sunPosition = sun.getSunPosition(vars.vect1);
        float[] sunColorArray = getSkyColor(sunPosition, vars.float1);
        Util.setTo(store, sunColorArray);
        // brighten the color's rgb proportional, reset alpha
        store.multLocal(1f/Util.getMaxComponent(store));
        store.a = 1f;
        store.interpolate(NightSunColor, linearNightBlendFactor);
        
        // TODO: for zenith-near sun positions the sun light color is to bluish
        
        vars.release();
        
        return store;
    }
    
    /**
     * Adapter to {@link #getSkycolor(float[], double, double, double)}.
     * 
     * @param p point (j3d coords)
     * @param store a float[] to store the result
     * @return the sky color at the given point as RGBA (alpha is 1) in float[]
     */
    public float[] getSkyColor(Vector3f p, float[] store) {
        return getSkyColor(p.x, p.y, p.z, store);
    }
    
    /**
     * Adapter to {@link #getSkycolor(float[], double, double, double)}.
     * 
     * @param px point's x coordinate (j3d coords)
     * @param py point's y coordinate (j3d coords)
     * @param pz point's z coordinate (j3d coords)
     * @param store a float[] to store the result
     * @return the sky color at the given point as RGBA (alpha is 1) in float[]
     */
    public float[] getSkyColor(float px, float py, float pz, float[] store) {
        if(store == null) {
            store = new float[4];
        }
        getSkycolor(store, px, py, pz);
        store[3] = 1f;
        return store;
    }
    
    // TODO: deal with that sky to j3d coordinate conversions
    
    /**
     * Calculates the color of a given point on a hemisphere
     * based on Perez' model and fills the results in the given color array.
     * 
     * @param colors the array the result is filled in
     * @param pointx point's x coordinate (j3d coords)
     * @param pointy point's y coordinate (j3d coords)
     * @param pointz point's z coordinate (j3d coords)
     */
    public void getSkycolor(float[] colors,
        double pointx, double pointy, double pointz)
    {
        if(linearNightBlendFactor >= 1f) {
            colors[0] = NightSkyColor.r;
            colors[1] = NightSkyColor.g;
            colors[2] = NightSkyColor.b;
            return;
        }
        // calculate direction vector onto hemisphere
        double vVertexx = pointx, vVertexy = -pointz, vVertexz = pointy; // from J3D to Sky
        double norm = 1d/Math.sqrt(vVertexx*vVertexx + vVertexy*vVertexy + vVertexz*vVertexz);
        vVertexx *= norm;
        vVertexy *= norm;
        vVertexz *= norm;
        // mirror upper hemisphere to the lower for ocean
        vVertexz = Math.abs(vVertexz);
        // @formatter:off
        float vertexThetaAngle =
            (vVertexz < 0.001) ? FastMath.PI/2f-0.001f :
                (float) Math.acos(vVertexz);
        float vertexPhiAngle =
            (vVertexx == 0.0 && vVertexy == 0.0) ? 0f :
                (float) Math.atan2(vVertexy, vVertexx);
        float gammaAngle = getAngleBetween(vertexThetaAngle, vertexPhiAngle, sunAngles.y, sunAngles.x);
        // compute xyY (and XYZ) values
        float x = calcPerezFunction(perezx, vertexThetaAngle, gammaAngle, zenithx, sunAngles.y);
        float y = calcPerezFunction(perezy, vertexThetaAngle, gammaAngle, zenithy, sunAngles.y);
        float Y = calcPerezFunction(perezY, vertexThetaAngle, gammaAngle, zenithY, sunAngles.y);
        float X = x / y * Y;
        float Z = (1f-x-y) / y * Y;
        // XYZ to RGB via standard Rec. 709 RGB with D65 white point
        // (http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC18)
        colors[0] =  3.240479f*X - 1.537150f*Y - 0.498535f*Z;
        colors[1] = -0.969256f*X + 1.875992f*Y + 0.041556f*Z;
        colors[2] =  0.055648f*X - 0.204043f*Y + 1.057311f*Z;
        // alternate solution:
        //Vector3f lum = new Vector3f(x * (Y/y), Y, (1f-x-y) * (Y/y));
        //XYZtoRGBMatrix.transform(lum);
        //colors[0] = lum.x; colors[1] = lum.y; colors[2] = lum.z;
        // mix in the night sky color, if necessary
        colors[0] = (float) FastMath.interpolateLinear(linearNightBlendFactor, colors[0], NightSkyColor.r);
        colors[1] = (float) FastMath.interpolateLinear(linearNightBlendFactor, colors[1], NightSkyColor.g);
        colors[2] = (float) FastMath.interpolateLinear(linearNightBlendFactor, colors[2], NightSkyColor.b);
        // apply exposure control
        colors[0] = 1f - (float) Math.exp(-exposureExponent*colors[0]);
        colors[1] = 1f - (float) Math.exp(-exposureExponent*colors[1]);
        colors[2] = 1f - (float) Math.exp(-exposureExponent*colors[2]);
        // apply gamma correction:
        // (= brightness + contrast * pow(color.xyz, vec3(gamma,gamma,gamma)))
        colors[0] = (float) (gammaCorrection.y + gammaCorrection.z * Math.pow(colors[0], gammaCorrection.x));
        colors[1] = (float) (gammaCorrection.y + gammaCorrection.z * Math.pow(colors[1], gammaCorrection.x));
        colors[2] = (float) (gammaCorrection.y + gammaCorrection.z * Math.pow(colors[2], gammaCorrection.x));
        /*colors[0] = MathExt.clamp01(colors[0]);
        colors[1] = MathExt.clamp01(colors[1]);
        colors[2] = MathExt.clamp01(colors[2]);*/
        // @formatter:on
    }
    
    /**
     * Calculates angle named <b>gamma</b> or <b>psi</b> in a <u>skydome</u>
     * between two vectors coming from the skydome's origin which are defined
     * by <b>theta1</b>/<b>phi1</b> and <b>theta2</b>/<b>phi2</b>.
     * 
     * @param theta1 theta for vector one <i>(in radians)</i>
     * @param phi1 phi for vector one <i>(in radians)</i> degrees
     * @param theta2 theta for vector <i>(in radians)</i>
     * @param phi2 phi for vector two <i>(in radians)</i>
     * @return angle named <b>gamma</b> or <b>psi</b> with float precision <i>(in radians)</i>
     */
    public static float getAngleBetween(float theta1, float phi1, float theta2, float phi2) {
        double cospsi = Math.sin(theta1) * Math.sin(theta2) * Math.cos(phi2 - phi1)
                + Math.cos(theta1) * Math.cos(theta2);
        if(cospsi > 1.0) {
            return 0f;
        } else if(cospsi < -1.0) {
            return FastMath.PI;
        }
        return (float) Math.acos(cospsi);
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
    public static float calcPerezFunction(float[] coeffs, float theta, float gamma, float lvz,
            float thetaS) {
        double A = coeffs[0], B = coeffs[1], C = coeffs[2], D = coeffs[3], E = coeffs[4];
        // num = perezF(coeffs, theta, gamma);
        // denum = perezF(coeffs, 0, thetaS);
        double num = (1 + A * Math.exp(B / Math.cos(theta)))
                * (1 + C * Math.exp(D * gamma) + E * Math.cos(gamma) * Math.cos(gamma));
        double den = (1 + A * Math.exp(B))
                * (1 + C * Math.exp(D * thetaS) + E * Math.cos(thetaS) * Math.cos(thetaS));
        // return Yz * num / denum;
        return (float) (lvz * num / den);
    }
    
    private static double perezF(float[] coeffs, float theta, float gamma) {
        double A = coeffs[0], B = coeffs[1], C = coeffs[2], D = coeffs[3], E = coeffs[4];
        return (1 + A * Math.exp(B / Math.cos(theta)))
                * (1 + C * Math.exp(D * gamma) + E * Math.cos(gamma)
                        * Math.cos(gamma));
    }
}
