package sed.sky;

import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;

public class SunQuad extends Mesh {
    
    public SunQuad(float size) {
        setBuffer(Type.Position, 3,
            new float[] {-size,-size,0, size,-size,0, size, size,0, -size, size,0});
        setBuffer(Type.TexCoord, 2, new float[] {0,0, 1,0, 1,1, 0,1});
        setBuffer(Type.Normal, 3, new float[] {0,0,1, 0,0,1, 0,0,1, 0,0,1});
        setBuffer(Type.Index, 3, new short[] {0,1,2, 0,2,3});
        updateBound();
    }
}
