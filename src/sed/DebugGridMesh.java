package sed;

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
        int n = 0;
        for(int i = -gridWidth; i <= gridWidth; i++) {
            p[n] = new Vector3f(i*gridSize/gridWidth,0, gridSize);
            n++;
            p[n] = new Vector3f(i*gridSize/gridWidth,0,-gridSize);
            n++;
        }
        for(int i = -gridHeight; i <= gridHeight; i++) {
            p[n] = new Vector3f( gridSize,0,i*gridSize/gridHeight);
            n++;
            p[n] = new Vector3f(-gridSize,0,i*gridSize/gridHeight);
            n++;
        }
        setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(p));
        setMode(Mode.Lines);
        updateBound();
    }
}
