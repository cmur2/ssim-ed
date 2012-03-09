package sed.sky;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class CloudPlane extends Mesh {

    private static final int FragmentDivisions = 8;
    private static final int VertexNum = FragmentDivisions+1;

    /**
     * Size per dimension, so width and height are equal.
     */
    private float planeSize;
    /**
     * Y extent of the (arched) plane.
     */
    private float heightScale;
    /**
     * Describes how the center of the plane (at bottom) is translated from
     * models origin.
     */
    private Vector3f translation;
    
    public CloudPlane(float planeSize, float heightScale, Vector3f translation) {
        this.planeSize = planeSize;
        this.heightScale = heightScale;
        this.translation = translation;
        initGeometry();
    }

    private void initGeometry() {
        Vector3f[][] positions = genCurvedPlane(VertexNum, planeSize, heightScale);
        Vector2f[][] texCoords = genCurvedPlaneTC(VertexNum);
        
        for(int i = 0; i < VertexNum; i++) { // x
            for(int j = 0; j < VertexNum; j++) { // -z
                positions[i][j].addLocal(translation);
            }
        }
        
        int numVertices = FragmentDivisions * FragmentDivisions * 2 * 3;
        Vector3f[] posFinal = new Vector3f[numVertices];
        Vector2f[] tcFinal = new Vector2f[numVertices];
        
        int idx = 0;
        for(int i = 0; i < FragmentDivisions; i++) {
            for(int j = 0; j < FragmentDivisions; j++) {
                // build each quad face out of two triangles
                posFinal[idx] = positions[i][j];
                tcFinal[idx] = texCoords[i][j];
                idx++;
                posFinal[idx] = positions[i][j+1];
                tcFinal[idx] = texCoords[i][j+1];
                idx++;
                posFinal[idx] = positions[i+1][j+1];
                tcFinal[idx] = texCoords[i+1][j+1];
                idx++;
                
                posFinal[idx] = positions[i][j];
                tcFinal[idx] = texCoords[i][j];
                idx++;
                posFinal[idx] = positions[i+1][j+1];
                tcFinal[idx] = texCoords[i+1][j+1];
                idx++;
                posFinal[idx] = positions[i+1][j];
                tcFinal[idx] = texCoords[i+1][j];
                idx++;
            }
        }
        
        setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(posFinal));
        setBuffer(Type.TexCoord, 2, BufferUtils.createFloatBuffer(tcFinal));
        updateBound();
    }
    
    private static Vector3f[][] genCurvedPlane(
            int numVertices, float planeSize, float heightScale)
    {
        Vector3f[][] coords = new Vector3f[numVertices][numVertices];
        
        float planeSizeHalf = .5f * planeSize;
        
        float x_height_max = (float) Math.pow(planeSizeHalf, 2.0);
        float z_height_max = (float) Math.pow(planeSizeHalf, 2.0);
        
        float indexScale = planeSize / (numVertices-1);
        
        for(int i = 0; i < numVertices; i++) { // x
            for(int j = 0; j < numVertices; j++) { // -z
                float x_dist = -planeSizeHalf + indexScale * i;
                float z_dist = +planeSizeHalf - indexScale * j;
                
                float x_height = (float) Math.pow(x_dist, 2.0);
                float z_height = (float) Math.pow(z_dist, 2.0);
                
                float h = (1f-x_height/x_height_max) * (1f-z_height/z_height_max);
                //float h = (x_height_max-x_height)*(z_height_max-z_height*0.005f;
                
                coords[i][j] = new Vector3f(x_dist, h*heightScale, z_dist);
            }
        }
        
        return coords;
    }
    
    private static Vector2f[][] genCurvedPlaneTC(int numVertices) {
        Vector2f[][] texCoords = new Vector2f[numVertices][numVertices];
        float texCoordScale = 1f / (numVertices-1);
        for(int i = 0; i < numVertices; i++) { // x
            for(int j = 0; j < numVertices; j++) { // -z
                texCoords[i][j] = new Vector2f(i, j);
                texCoords[i][j].multLocal(texCoordScale);
            }
        }
        return texCoords;
    }
}
