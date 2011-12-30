package sed.sky;

import java.util.Random;

import com.jme3.math.FastMath;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;

public class StarField extends Mesh {
    
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
            float phi = (rand.nextFloat()*359f)/180f*FastMath.PI;
            float theta = (5+rand.nextFloat()*82f)/180f*FastMath.PI;
            starFieldCoords[i*3+0] = (float)( radius*Math.sin(theta)*Math.cos(phi));
            starFieldCoords[i*3+1] = (float)( radius*Math.cos(theta));
            starFieldCoords[i*3+2] = (float)(-radius*Math.sin(theta)*Math.sin(phi));
            float mag = 0.2f + 0.8f * rand.nextFloat();
            starFieldColors[i*4+0] = starFieldColors[i*4+1] = starFieldColors[i*4+2] = mag;
            starFieldColors[i*4+3] = 1f;
        }
        
        setBuffer(Type.Position, 3, starFieldCoords);
        setBuffer(Type.Color, 4, starFieldColors);
        setMode(Mode.Points);
        setStatic();
        updateBound();
    }
}
