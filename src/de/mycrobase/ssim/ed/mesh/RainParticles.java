package de.mycrobase.ssim.ed.mesh;

import java.nio.FloatBuffer;
import java.util.Random;


import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.util.BufferUtils;

import de.mycrobase.ssim.ed.util.TempVars;

public class RainParticles extends Mesh {
    
    private static final float DeviationScale = 0.075f;
    
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
    private float initY;
    
    private Vector3f windVelocity;
    
    public RainParticles(int numDrops, float size) {
        this.numDrops = numDrops;
        this.size = size;
        
        random = new Random();
        
        initGeometry();
    }
    
    // TODO: transparent and with lighting applied
    
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
    
    public float getInitY() {
        return initY;
    }

    public void setInitY(float initY) {
        this.initY = initY;
    }

    public Vector3f getWindVelocity() {
        return windVelocity;
    }

    public void setWindVelocity(Vector3f windVelocity) {
        this.windVelocity = windVelocity;
    }

    public void initFirstDrops() {
        TempVars vars = TempVars.get();
        
        for(int i = 0; i < numDrops; i++) {
            Vector3f dir = getVaryingDirection(vars.vect1);
            // position (upper end)
            float x = random.nextFloat() * size;
            float y = random.nextFloat() * (maxY-minY) + initY;
            float z = random.nextFloat() * size;
            // drop length in m, will displace lower end with this
            float length = getVaryingLength();
            Vector3f disp = vars.vect2.set(dir).multLocal(length);
            positionBuffer.put(x).put(y).put(z);
            positionBuffer.put(x + disp.x).put(y + disp.y).put(z + disp.z);
            // color (both ends)
            ColorRGBA c = getVaryingColor(vars.color1);
            colorBuffer.put(c.r).put(c.g).put(c.b).put(c.a);
            colorBuffer.put(c.r).put(c.g).put(c.b).put(c.a);
            // velocity (no shader parameter, only used on CPU)
            // This needs new Vector3f instance since velocities[i] might be
            // uninitialized.
            velocities[i] = dir.mult(getVaryingVelocity());
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
                Vector3f dir = getVaryingDirection(vars.vect1);
                
                float x = random.nextFloat() * size;
                float y = maxY + (curY-minY);
                float z = random.nextFloat() * size;
                
                float length = getVaryingLength();
                Vector3f disp = vars.vect2.set(dir).multLocal(length);
                
                positionBuffer.put(n, x); n++;
                positionBuffer.put(n, y); n++;
                positionBuffer.put(n, z); n++;
                positionBuffer.put(n, x + disp.x); n++;
                positionBuffer.put(n, y + disp.y); n++;
                positionBuffer.put(n, z + disp.z); n++;
                
                ColorRGBA c = getVaryingColor(vars.color1);
                int nc = i*2*4;
                colorBuffer.put(nc+0, c.r);
                colorBuffer.put(nc+1, c.g);
                colorBuffer.put(nc+2, c.b);
                colorBuffer.put(nc+3, c.a);
                colorBuffer.put(nc+4, c.r);
                colorBuffer.put(nc+5, c.g);
                colorBuffer.put(nc+6, c.b);
                colorBuffer.put(nc+7, c.a);
                
                // reuse old Vector3f
                velocities[i].set(dir);
                velocities[i].multLocal(getVaryingVelocity());
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
        colorVBO.updateData(colorBuffer);
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

    private Vector3f getVaryingDirection(Vector3f store) {
        if(store == null) {
            store = new Vector3f();
        }
        // take sum of weighted windVelocity (in m/s) and -UNIT_Y, then
        // normalize, no random variation included atm
        store.set(windVelocity);
        store.multLocal(DeviationScale);
        store.addLocal(0f, -1f, 0f);
        store.normalizeLocal();
        return store;
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
