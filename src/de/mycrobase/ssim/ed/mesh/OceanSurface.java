package de.mycrobase.ssim.ed.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import ssim.util.FFT;
import ssim.util.MathExt;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.util.BufferUtils;

public class OceanSurface extends Mesh {
    
    // TODO: convert into single parameter or function to allow variations
    private static final float Depth = 4000; // in m
    
    // used to determine pessimistic bounding box
    private static final float AssumedMaxWaveHeight = 50; // in m
    
    private int numX;
    private int numY;
    private int numVertexX;
    private int numVertexY;
    private float scaleX;
    private float scaleY;
    private FFT fft;
    
    private FloatBuffer positionBuffer;
    private FloatBuffer normalBuffer;
    private IntBuffer indexBuffer;
    
    // sim data
    private float accTime;
    // fHold saves fixed calculations for later use
    private Vector3f[][] fHold;
    private Vector2f[][] mH0;
    
    // temporary data, discarded between frames
    private Vector3f[][] vPositions;
    private Vector3f[][] vNormals;
    private Vector3f[][] fNormals;
    private Vector2f[][] c;
    private Vector2f[][] mDeltaX;
    private Vector2f[][] mDeltaY;
    
    private VertexBuffer positionVBO;
    private VertexBuffer normalVBO;
    private VertexBuffer indexVBO;
    
    private float waveHeightScale;
    private float convergenceConstant;
    private float aConstant;
    private Vector3f windVelocity;
    private float lambda;
    
    public OceanSurface(int numX, int numY, float scaleX, float scaleY) {
        this.numX = numX;
        this.numY = numY;
        this.numVertexX = numX+1;
        this.numVertexY = numY+1;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        
        fft = new FFT();
        
        initGeometry();
    }
    
    public float getWaveHeightScale() {
        return waveHeightScale;
    }

    public void setWaveHeightScale(float waveHeightScale) {
        this.waveHeightScale = waveHeightScale;
    }

    public float getConvergenceConstant() {
        return convergenceConstant;
    }

    public void setConvergenceConstant(float convergenceConstant) {
        this.convergenceConstant = convergenceConstant;
    }

    public float getAConstant() {
        return aConstant;
    }

    public void setAConstant(float aConstant) {
        this.aConstant = aConstant;
    }
    
    public Vector3f getWindVelocity() {
        return windVelocity;
    }
    
    public void setWindVelocity(Vector3f windVelocity) {
        this.windVelocity = windVelocity;
    }

    public float getLambda() {
        return lambda;
    }

    public void setLambda(float lambda) {
        this.lambda = lambda;
    }

    public void initSim() {
        // TODO: accTime may overrun
        accTime = 0f;
        
        for(int ix = 0; ix < numVertexX; ix++) {
            for(int iy = 0; iy < numVertexY; iy++) {
                vPositions[ix][iy] = new Vector3f();
                vNormals[ix][iy] = new Vector3f();
            }
        }

        for(int ix = 0; ix < numX; ix++) {
            for(int iy = 0; iy < numY; iy++) {
                fHold[ix][iy] = new Vector3f();
                mH0[ix][iy] = new Vector2f();

                fNormals[ix][iy] = new Vector3f();
                c[ix][iy] = new Vector2f();
                mDeltaX[ix][iy] = new Vector2f();
                mDeltaY[ix][iy] = new Vector2f();
            }
        }
        
        Random r = new Random();
        for(int ix = 0; ix < numX; ix++) {
            for(int iy = 0; iy < numY; iy++) {
                // horizontal components of K, the movement direction
                fHold[ix][iy].x = 2f * MathExt.PI * ((float) ix - numX/2) / scaleX;
                fHold[ix][iy].y = 2f * MathExt.PI * ((float) iy - numY/2) / scaleY;
                
                // length named k of movement vector K
                fHold[ix][iy].z = (float)
                    Math.sqrt(fHold[ix][iy].x*fHold[ix][iy].x + fHold[ix][iy].y*fHold[ix][iy].y);
                
                float phillipsRoot =
                    (float) Math.sqrt(calcPhillipsFunction(fHold[ix][iy])) * MathExt.INV_SQRT_TWO;
                
                mH0[ix][iy].set(
                    (float) (r.nextGaussian() * phillipsRoot),
                    (float) (r.nextGaussian() * phillipsRoot));
            }
        }
        
    }
    
    public void update(float dt) {
        accTime += dt;
        
        // TODO: fewer updates!
        
        updateWaveCoefficients();
        
        updateFaceNormals();
        
        updateVertexPositions();
        
        updateVertexNormals();
        
        // final step: bring data model into vertex buffers
        updateGridDataVBOs();
    }
    
    // helper
    
    private void initGeometry() {
        // vertex data
        positionBuffer = BufferUtils.createFloatBuffer(numVertexX * numVertexY * 3);
        normalBuffer = BufferUtils.createFloatBuffer(numVertexX * numVertexY * 3);
        indexBuffer = BufferUtils.createIntBuffer(numX * numY * 2 * 3 * 1);
        
        // CPU only data
        vPositions = new Vector3f[numVertexX][numVertexY];
        vNormals = new Vector3f[numVertexX][numVertexY];
        fHold = new Vector3f[numX][numY];
        mH0 = new Vector2f[numX][numY];
        fNormals = new Vector3f[numX][numY];
        c = new Vector2f[numX][numY];
        mDeltaX = new Vector2f[numX][numY];
        mDeltaY = new Vector2f[numX][numY];
        
        positionVBO = new VertexBuffer(Type.Position);
        positionVBO.setupData(Usage.Stream, 3, Format.Float, positionBuffer);
        setBuffer(positionVBO);
        
        normalVBO = new VertexBuffer(Type.Normal);
        normalVBO.setupData(Usage.Stream, 3, Format.Float, normalBuffer);
        setBuffer(normalVBO);
        
        indexVBO = new VertexBuffer(Type.Index);
        indexVBO.setupData(Usage.Static, 1, Format.UnsignedInt, indexBuffer);
        setBuffer(indexVBO);

        updateTriangleIndexVBO();
        
        // TODO: TriangleStrip
        setMode(Mode.Triangles);
        
        setBound(new BoundingBox(
            new Vector3f(0, -AssumedMaxWaveHeight, 0),
            new Vector3f(scaleX, +AssumedMaxWaveHeight, scaleY)
        ));
        
        //setLodLevels(null);
    }
    
    private void updateWaveCoefficients() {
        for(int ix = 0; ix < numX; ix++) {
            for(int iy = 0; iy < numY; iy++) {
                double wkt = Math.sqrt(fHold[ix][iy].z * 9.81 * Math.tanh(fHold[ix][iy].z * Depth)) * accTime;
                
                double sinwkt = Math.sin(wkt);
                double coswkt = Math.cos(wkt);
                
                // calculate h~(K, t) from the Tessendorf paper
                c[ix][iy].set(
                    (float) (mH0[ix][iy].x*coswkt + mH0[ix][iy].y*sinwkt + mH0[numX-1-ix][numY-1-iy].x*coswkt - mH0[numX-1-ix][numY-1-iy].y*sinwkt),
                    (float) (mH0[ix][iy].y*coswkt + mH0[ix][iy].x*sinwkt - mH0[numX-1-ix][numY-1-iy].y*coswkt - mH0[numX-1-ix][numY-1-iy].x*sinwkt)
                );
            }
        }
        
        updateChoppinessDelta();
        
        fft.iFFT2D(c);
        
        for(int ix = 0; ix < numX; ix++) {
            for(int iy = 0; iy < numY; iy++) {
                //if((ix+iy) % 2 != 0) c[ix][iy].x *= -1;
                if(((ix+iy) & 0x01) != 0) c[ix][iy].x = -c[ix][iy].x;
            }
        }
    }
    
    private void updateChoppinessDelta() {
        for(int ix = 0; ix < numX; ix++) {
            for(int iy = 0; iy < numY; iy++) {
                float k = fHold[ix][iy].z;
                if(k == 0) {
                    mDeltaX[ix][iy].set(0, 0);
                    mDeltaY[ix][iy].set(0, 0);
                } else {
                    mDeltaX[ix][iy].set(0, c[ix][iy].y * -fHold[ix][iy].x/k);
                    mDeltaY[ix][iy].set(0, c[ix][iy].y * -fHold[ix][iy].y/k);
                }
            }
        }
        
        // TODO: maybe parallize
        fft.iFFT2D(mDeltaX);
        fft.iFFT2D(mDeltaY);
        
        for(int ix = 0; ix < numX; ix++) {
            for(int iy = 0; iy < numY; iy++) {
                float s = lambda;
                if((ix+iy) % 2 != 0) {
                    s *= -1;
                }
                mDeltaX[ix][iy].multLocal(s);
                mDeltaY[ix][iy].multLocal(s);
            }
        }
    }

    private void updateFaceNormals() {
        float xStep = scaleX/numX;
        float yStep = scaleY/numY;
        
        for(int ix = 0; ix < numX; ix++) {
            int ixRight = MathExt.wrapByMax(ix+1, numX);
            
            for(int iy = 0; iy < numY; iy++) {
                int iyRight = MathExt.wrapByMax(iy+1, numY);
                
                // TODO: does not take mDelta into account
                float tax = 0;
                float tay = (c[ix][iyRight].x-c[ix][iy].x) * waveHeightScale;
                float taz = yStep;
                
                float tbx = xStep;
                float tby = (c[ixRight][iy].x-c[ix][iy].x) * waveHeightScale;
                float tbz = 0;
                
                // cross product
                float tcx = tay*tbz - taz*tby;
                float tcy = taz*tbx - tax*tbz;
                float tcz = tax*tby - tay*tbx;
                
                // set and normalize
                fNormals[ix][iy].set(tcx, tcy, tcz);
                fNormals[ix][iy].normalizeLocal();
            }
        }
    }
    
    private void updateVertexPositions() {
        for(int ix = 0; ix < numX; ix++) {
            for(int iy = 0; iy < numY; iy++) {
                vPositions[ix][iy].x = (float) ix/numX * scaleX + mDeltaX[ix][iy].y;
                vPositions[ix][iy].y = c[ix][iy].x * waveHeightScale;
                vPositions[ix][iy].z = (float) iy/numY * scaleY + mDeltaY[ix][iy].y;
            }
        }
        
        for(int iy = 0; iy < numVertexY-1; iy++) {
            vPositions[numVertexX-1][iy].set(vPositions[0][iy].x+scaleX, vPositions[0][iy].y, vPositions[0][iy].z);
        }
        for(int ix = 0; ix < numVertexX-1; ix++) {
            vPositions[ix][numVertexY-1].set(vPositions[ix][0].x, vPositions[ix][0].y, vPositions[ix][0].z+scaleY);
        }
        vPositions[numVertexX-1][numVertexY-1].set(vPositions[0][0].x+scaleX, vPositions[0][0].y, vPositions[0][0].z+scaleY);
    }
    
    private void updateVertexNormals() {
        for(int ix = 0; ix < numX; ix++) {
            int ixLeft = MathExt.wrapByMax(ix-1, numX);
            for(int iy = 0; iy < numY; iy++) {
                int iyLeft = MathExt.wrapByMax(iy-1, numY);
                float xsum = fNormals[ix][iy].x + fNormals[ixLeft][iy].x + fNormals[ix][iyLeft].x + fNormals[ixLeft][iyLeft].x;
                float ysum = fNormals[ix][iy].y + fNormals[ixLeft][iy].y + fNormals[ix][iyLeft].y + fNormals[ixLeft][iyLeft].y;
                float zsum = fNormals[ix][iy].z + fNormals[ixLeft][iy].z + fNormals[ix][iyLeft].z + fNormals[ixLeft][iyLeft].z;
                vNormals[ix][iy].set(xsum/4, ysum/4, zsum/4);
            }
        }
        
        for(int iy = 0; iy < numVertexY-1; iy++) {
            vNormals[numVertexX-1][iy].set(vNormals[0][iy]);
        }
        for(int ix = 0; ix < numVertexX-1; ix++) {
            vNormals[ix][numVertexY-1].set(vNormals[ix][0]);
        }
        vNormals[numVertexX-1][numVertexY-1].set(vNormals[0][0]);
    }
    
    private void updateGridDataVBOs() {
        // apply the indexing scheme given by #getIndexFor(int ix, int iy)
        for(int ix = 0; ix < numVertexX; ix++) {
            for(int iy = 0; iy < numVertexY; iy++) {
                put(positionBuffer, vPositions[ix][iy]);
                put(normalBuffer, vNormals[ix][iy]);
            }
        }
        positionBuffer.rewind();
        normalBuffer.rewind();
        
        positionVBO.updateData(positionBuffer);
        normalVBO.updateData(normalBuffer);
    }
    
    private void updateTriangleIndexVBO() {
        for(int ix = 0; ix < numX; ix++) {
            for(int iy = 0; iy < numY; iy++) {
                // first triangle
                indexBuffer.put(getIndexFor(ix, iy));
                indexBuffer.put(getIndexFor(ix, iy+1));
                indexBuffer.put(getIndexFor(ix+1, iy+1));
                
                // second triangle
                indexBuffer.put(getIndexFor(ix, iy));
                indexBuffer.put(getIndexFor(ix+1, iy+1));
                indexBuffer.put(getIndexFor(ix+1, iy));
            }
        }
        indexBuffer.rewind();
        
        indexVBO.updateData(indexBuffer);
    }
    
    private int getIndexFor(int ix, int iy) {
        return ix * numVertexY + iy;
    }
    
    // TODO: into interface
    private float calcPhillipsFunction(Vector3f vK) {
        float g = 9.81f;
        
        // k = length(vector(K))
        float k = vK.z;
        
        if(k == 0) return 0;
        
        // L = V*V / g
        float L = windVelocity.lengthSquared() / g;

        // dot product between K and windVelocity on the XZ plane
        float dotKW = (vK.x * windVelocity.x + vK.y * windVelocity.z);
        
        double f = aConstant;
        
        f *= Math.exp( -1d / ((k*L)*(k*L)) );
        
        f *= 1d / (k*k*k*k);
        
        //f *= dotKW * dotKW;
        f *= dotKW*dotKW / (k*k * windVelocity.lengthSquared()); 
        
        f *= Math.exp(-k * convergenceConstant);
        
        f *= Math.exp(-k*k * windVelocity.lengthSquared());
        
        return (float) f;
    }
    
    private static void put(FloatBuffer buffer, Vector3f v) {
        buffer.put(v.x);
        buffer.put(v.y);
        buffer.put(v.z);
    }
}
