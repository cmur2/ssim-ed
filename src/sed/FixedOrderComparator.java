package sed;

import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.GeometryComparator;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;

/**
 * Reads the {@link #ORDER_INDEX} user data field of both {@link Geometry}s to
 * compare and sorts inverse to {@link Integer#compareTo(Integer)}.
 * That means that low order indices are at the end of the {@link RenderQueue}
 * and drawn last (on top) onto the FrameBuffer so they should be the nearest
 * to the viewer.
 * 
 * @author cn
 */
public class FixedOrderComparator implements GeometryComparator {
    
    public static final String ORDER_INDEX = "orderIndex";
    
    public int compare(Geometry o1, Geometry o2) {
        Integer layer1 = o1.getUserData(ORDER_INDEX);
        Integer layer2 = o2.getUserData(ORDER_INDEX);
        // negate the result, exchanging parameters is equivalent but faster:
        //return -layer1.compareTo(layer2);
        return layer2.compareTo(layer1);
    }
    
    public void setCamera(Camera cam) {
    }
}
