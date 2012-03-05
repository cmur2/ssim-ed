package sed;


import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class DebugGridMesh extends Mesh {
    
    // all values per dimension
    private float gridSize;
    private int gridWidth;
    private int gridHeight;
    
    public DebugGridMesh(float gridSize, int gridWidth, int gridHeight) {
        this.gridSize = gridSize;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        initGeometry();
    }
    
    private void initGeometry() {
        int numGridLines = gridWidth*2+1 + gridHeight*2+1;
        Vector3f[] p = new Vector3f[numGridLines * 2];
        float[] c = new float[numGridLines * 2 * 4];
        
        int n = 0;
        
        for(int i = -gridWidth; i <= gridWidth; i++) {
            p[n] = new Vector3f(i*gridSize/gridWidth,0, gridSize);
            Util.setTo(c, n*4, ColorRGBA.Gray);
            n++;
            p[n] = new Vector3f(i*gridSize/gridWidth,0,-gridSize);
            Util.setTo(c, n*4, ColorRGBA.Gray);
            n++;
        }
        
        for(int i = -gridHeight; i <= gridHeight; i++) {
            p[n] = new Vector3f( gridSize,0,i*gridSize/gridHeight);
            Util.setTo(c, n*4, ColorRGBA.Gray);
            n++;
            p[n] = new Vector3f(-gridSize,0,i*gridSize/gridHeight);
            Util.setTo(c, n*4, ColorRGBA.Gray);
            n++;
        }
        
        setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(p));
        setBuffer(Type.Color, 4, c);
        setMode(Mode.Lines);
        updateBound();
    }
}
