package sed;

import java.io.PrintStream;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class Util {
    
    /**
     * Apply the given rgba (from array[0] to array[3]) data to the color.
     *  
     * @param c the target
     * @param rgba delivers rgba data
     * @return the target
     */
    public static ColorRGBA setTo(ColorRGBA c, float[] rgba) {
        c.set(rgba[0], rgba[1], rgba[2], rgba[3]);
        return c;
    }
    
    /**
     * Apply the given xyz (from vector) and alpha data to the color.
     *  
     * @param c the target
     * @param v delivers xyz/rgb data
     * @param alpha delivers alpha data
     * @return the target
     */
    public static ColorRGBA setTo(ColorRGBA c, Vector3f v, float alpha) {
        c.set(v.x, v.y, v.z, alpha);
        return c;
    }
    
    /**
     * Insert given rgba (from color) into array at given offset. 
     * 
     * @param f the target
     * @param offset index offset to begin filling
     * @param c delivers rgba
     */
    public static void setTo(float[] f, int offset, ColorRGBA c) {
        f[offset+0] = c.r;
        f[offset+1] = c.g;
        f[offset+2] = c.b;
        f[offset+3] = c.a;
    }
    
    /**
     * Prints the scenegraph {@linkplain Node}s below the given root node one
     * node per line with increasing indent to {@link System#out}.
     * 
     * @param root root node
     */
    public static void printSceneGraph(Node root) {
        printSceneGraph(root, System.out);
    }
    
    /**
     * Prints the scenegraph {@linkplain Node}s below the given root node one
     * node per line with increasing indent to given {@link PrintStream}.
     * 
     * @param root root node
     */
    public static void printSceneGraph(Node root, PrintStream p) {
        printSceneGraph(root, 0, p);
    }
    
    private static void printSceneGraph(Spatial root, int indent, PrintStream p) {
        String s = "";
        for(int i = 0; i < indent; i++) {
            s += "  ";
        }
        p.println(s + root.toString());
        if(!(root instanceof Node))
            return;
        for(Spatial c : ((Node) root).getChildren()) {
            printSceneGraph(c, indent + 1, p);
        }
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
