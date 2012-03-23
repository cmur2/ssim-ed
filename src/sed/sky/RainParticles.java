package sed.sky;

import java.nio.FloatBuffer;
import java.util.Random;

import sed.util.TempVars;

import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.util.BufferUtils;

public class RainParticles extends Mesh {
    
    private int numDrops;
    private float size;
    private Random random;
    
    private FloatBuffer positionBuffer;
    private FloatBuffer colorBuffer;
    private Vector3f[] velocities;
    
    private VertexBuffer positionVBO;
    private VertexBuffer colorVBO;
    
    private float dropLength;
    private float dropLengthVar;
    private ColorRGBA dropColor;
    private ColorRGBA dropColorVar;
    private float dropVelocity;
    private float dropVelocityVar;
    private float minY;
    private float maxY;
    
    public RainParticles(int numDrops, float size) {
        this.numDrops = numDrops;
        this.size = size;
        
        random = new Random();
        
        initGeometry();
    }
    
    public float getDropLength() {
        return dropLength;
    }

    public void setDropLength(float dropLength) {
        this.dropLength = dropLength;
    }

    public float getDropLengthVar() {
        return dropLengthVar;
    }

    public void setDropLengthVar(float dropLengthVar) {
        this.dropLengthVar = dropLengthVar;
    }

    public ColorRGBA getDropColor() {
        return dropColor;
    }

    public void setDropColor(ColorRGBA dropColor) {
        this.dropColor = dropColor;
    }

    public ColorRGBA getDropColorVar() {
        return dropColorVar;
    }

    public void setDropColorVar(ColorRGBA dropColorVar) {
        this.dropColorVar = dropColorVar;
    }

    public float getDropVelocity() {
        return dropVelocity;
    }

    public void setDropVelocity(float dropVelocity) {
        this.dropVelocity = dropVelocity;
    }

    public float getDropVelocityVar() {
        return dropVelocityVar;
    }

    public void setDropVelocityVar(float dropVelocityVar) {
        this.dropVelocityVar = dropVelocityVar;
    }

    public float getMinY() {
        return minY;
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }

    public float getMaxY() {
        return maxY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }

    public void initFirstDrops() {
        TempVars vars = TempVars.get();
        
        for(int i = 0; i < numDrops; i++) {
            // position (upper end)
            float x = random.nextFloat() * size;
            float y = random.nextFloat() * (maxY-minY) + minY;
            float z = random.nextFloat() * size;
            // drop length in m, will displace lower end with this
            float length = getVaryingLength();
            positionBuffer.put(x).put(y).put(z);
            positionBuffer.put(x).put(y-length).put(z);
            // color (both ends)
            // TODO: use small color palette and random color index if possible
            ColorRGBA c = getVaryingColor(vars.color1);
            colorBuffer.put(c.r).put(c.g).put(c.b).put(c.a);
            colorBuffer.put(c.r).put(c.g).put(c.b).put(c.a);
            // velocity (no shader parameter, only used on CPU)
            // TODO: wind influence
            velocities[i] = new Vector3f(
                0,
                -getVaryingVelocity(),
                0
                );
        }
        positionBuffer.rewind();
        colorBuffer.rewind();
        
        vars.release();
        
        positionVBO.updateData(positionBuffer);
        colorVBO.updateData(colorBuffer);
        //updateBound();
    }
    
    public void update(float dt) {
        TempVars vars = TempVars.get();
        
        int n = 0;
        for(int i = 0; i < numDrops; i++) {
            // get y from second drop position (lower end)
            float curY = positionBuffer.get((i*2+1)*3 + 1);
            // reinitialize drop if it's below minY
            if(curY < minY) {
                float x = random.nextFloat() * size;
                float y = maxY + (curY-minY);
                float z = random.nextFloat() * size;
                
                float length = getVaryingLength();
                
                positionBuffer.put(n, x); n++;
                positionBuffer.put(n, y); n++;
                positionBuffer.put(n, z); n++;
                positionBuffer.put(n, x); n++;
                positionBuffer.put(n, y-length); n++;
                positionBuffer.put(n, z); n++;
            } else {
                Vector3f dist = vars.vect1.set(velocities[i]);
                dist.multLocal(dt);
                
                positionBuffer.put(n, positionBuffer.get(n) + dist.x); n++;
                positionBuffer.put(n, positionBuffer.get(n) + dist.y); n++;
                positionBuffer.put(n, positionBuffer.get(n) + dist.z); n++;
                positionBuffer.put(n, positionBuffer.get(n) + dist.x); n++;
                positionBuffer.put(n, positionBuffer.get(n) + dist.y); n++;
                positionBuffer.put(n, positionBuffer.get(n) + dist.z); n++;
            }
        }
        
        vars.release();
        
        positionVBO.updateData(positionBuffer);
//        colorVBO.updateData(colorBuffer);
//        updateBound();
    }
    
    private void initGeometry() {
        // vertex data
        positionBuffer = BufferUtils.createFloatBuffer(numDrops * 2 * 3);
        colorBuffer = BufferUtils.createFloatBuffer(numDrops * 2 * 4);
        
        // CPU only data
        velocities = new Vector3f[numDrops];
        
        positionVBO = new VertexBuffer(Type.Position);
        positionVBO.setupData(Usage.Stream, 3, Format.Float, positionBuffer);
        setBuffer(positionVBO);
        
        colorVBO = new VertexBuffer(Type.Color);
        colorVBO.setupData(Usage.Stream, 4, Format.Float, colorBuffer);
        setBuffer(colorVBO);
        
        setMode(Mode.Lines);
    }

    private float getVaryingVelocity() {
        return dropVelocity + random.nextFloat() * dropVelocityVar;
    }
    
    private float getVaryingLength() {
        return dropLength + random.nextFloat() * dropLengthVar;
    }
    
    private ColorRGBA getVaryingColor(ColorRGBA store) {
        float v = random.nextFloat();
        store.r = dropColor.r + v * dropColorVar.r;
        store.g = dropColor.g + v * dropColorVar.g;
        store.b = dropColor.b + v * dropColorVar.b;
        store.a = dropColor.a + v * dropColorVar.a;
        return store;
    }
}
