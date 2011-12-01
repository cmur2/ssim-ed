package sed.sky;

import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;

import com.jme3.math.FastMath;

public class SkyGradient {
    
	// night sky blend consts
    private static final float NightThetaRange = 6f; // in degrees
    private static final float NightThetaMin = 100f; // in degrees
    private static final float NightThetaMax = NightThetaMin+NightThetaRange; // in degrees

    private static final Color3f NightSkyColor = new Color3f(0f, 0f, 0.08f);
    // NightSun is the moon actually
    private static final Color3f NightSunColor = new Color3f(0.1f, 0.1f, 0.1f);

    // all determined by sunPosition()
    /**
     * Angle from zenith (top center of sky-dome) to current sun position.
     * If sun is:
     *  - at zenith this angle is 0 deg,
     *  - at horizon this angle is 90 deg
     */
    private float sunThetaAngle;
    private float sunPhiAngle;
    /**
     * meanings:
     *   1 - full night sky color
     *   0 - full perez colors (day light)
     */
    private float linearNightBlendFactor;

    private Vector3f vToSun = new Vector3f(); // in sky coordinates
    
    private float turbidity;
    private float exposureExponent = 0.05f; // std: 1/25
    
    private float zenithx, zenithy, zenithY; // the values at the suns position
    private float[] perezx, perezy, perezY;
    
    private Vector3f gammaCorrection = new Vector3f(1,0,1);
    
    public SkyGradient() {
	}
    
    public void setTurbidity(float t) {
        turbidity = t;
        perezY = new float[] { 0.17872f * t -1.46303f,
                              -0.35540f * t +0.42749f,
                              -0.02266f * t +5.32505f,
                               0.12064f * t -2.57705f,
                              -0.06696f * t +0.37027f
        };
        perezx = new float[] {-0.01925f * t -0.25922f,
                              -0.06651f * t +0.00081f,
                              -0.00041f * t +0.21247f,
                              -0.06409f * t -0.89887f,
                              -0.00325f * t +0.04517f
        };
        perezy = new float[] {-0.01669f * t -0.26078f,
                              -0.09495f * t +0.00921f,
                              -0.00792f * t +0.21023f,
                              -0.04405f * t -1.65369f,
                              -0.01092f * t +0.05291f
        };
    }
    public float getTurbidity() { return turbidity; }
    
    /**
     * Sets the sun's position according to the parameters given.
     * @param timeOfDay time of day <i>(12.25 ^= 12:15 PM)</i>
     * @param julianDay day in julian calendar <i>(0-365)</i>
     * @param latitude coordinate's latitude on the earth
     * @param standardMeridian time zone or standart meridian <i>(x in GMT+x)</i>
     * @param longitude coordinate's longitudee on the earth
     */
    public void updateSunPosition(double timeOfDay, int julianDay, double latitude, int standardMeridian, double longitude) {
        sunPosition(timeOfDay, julianDay, latitude, standardMeridian, longitude);
        vToSun.set((float) (Math.sin(sunThetaAngle)*Math.cos(sunPhiAngle)),
                   (float) (Math.sin(sunThetaAngle)*Math.sin(sunPhiAngle)),
                   (float) (Math.cos(sunThetaAngle)));
        vToSun.normalize();
        
        float theta2 = sunThetaAngle*sunThetaAngle, theta3 = theta2*sunThetaAngle;
        float t = turbidity;
        
        double chi = (4f/9f - t/120f) * (FastMath.PI - 2f*sunThetaAngle);
        zenithY = (float) ( (4.0453*t-4.9710)*Math.tan(chi) - 0.2155*t + 2.4192 ); // in Kcd/m^2
        // Auch ohne Konvertierung sieht es gut aus:
        //zenithY *= 1000;  // conversion from kcd/m^2 to cd/m^2
        
        zenithx = ( 0.00165f*theta3 - 0.00375f*theta2 + 0.00209f*sunThetaAngle + 0.00000f)*t*t +
                  (-0.02903f*theta3 + 0.06377f*theta2 - 0.03202f*sunThetaAngle + 0.00394f)*t +
                  ( 0.11693f*theta3 - 0.21196f*theta2 + 0.06052f*sunThetaAngle + 0.25886f);
        
        zenithy = ( 0.00275f*theta3 - 0.00610f*theta2 + 0.00317f*sunThetaAngle + 0.00000f)*t*t +
                  (-0.04214f*theta3 + 0.08970f*theta2 - 0.04153f*sunThetaAngle + 0.00516f)*t +
                  ( 0.15346f*theta3 - 0.26756f*theta2 + 0.06670f*sunThetaAngle + 0.26688f);
    }

    /**
     * Calculates the color of a point (parameter <b>vertex</b>) of a <u>skydome</u>
     * based on <u>Perez' model</u> and fills it in <b>f</b>.
     * @param colors the array the color is filled in
     * @param pointx point's x of the skydome
     * @param pointy point's y of the skydome
     * @param pointz point's z of the skydome
     */
    public void getSkycolor(float[] colors, double pointx, double pointy, double pointz) {
        if(linearNightBlendFactor >= 1f) {
            colors[0] = NightSkyColor.x;
            colors[1] = NightSkyColor.y;
            colors[2] = NightSkyColor.z;
            return;
        }
        
        double vVertexx = pointx, vVertexy = -pointz, vVertexz = pointy; // from J3D to Sky
        double norm = 1f/Math.sqrt(vVertexx*vVertexx + vVertexy*vVertexy + vVertexz*vVertexz);
        vVertexx *= norm; vVertexy *= norm; vVertexz *= norm;
        if(vVertexz < 0) { vVertexz *= -1; }
        float vertexThetaAngle = (vVertexz < 0.001) ? FastMath.PI/2f-0.001f : (float)Math.acos(vVertexz);
        float vertexPhiAngle = (vVertexx == 0.0 && vVertexy == 0.0) ? 0f : (float)Math.atan2(vVertexy, vVertexx);
        float gammaAngle = getAngleBetween(vertexThetaAngle, vertexPhiAngle, sunThetaAngle, sunPhiAngle);
        // Compute xyY values
        float x = calcPerezFunction(perezx, vertexThetaAngle, gammaAngle, zenithx, sunThetaAngle);
        float y = calcPerezFunction(perezy, vertexThetaAngle, gammaAngle, zenithy, sunThetaAngle);
        float Y = calcPerezFunction(perezY, vertexThetaAngle, gammaAngle, zenithY, sunThetaAngle);
        float X = x / y * Y;
        float Z = (1f-x-y) / y * Y;
        // standart Rec. 709 RGB with D65 white point (http://www.inforamp.net/%7Epoynton/notes/colour_and_gamma/ColorFAQ.html#RTFToC18)
        colors[0] =  3.240479f*X - 1.537150f*Y - 0.498535f*Z;
        colors[1] = -0.969256f*X + 1.875992f*Y + 0.041556f*Z;
        colors[2] =  0.055648f*X - 0.204043f*Y + 1.057311f*Z;
        // mix in the night sky color, if necessary
        colors[0] = (float) FastMath.interpolateLinear(linearNightBlendFactor, colors[0], NightSkyColor.x);
        colors[1] = (float) FastMath.interpolateLinear(linearNightBlendFactor, colors[1], NightSkyColor.y);
        colors[2] = (float) FastMath.interpolateLinear(linearNightBlendFactor, colors[2], NightSkyColor.z);
        //Vector3f lum = new Vector3f(x * (Y/y), Y, (1f-x-y) * (Y/y));
        //XYZtoRGBMatrix.transform(lum);
        //colors[0] = lum.x; colors[1] = lum.y; colors[2] = lum.z;
        colors[0] = 1f - (float)Math.exp(-exposureExponent*colors[0]);
        colors[1] = 1f - (float)Math.exp(-exposureExponent*colors[1]);
        colors[2] = 1f - (float)Math.exp(-exposureExponent*colors[2]);
        //System.out.println(colors[0]+" "+colors[1]+" "+colors[2]);
        colors[0] = (float) (gammaCorrection.y + gammaCorrection.z * Math.pow(colors[0], gammaCorrection.x));
        colors[1] = (float) (gammaCorrection.y + gammaCorrection.z * Math.pow(colors[1], gammaCorrection.x));
        colors[2] = (float) (gammaCorrection.y + gammaCorrection.z * Math.pow(colors[2], gammaCorrection.x));
        /*colors[0] = MathExt.clamp01(colors[0]);
        colors[1] = MathExt.clamp01(colors[1]);
        colors[2] = MathExt.clamp01(colors[2]);*/
        //brightness + contrast * pow(color.xyz, vec3(gamma,gamma,gamma))
    }
    
    private void sunPosition(double timeOfDay, int julianDay, double latitude, int standardMeridian, double longitude) {
        // solarTime in Stunden
        double solarTime = timeOfDay +
            (0.170 * Math.sin(4d*Math.PI*(julianDay-80d)/373d) - 0.129*Math.sin(2d*Math.PI*(julianDay-8d)/355d)) +
            (standardMeridian-longitude)/15d; // Zeitdifferenz zwischen 2 Meridianen betraegt 4 min, 15 Meridiane = 60 min = eine Stunde
        double solarDeclination = 0.4093 * Math.sin(2d*Math.PI*(julianDay-81d)/368d);
        latitude = Math.toRadians(latitude);
        double temp = Math.cos(solarDeclination) * Math.cos(Math.PI*solarTime/12d);
        double opp = -(Math.cos(solarDeclination) * Math.sin(Math.PI*solarTime/12d));
        double adj = -(Math.cos(latitude)*Math.sin(solarDeclination) + Math.sin(latitude)*temp);
        sunPhiAngle = -(float)Math.atan2(opp, adj)-FastMath.PI/2f;   // solarAzimuth = Math.atan2(opp, adj)
        double solarAltitude = Math.asin(Math.sin(latitude)*Math.sin(solarDeclination) - Math.cos(latitude)*temp); // height angle
        sunThetaAngle = (float)(0.5*Math.PI - solarAltitude); // zenith angle

        // extension to allow blend to night sky color
        float thetaDeg = (float) Math.toDegrees(sunThetaAngle);
        if(thetaDeg < NightThetaMin) {
            linearNightBlendFactor = 0f;
        } else if(thetaDeg > NightThetaMax) {
            linearNightBlendFactor = 1f;
        } else {
            linearNightBlendFactor = (thetaDeg-NightThetaMin)/NightThetaRange;
        }
//        System.out.println(linearNightBlendFactor);
    }
    
    /**
     * Calculates angle named <b>gamma</b> or <b>psi</b> in a <u>skydome</u>
     * between two vectors comming from the skydome's origin which are defined
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
        else if(cospsi < -1.0) { return FastMath.PI; }
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
}
