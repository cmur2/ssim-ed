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
}
