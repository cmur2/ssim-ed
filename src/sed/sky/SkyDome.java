package sed.sky;

import ssim.util.MathExt;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class SkyDome extends Mesh {
    
    public SkyDome() {
        float dtheta = 2;
        float dphi = 2;
        Vector3f[] p = genDome(100f, dtheta, dphi);
        int[] indices = genDomeIndices(dtheta, dphi);
        
        setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(p));
        setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(indices));
        updateBound();
        
        //setMode(Mode.Points);
    }
    
    private static Vector3f[] genDome1(float radius, float dtheta, float dphi) {
        // 360/dphi * 90/dtheta quads
        // 360/dphi * 90/dtheta * 2 triangles
        // 360/dphi * 90/dtheta * 2 * 3 vertices
        Vector3f[] vs = new Vector3f[(int) ((360d / dphi) * (90d / dtheta) * 6d)];
        int n = 0;
        for(double phi = 0; phi <= 360 - dphi; phi += dphi) {
            float sindphi = (float) Math.sin((phi + dphi) * FastMath.DEG_TO_RAD);
            float cosdphi = (float) Math.cos((phi + dphi) * FastMath.DEG_TO_RAD);
            float sinphi = (float) Math.sin(phi * FastMath.DEG_TO_RAD);
            float cosphi = (float) Math.cos(phi * FastMath.DEG_TO_RAD);
            for(double theta = 0; theta <= 90 - dtheta; theta += dtheta) {
                float sindtheta = radius * (float) Math.sin((theta + dtheta) * FastMath.DEG_TO_RAD);
                float cosdtheta = radius * (float) Math.cos((theta + dtheta) * FastMath.DEG_TO_RAD);
                float sintheta = radius * (float) Math.sin(theta * FastMath.DEG_TO_RAD);
                float costheta = radius * (float) Math.cos(theta * FastMath.DEG_TO_RAD);
                vs[n++] = new Vector3f(sindtheta * cosphi, cosdtheta, -sindtheta * sinphi); // lu
                vs[n++] = new Vector3f(sintheta * cosphi, costheta, -sintheta * sinphi); // lo
                vs[n++] = new Vector3f(sintheta * cosdphi, costheta, -sintheta * sindphi); // ro
                
                vs[n++] = new Vector3f(sindtheta * cosdphi, cosdtheta, -sindtheta * sindphi); // ru
                vs[n++] = new Vector3f(sindtheta * cosphi, cosdtheta, -sindtheta * sinphi); // lu
                vs[n++] = new Vector3f(sintheta * cosdphi, costheta, -sintheta * sindphi); // ro
            }
        }
        return vs;
    }
    
    private static Vector3f[] genDome(float radius, float dtheta, float dphi) {
        // 360/dphi * (90/dtheta+1) vertices // +1 is last/bottom row
        int num = (int) ((360d / dphi) * (90d / dtheta + 1));
        Vector3f[] vs = new Vector3f[num];
        int n = 0;
        for(double phi = 0; phi <= 360 - dphi; phi += dphi) {
            float sinphi = (float) Math.sin(phi * FastMath.DEG_TO_RAD);
            float cosphi = (float) Math.cos(phi * FastMath.DEG_TO_RAD);
            for(double theta = 0; theta <= 90; theta += dtheta) {
                float sintheta = radius * (float) Math.sin(theta * FastMath.DEG_TO_RAD);
                float costheta = radius * (float) Math.cos(theta * FastMath.DEG_TO_RAD);
                vs[n++] = new Vector3f(sintheta * cosphi, costheta, -sintheta * sinphi); // lo
            }
        }
        return vs;
    }
    
    private static int[] genDomeIndices(float dtheta, float dphi) {
        // 360/dphi * 90/dtheta quads
        // 360/dphi * 90/dtheta * 2 triangles
        int numRows = (int) (90d / dtheta);
        int numRowVerts = numRows + 1;
        int numCols = (int) (360d / dphi);
        //System.out.println(numCols+" "+numRows);
        int num = numCols * numRows * 2 * 3;
        int[] is = new int[num];
        int n = 0;
        for(int col = 0; col < numCols; col++) {
            for(int row = 0; row < numRows; row++) {
                // each quad
                //System.out.println(col+" "+row);
                int colp1 = MathExt.wrapByMax(col + 1, numCols);
                is[n++] = (col + 0) * numRowVerts + (row + 1); // lu
                is[n++] = (col + 0) * numRowVerts + (row + 0); // lo
                is[n++] = (colp1) * numRowVerts + (row + 0); // ro
                
                is[n++] = (colp1) * numRowVerts + (row + 1); // ru
                is[n++] = (col + 0) * numRowVerts + (row + 1); // lu
                is[n++] = (colp1) * numRowVerts + (row + 0); // ro
            }
        }
        return is;
    }
}
