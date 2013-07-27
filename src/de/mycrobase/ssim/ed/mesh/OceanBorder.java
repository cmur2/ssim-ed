package de.mycrobase.ssim.ed.mesh;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class OceanBorder extends Mesh {
    
    private static final float TexCoordScale = 1f;
    
    private float sizeX;
    private float sizeZ;
    private float innerSizeX;
    private float innerSizeZ;
    private float texCoordPerMeter;
    private float texCoordScale;
    
    public OceanBorder(float sizeX, float sizeZ, float innerSizeX,
            float innerSizeZ, float texCoordPerMeter, float texCoordScale) {
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
        this.innerSizeX = innerSizeX;
        this.innerSizeZ = innerSizeZ;
        this.texCoordPerMeter = texCoordPerMeter;
        this.texCoordScale = texCoordScale;
        
        initGeometry();
    }
    
    // helper
    
    private void initGeometry() {
        int numVertices = 8;
        
        // needed for deriving texture coordinates later
        Vector3f[] positions = new Vector3f[numVertices];
        
        {
            //Vector3f[] positions = new Vector3f[numVertices];
            positions[0] = new Vector3f(-innerSizeX/2, 0, -innerSizeZ/2);
            positions[1] = new Vector3f(+innerSizeX/2, 0, -innerSizeZ/2);
            positions[2] = new Vector3f(+innerSizeX/2, 0, +innerSizeZ/2);
            positions[3] = new Vector3f(-innerSizeX/2, 0, +innerSizeZ/2);

            positions[4] = new Vector3f(-sizeX/2, 0, -sizeZ/2);
            positions[5] = new Vector3f(+sizeX/2, 0, -sizeZ/2);
            positions[6] = new Vector3f(+sizeX/2, 0, +sizeZ/2);
            positions[7] = new Vector3f(-sizeX/2, 0, +sizeZ/2);
            setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(positions));
        }
        
        {
            Vector3f[] normals = new Vector3f[numVertices];
            for(int i = 0; i < normals.length; i++) {
                normals[i] = Vector3f.UNIT_Y;
            }
            setBuffer(Type.Normal, 3, BufferUtils.createFloatBuffer(normals));
        }
        
        {
            Vector2f[] texCoords = new Vector2f[numVertices];
            
            // derive texture coordinates from physical dimensions via texCoordPerMeter
            // resolution for all vertices
            for(int i = 0; i < texCoords.length; i++) {
                texCoords[i] = new Vector2f();
                texCoords[i].x = (positions[i].x + innerSizeX/2) * texCoordPerMeter;
                texCoords[i].y = (positions[i].z + innerSizeZ/2) * texCoordPerMeter;
                // apply normal texture coordinate scale
                texCoords[i].multLocal(texCoordScale);
            }
            
            setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));
        }
        
        {
            Vector3f[] tangents = new Vector3f[numVertices];
            for(int i = 0; i < tangents.length; i++) {
                tangents[i] = Vector3f.UNIT_X;
            }
            setBuffer(Type.Tangent, 3, BufferUtils.createFloatBuffer(tangents));
        }
        
        {
            setBuffer(Type.Index, 1, new short[] {4,0,1, 4,1,5, 5,1,2, 5,2,6, 6,2,3, 6,3,7, 7,3,0, 7,0,4});
            // for debugging only +x and -x sides:
            //setBuffer(Type.Index, 1, new short[] {4,0,1, 4,1,5, 6,2,3, 6,3,7});
        }
        
        updateBound();
    }
    
}
