package de.mycrobase.ssim.ed.mesh;

import ssim.util.MathExt;

import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class WindRose extends Mesh {
    
    private static final int divisions = 30;
    
    private float size;
    
    public WindRose(float size) {
        this.size = size;
        initGeometry();
    }

    private void initGeometry() {
        Vector3f[] positions = new Vector3f[divisions];
        
        int n = 0;
        for(int i = 0; i < divisions; i++) {
            double winkel = 360/divisions*i;
            positions[n] = new Vector3f();
            positions[n].x = (float) (Math.sin(Math.toRadians(winkel))*size);
            positions[n].y = (float) 0;
            positions[n].z = (float) (Math.cos(Math.toRadians(winkel))*size);
            n++;
        }
        
        Vector3f[] finalPos = new Vector3f[divisions*2 + 2];
        
        int m = 0;
        for(int i = 0; i < divisions; i++) {
            finalPos[m] = positions[i];
            m++;
            finalPos[m] = positions[MathExt.wrapByMax(i+1, divisions)];
            m++;
        }
        
        finalPos[m] = positions[divisions/2];
        m++;
        finalPos[m] = Vector3f.ZERO;
        m++;
        
        setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(finalPos));
        setMode(Mode.Lines);
        updateBound();
    }
}
