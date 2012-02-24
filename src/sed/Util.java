package sed;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;

public class Util {
    
    public static ColorRGBA setTo(ColorRGBA c, float[] rgba) {
        c.set(rgba[0], rgba[1], rgba[2], rgba[3]);
        return c;
    }
    
    public static ColorRGBA setTo(ColorRGBA c, Vector3f v, float alpha) {
        c.set(v.x, v.y, v.z, alpha);
        return c;
    }
    
    /**
     * Since byte is a signed type you cannot receive the unsigned value
     * even if you do {@code (byte) 200}. This method will return {@code 200}
     * in this case instead of a negative one.
     * <p>
     * Applied mapping:
     * [0..127] to [0..127] and [-128..-1] to [128..255] 
     * 
     * @param b value in range -128 to 127
     * @return value in range 0 to 255
     */
    public static int unpackUnsignedByte(byte b) {
        // byte: 0 - 127 => 0 - 127, 128 - 255 => -128 - -1
        return b >= 0 ? b : b+256;
    }
}
