package sed.sky;

import java.util.Random;

import com.jme3.math.FastMath;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;

public class StarField extends Mesh {
    
    private static final float PhiRange = 359f; // in degree
    private static final float ThetaRange = 75f; // in degree
    private static final float ThetaOffset = 5f; // in degree
    private static final float MagOffset = 0.4f;
    private static final float MagRange = 0.6f;
    
    private int numStars;
    private float radius;
    
    public StarField(int numStars, float radius) {
        this.numStars = numStars;
        this.radius = radius;
        initGeometry();
    }
    
    private void initGeometry() {
        float[] starFieldCoords = new float[numStars*3];
        float[] starFieldColors = new float[numStars*4];
        
        Random rand = new Random();
        for(int i = 0; i < numStars; i++) {
            // phi range should cover the whole 360°
            float phi = (rand.nextFloat() * PhiRange) * FastMath.DEG_TO_RAD;
            // the theta offset prevents positions exactly in zenith
            // (theta = 0 in zenith), and theta range should be smaller than
            // 90°-offset because horizon is at 90°
            float theta = (ThetaOffset + rand.nextFloat() * ThetaRange) * FastMath.DEG_TO_RAD;
            // the magnitude provides a grey scale color value in [0.0, 1.0]
            float mag = MagOffset + MagRange * rand.nextFloat();
            // generate position
            starFieldCoords[i*3+0] = (float)( radius*Math.sin(theta)*Math.cos(phi));
            starFieldCoords[i*3+1] = (float)( radius*Math.cos(theta));
            starFieldCoords[i*3+2] = (float)(-radius*Math.sin(theta)*Math.sin(phi));
            // generate color
            starFieldColors[i*4+0] = mag; // R
            starFieldColors[i*4+1] = mag; // G
            starFieldColors[i*4+2] = mag; // B
            starFieldColors[i*4+3] = 1f; // A
        }
        
        setBuffer(Type.Position, 3, starFieldCoords);
        setBuffer(Type.Color, 4, starFieldColors);
        setMode(Mode.Points);
        setStatic();
        updateBound();
    }
}
