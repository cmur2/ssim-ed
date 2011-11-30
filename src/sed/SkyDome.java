package sed;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class SkyDome extends Mesh {
	
	public SkyDome() {
		float dtheta = 2;
		float dphi = 2;
		Vector3f[] p = generateDomeCoords(100f, dtheta, dphi);
		
//		float[] points = new float[p.length*3];
//		for(int i = 0; i < p.length; i++) {
//			points[i*3+0] = p[i].x;
//			points[i*3+1] = p[i].y;
//			points[i*3+2] = p[i].z;
//		}
		
		int numRows = (int)(90d/dtheta+1);
		int numCols = (int)(360d/dphi);
		
//		int[] indizes = new int[p.length*2 * 3];
//		for(int i = 0; i < indizes.length/3; i++) {
//			indizes[3*i+0] = i;
//			indizes[3*i+1] = i+numCols;
//			indizes[3*i+2] = i+numCols+1;
//		}
		
		setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(p));
//		setBuffer(Type.Index, 1, BufferUtils.createIntBuffer(indizes));
		updateBound();
		
//		setMode(Mode.Points);
	}
	
	private static Vector3f[] generateDomeCoords(float radius, float dtheta, float dphi) {
		Vector3f[] Vertices = new Vector3f[(int)((360d/dphi)*(90d/dtheta)*6d)];
		int n = 0;
		dtheta *= FastMath.DEG_TO_RAD;
		dphi *= FastMath.DEG_TO_RAD;
		for(double phi = 0; phi <= 360*FastMath.DEG_TO_RAD - dphi; phi += dphi) {
			float sindphi = (float)Math.sin(phi+dphi);
			float cosdphi = (float)Math.cos(phi+dphi);
			float sinphi = (float)Math.sin(phi);
			float cosphi = (float)Math.cos(phi);
			for(double theta = 0; theta <= 90*FastMath.DEG_TO_RAD - dtheta; theta += dtheta) {
				float sindtheta = radius*(float)Math.sin(theta+dtheta);
				float cosdtheta = radius*(float)Math.cos(theta+dtheta);
				float sintheta = radius*(float)Math.sin(theta);
				float costheta = radius*(float)Math.cos(theta);
				// lu
				Vertices[n++] = new Vector3f(sindtheta* cosphi, cosdtheta, -sindtheta*  sinphi);
				// lo
				Vertices[n++] = new Vector3f( sintheta* cosphi,  costheta, - sintheta* sinphi);
				// ro
				Vertices[n++] = new Vector3f( sintheta*cosdphi,  costheta, - sintheta*sindphi);
				
				// ru
				Vertices[n++] = new Vector3f(sindtheta*cosdphi, cosdtheta, -sindtheta*sindphi);
				// lu
				Vertices[n++] = new Vector3f(sindtheta* cosphi, cosdtheta, -sindtheta*  sinphi);
				// ro
				Vertices[n++] = new Vector3f( sintheta*cosdphi,  costheta, - sintheta*sindphi);
			}
		}
		return Vertices;
	}
}
