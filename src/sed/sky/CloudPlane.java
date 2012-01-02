package sed.sky;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class CloudPlane extends Mesh {

    private static final int FragmentDivisions = 8;
    private static final int VertexNum = FragmentDivisions+1;
    private static final float PlaneSize = 12000; // in m

    private static final float PlaneY = 2000; // in m

    private static final float DivisionGeomDelta = PlaneSize / FragmentDivisions;
    private static final float DivisionTexDelta = 1f / FragmentDivisions;
    private static final float PlaneDeltaHeight = 800; // in m

    public CloudPlane() {
        initGeometry();
    }

    private void initGeometry() {
        int numVertices = FragmentDivisions * FragmentDivisions * 4;
        
        Vector3f[][] positions = genCurvedPlane();
        Vector2f[][] texCoords = genCurvedPlaneTC();
        
        Vector3f[] posFinal = new Vector3f[numVertices];
        Vector2f[] tcFinal = new Vector2f[numVertices];
        
        int idx = 0;
        for(int i = 0; i < FragmentDivisions; i++) {
            for(int j = 0; j < FragmentDivisions; j++) {
                posFinal[idx] = positions[i][j];
                tcFinal[idx] = texCoords[i][j];
                idx++;
                posFinal[idx] = positions[i][j+1];
                tcFinal[idx] = texCoords[i][j+1];
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
    
    private static Vector3f[][] genCurvedPlane() {
        Vector3f[][] coords = new Vector3f[VertexNum][VertexNum];
        
        float x_height_max = (float) Math.pow(0.5f * PlaneSize*0.07, 2.0) / PlaneDeltaHeight;
        float z_height_max = (float) Math.pow(0.5f * PlaneSize*0.07, 2.0) / PlaneDeltaHeight;
        
        for(int i = 0; i < VertexNum; i++) { // x
            for(int j = 0; j < VertexNum; j++) { // -z
            float x_dist = -0.5f*PlaneSize + i*DivisionGeomDelta;
            float z_dist = +0.5f*PlaneSize - j*DivisionGeomDelta;
            float x_height = (float)Math.pow(x_dist*0.07, 2.0)/PlaneDeltaHeight;
            float z_height = (float)Math.pow(z_dist*0.07, 2.0)/PlaneDeltaHeight;
            float h = ( (1f-x_height/x_height_max)*(1f-z_height/z_height_max) )*4000f;
            //float h = ( (x_height_max-x_height)*(z_height_max-z_height) )*0.005f;
            coords[i][j] = new Vector3f(x_dist, PlaneY+h, z_dist);
            }
        }
        
        return coords;
    }
    
    private static Vector2f[][] genCurvedPlaneTC() {
        Vector2f[][] texCoords = new Vector2f[VertexNum][VertexNum];
        for(int i = 0; i < VertexNum; i++) { // x
            for(int j = 0; j < VertexNum; j++) { // -z
              texCoords[i][j] = new Vector2f(i, j);
              texCoords[i][j].mult(DivisionTexDelta);
            }
        }
        return texCoords;
    }
    
}
