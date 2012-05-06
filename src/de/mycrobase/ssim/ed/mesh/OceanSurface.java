package de.mycrobase.ssim.ed.mesh;

import java.nio.FloatBuffer;
import java.util.Random;

import ssim.util.FFT;
import ssim.util.MathExt;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.util.BufferUtils;

public class OceanSurface extends Mesh {
    
    private int numX;
    private int numY;
    private int numVertexX;
    private int numVertexY;
    private float scaleX;
    private float scaleY;
    private FFT fft;
    
    private FloatBuffer positionBuffer;
    private FloatBuffer normalBuffer;
    
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
    
    private VertexBuffer positionVBO;
    private VertexBuffer normalVBO;
    
    private float waveHeightScale;
    private float convergenceConstant;
    private float aConstant;
    private Vector3f windVelocity;
    
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
        
        for(int ix = 0; ix < numX; ix++) {
            for(int iy = 0; iy < numY; iy++) {
                double wkt = Math.sqrt(fHold[ix][iy].z * 9.81 * Math.tanh(fHold[ix][iy].z * 4000)) * accTime;
                
                double sinwkt = Math.sin(wkt);
                double coswkt = Math.cos(wkt);
                
                // calculate h~(K, t) from the Tessendorf paper
                c[ix][iy].set(
                    (float) (mH0[ix][iy].x*coswkt + mH0[ix][iy].y*sinwkt + mH0[numX-1-ix][numY-1-iy].x*coswkt - mH0[numX-1-ix][numY-1-iy].y*sinwkt),
                    (float) (mH0[ix][iy].y*coswkt + mH0[ix][iy].x*sinwkt - mH0[numX-1-ix][numY-1-iy].y*coswkt - mH0[numX-1-ix][numY-1-iy].x*sinwkt)
                );
            }
        }
        
        fft.iFFT2D(c);
        
        for(int ix = 0; ix < numX; ix++) {
            for(int iy = 0; iy < numY; iy++) {
                //if((ix+iy) % 2 != 0) c[ix][iy].x *= -1;
                if(((ix+iy) & 0x01) != 0) c[ix][iy].x = -c[ix][iy].x;
            }
        }
        
        updateFaceNormals();
        
        for(int ix = 0; ix < numX; ix++) {
            int ixLeft = MathExt.wrapByMax(ix-1, numX-1);
            for(int iy = 0; iy < numY; iy++) {
                int iyLeft = MathExt.wrapByMax(iy-1, numY-1);
                
                vPositions[ix][iy].x = (float) ix/numX * scaleX;
                vPositions[ix][iy].y = c[ix][iy].x * waveHeightScale;
                vPositions[ix][iy].z = (float) iy/numY * scaleY;
                
                float xsum = fNormals[ix][iy].x + fNormals[ixLeft][iy].x + fNormals[ix][iyLeft].x + fNormals[ixLeft][iyLeft].x;
                float ysum = fNormals[ix][iy].y + fNormals[ixLeft][iy].y + fNormals[ix][iyLeft].y + fNormals[ixLeft][iyLeft].y;
                float zsum = fNormals[ix][iy].z + fNormals[ixLeft][iy].z + fNormals[ix][iyLeft].z + fNormals[ixLeft][iyLeft].z;
                
                vNormals[ix][iy].set(xsum/4, ysum/4, zsum/4);
            }
        }
        
        for(int iy = 0; iy < numVertexY-1; iy++) {
            vPositions[numVertexX-1][iy].set(vPositions[0][iy].x+scaleX, vPositions[0][iy].y, vPositions[0][iy].z);
            vNormals[numVertexX-1][iy].set(vNormals[0][iy]);
        }
        for(int ix = 0; ix < numVertexX-1; ix++) {
            vPositions[ix][numVertexY-1].set(vPositions[ix][0].x, vPositions[ix][0].y, vPositions[ix][0].z+scaleY);
            vNormals[ix][numVertexY-1].set(vNormals[ix][0]);
        }
        vPositions[numVertexX-1][numVertexY-1].set(vPositions[0][0].x+scaleX, vPositions[0][0].y, vPositions[0][0].z+scaleY);
        vNormals[numVertexX-1][numVertexY-1].set(vNormals[0][0]);
        
        // final step: bring data model into vertex buffers
        for(int ix = 0; ix < numX; ix++) {
            for(int iy = 0; iy < numY; iy++) {
                // first triangle
                positionBuffer.put(vPositions[ix][iy].x);
                positionBuffer.put(vPositions[ix][iy].y);
                positionBuffer.put(vPositions[ix][iy].z);
                
                normalBuffer.put(vNormals[ix][iy].x);
                normalBuffer.put(vNormals[ix][iy].y);
                normalBuffer.put(vNormals[ix][iy].z);

                positionBuffer.put(vPositions[ix][iy+1].x);
                positionBuffer.put(vPositions[ix][iy+1].y);
                positionBuffer.put(vPositions[ix][iy+1].z);

                normalBuffer.put(vNormals[ix][iy+1].x);
                normalBuffer.put(vNormals[ix][iy+1].y);
                normalBuffer.put(vNormals[ix][iy+1].z);

                positionBuffer.put(vPositions[ix+1][iy+1].x);
                positionBuffer.put(vPositions[ix+1][iy+1].y);
                positionBuffer.put(vPositions[ix+1][iy+1].z);

                normalBuffer.put(vNormals[ix+1][iy+1].x);
                normalBuffer.put(vNormals[ix+1][iy+1].y);
                normalBuffer.put(vNormals[ix+1][iy+1].z);

                // second triangle
                positionBuffer.put(vPositions[ix][iy].x);
                positionBuffer.put(vPositions[ix][iy].y);
                positionBuffer.put(vPositions[ix][iy].z);

                normalBuffer.put(vNormals[ix][iy].x);
                normalBuffer.put(vNormals[ix][iy].y);
                normalBuffer.put(vNormals[ix][iy].z);

                positionBuffer.put(vPositions[ix+1][iy+1].x);
                positionBuffer.put(vPositions[ix+1][iy+1].y);
                positionBuffer.put(vPositions[ix+1][iy+1].z);

                normalBuffer.put(vNormals[ix+1][iy+1].x);
                normalBuffer.put(vNormals[ix+1][iy+1].y);
                normalBuffer.put(vNormals[ix+1][iy+1].z);

                positionBuffer.put(vPositions[ix+1][iy].x);
                positionBuffer.put(vPositions[ix+1][iy].y);
                positionBuffer.put(vPositions[ix+1][iy].z);

                normalBuffer.put(vNormals[ix+1][iy].x);
                normalBuffer.put(vNormals[ix+1][iy].y);
                normalBuffer.put(vNormals[ix+1][iy].z);
            }
        }
        positionBuffer.rewind();
        normalBuffer.rewind();
        
        positionVBO.updateData(positionBuffer);
        normalVBO.updateData(normalBuffer);
    }
    
    private void initGeometry() {
        // vertex data
        positionBuffer = BufferUtils.createFloatBuffer(numX * numY * 2 * 3 * 3);
        normalBuffer = BufferUtils.createFloatBuffer(numX * numY * 2 * 3 * 3);
        
        // CPU only data
        vPositions = new Vector3f[numVertexX][numVertexY];
        vNormals = new Vector3f[numVertexX][numVertexY];
        fHold = new Vector3f[numX][numY];
        mH0 = new Vector2f[numX][numY];
        fNormals = new Vector3f[numX][numY];
        c = new Vector2f[numX][numY];
        
        positionVBO = new VertexBuffer(Type.Position);
        positionVBO.setupData(Usage.Stream, 3, Format.Float, positionBuffer);
        setBuffer(positionVBO);
        
        normalVBO = new VertexBuffer(Type.Normal);
        normalVBO.setupData(Usage.Stream, 3, Format.Float, normalBuffer);
        setBuffer(normalVBO);
        
        setMode(Mode.Triangles);
    }
    
    private void updateFaceNormals() {
        float xStep = scaleX/numX;
        float yStep = scaleY/numY;
        
        for(int ix = 0; ix < numX-1; ix++) {
            for(int iy = 0; iy < numY-1; iy++) {
                float tax = 0;
                float tay = (c[ix][iy+1].x-c[ix][iy].x) * waveHeightScale;
                float taz = yStep;
                
                float tbx = xStep;
                float tby = (c[ix+1][iy].x-c[ix][iy].x) * waveHeightScale;
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
        
        for(int ix = 0; ix < numX-1; ix++) { fNormals[ix][numY-1].set(fNormals[ix][0]); }
        for(int iy = 0; iy < numY-1; iy++) { fNormals[numX-1][iy].set(fNormals[0][iy]); }
        fNormals[numX-1][numY-1].set(fNormals[0][0]);
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
        
        System.out.println(f);
        return (float) f;
    }
}
