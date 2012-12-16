package de.mycrobase.ssim.ed.mesh;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class OceanBorder extends Mesh {
    
    private float sizeX;
    private float sizeZ;
    private float innerSizeX;
    private float innerSizeZ;
    
    public OceanBorder(float sizeX, float sizeZ, float innerSizeX, float innerSizeZ) {
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
        this.innerSizeX = innerSizeX;
        this.innerSizeZ = innerSizeZ;
        
        initGeometry();
    }
    
    // helper
    
    private void initGeometry() {
        int numVertices = 8;
        
        Vector3f[] positions = new Vector3f[numVertices];
        positions[0] = new Vector3f(-innerSizeX/2, 0, -innerSizeZ/2);
        positions[1] = new Vector3f(+innerSizeX/2, 0, -innerSizeZ/2);
        positions[2] = new Vector3f(+innerSizeX/2, 0, +innerSizeZ/2);
        positions[3] = new Vector3f(-innerSizeX/2, 0, +innerSizeZ/2);

        positions[4] = new Vector3f(-sizeX/2, 0, -sizeZ/2);
        positions[5] = new Vector3f(+sizeX/2, 0, -sizeZ/2);
        positions[6] = new Vector3f(+sizeX/2, 0, +sizeZ/2);
        positions[7] = new Vector3f(-sizeX/2, 0, +sizeZ/2);
        
        setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(positions));
        
        Vector3f[] normals = new Vector3f[numVertices];
        for(int i = 0; i < normals.length; i++) {
            normals[i] = new Vector3f(0f, 1f, 0f);
        }
        
        setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        
        setBuffer(Type.Index, 1, new short[] {4,0,1, 4,1,5, 5,1,2, 5,2,6, 6,2,3, 6,3,7, 7,3,0, 7,0,4});
        // for debugging only +x and -x sides:
        //setBuffer(Type.Index, 1, new short[] {4,0,1, 4,1,5, 6,2,3, 6,3,7});
        
        updateBound();
    }
    
}
