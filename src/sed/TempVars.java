package sed;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

/**
 * Nice idea copied from {@link com.jme3.util.TempVars} to solve my problems
 * with temporary object creation. Currently not used.
 * 
 * @author cn
 */
public class TempVars {
    
    private static final int STACK_SIZE = 5;
    
    private static class TempVarsStack {
        int index = 0;
        TempVars[] tempVars = new TempVars[STACK_SIZE];
    }
    
    private static final ThreadLocal<TempVarsStack> varsLocal = new ThreadLocal<TempVarsStack>() {
        @Override
        public TempVarsStack initialValue() {
            return new TempVarsStack();
        }
    };
    
    private boolean isUsed = false;
    
    private TempVars() {
    }
    
    public void release() {
        if(!isUsed) {
            throw new IllegalStateException(
                "This instance of TempVars was already released!");
        }
        isUsed = false;
        TempVarsStack stack = varsLocal.get();
        stack.index--;
        if(stack.tempVars[stack.index] != this) {
            throw new IllegalStateException(
                "An instance of TempVars has not been released in a called method!");
        }
    }
    
    public final Vector3f va = new Vector3f();
    public final Vector3f vb = new Vector3f();
    public final Vector3f vc = new Vector3f();
    public final Vector3f vd = new Vector3f();
    
    public final Vector2f v2a = new Vector2f();
    public final Vector2f v2b = new Vector2f();
    
    public final ColorRGBA ca = new ColorRGBA();
    public final ColorRGBA cb = new ColorRGBA();
    
    /**
     * @return a variable set
     */
    public static TempVars get() {
        TempVarsStack stack = varsLocal.get();
        TempVars instance = stack.tempVars[stack.index];
        if(instance == null) {
            instance = new TempVars();
            stack.tempVars[stack.index] = instance;
        }
        stack.index++;
        instance.isUsed = true;
        return instance;
    }
}
