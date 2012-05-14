package de.mycrobase.ssim.ed.mesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;

import ssim.sim.SimConst;
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

import de.mycrobase.ssim.ed.ocean.WaveSpectrum;

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
    private float scaleZ;
    private WaveSpectrum waveSpectrum;
    private ScheduledExecutorService executor;
    private List<FFTUpdater> fftUpdaters;
    
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
    private Vector2f[][] mDeltaX;
    private Vector2f[][] mDeltaY;
    
    private VertexBuffer positionVBO;
    private VertexBuffer normalVBO;
    
    private float waveHeightScale;
    private float lambda;
    
    public OceanSurface(int numX, int numY, float scaleX, float scaleZ,
            WaveSpectrum waveSpectrum, ScheduledExecutorService executor) {
        this.numX = numX;
        this.numY = numY;
        this.numVertexX = numX+1;
        this.numVertexY = numY+1;
        this.scaleX = scaleX;
        this.scaleZ = scaleZ;
        this.waveSpectrum = waveSpectrum;
        this.executor = executor;
        
        initGeometry();
    }
    
    public float getWaveHeightScale() {
        return waveHeightScale;
    }

    public void setWaveHeightScale(float waveHeightScale) {
        this.waveHeightScale = waveHeightScale;
    }

    public float getLambda() {
        return lambda;
    }

    public void setLambda(float lambda) {
        this.lambda = lambda;
    }

    public void initSim() {
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

        fftUpdaters = Arrays.asList(
            new FFTUpdater(mDeltaX),
            new FFTUpdater(mDeltaY),
            new FFTUpdater(c)
        );
        
        Random r = new Random();
        for(int ix = 0; ix < numX; ix++) {
            for(int iy = 0; iy < numY; iy++) {
                // horizontal components of K, the movement direction
                fHold[ix][iy].x = 2f * MathExt.PI * ((float) ix - numX/2) / scaleX;
                fHold[ix][iy].y = 2f * MathExt.PI * ((float) iy - numY/2) / scaleZ;
                
                // length named k of movement vector K
                fHold[ix][iy].z = (float)
                    Math.sqrt(fHold[ix][iy].x*fHold[ix][iy].x + fHold[ix][iy].y*fHold[ix][iy].y);
                
                float phillipsRoot =
                    (float) Math.sqrt(waveSpectrum.getWaveCoefficient(fHold[ix][iy])) * MathExt.INV_SQRT_TWO;
                
                mH0[ix][iy].set(
                    (float) (r.nextGaussian() * phillipsRoot),
                    (float) (r.nextGaussian() * phillipsRoot));
            }
        }
        
    }
    
    public void update(float dt) {
        accTime += dt;
        
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
        positionBuffer = BufferUtils.createFloatBuffer((numVertexX * numVertexY + 4) * 3);
        normalBuffer = BufferUtils.createFloatBuffer((numVertexX * numVertexY + 4) * 3);
        
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
        
        // TODO: TriangleStrip
        setMode(Mode.Triangles);
        
        setBound(new BoundingBox(
            new Vector3f(0, -AssumedMaxWaveHeight, 0),
            new Vector3f(scaleX, +AssumedMaxWaveHeight, scaleZ)
        ));

        VertexBuffer[] indexVBOs = initTriangleIndexVBOs();
        
        setBuffer(indexVBOs[0]);
        setLodLevels(indexVBOs);
    }

    private VertexBuffer[] initTriangleIndexVBOs() {
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(numX * numY * 2 * 3 * 1);
        IntBuffer index2Buffer = BufferUtils.createIntBuffer(1 * 2 * 3);
        
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
        
        int offset = numVertexX * numVertexY;
        index2Buffer.put(offset+0).put(offset+1).put(offset+3);
        index2Buffer.put(offset+0).put(offset+3).put(offset+2);
        index2Buffer.rewind();

        VertexBuffer indexVBO = new VertexBuffer(Type.Index);
        indexVBO.setupData(Usage.Static, 1, Format.UnsignedInt, indexBuffer);
        indexVBO.updateData(indexBuffer);
        
        VertexBuffer index2VBO = new VertexBuffer(Type.Index);
        index2VBO.setupData(Usage.Static, 1, Format.UnsignedInt, index2Buffer);
        index2VBO.updateData(index2Buffer);
        
        return new VertexBuffer[] { indexVBO, index2VBO };
    }
    
    private void updateWaveCoefficients() {
        for(int ix = 0; ix < numX; ix++) {
            for(int iy = 0; iy < numY; iy++) {
                double wkt = Math.sqrt(fHold[ix][iy].z * SimConst.g * Math.tanh(fHold[ix][iy].z * Depth)) * accTime;
                
                double sinwkt = Math.sin(wkt);
                double coswkt = Math.cos(wkt);
                
                // calculate h~(K, t) from the Tessendorf paper
                c[ix][iy].set(
                    (float) (mH0[ix][iy].x*coswkt + mH0[ix][iy].y*sinwkt + mH0[numX-1-ix][numY-1-iy].x*coswkt - mH0[numX-1-ix][numY-1-iy].y*sinwkt),
                    (float) (mH0[ix][iy].y*coswkt + mH0[ix][iy].x*sinwkt - mH0[numX-1-ix][numY-1-iy].y*coswkt - mH0[numX-1-ix][numY-1-iy].x*sinwkt)
                );
            }
        }
        
        // set up the DX-DY-choppiness, needs all c values in position *before*
        // inverse FFT on c
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
        
        // do the inverse FFT on c to get the surface,
        // do inverse FFTs on DX-DY to get delta values
        try {
            executor.invokeAll(fftUpdaters);
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
        
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
        
        // create negative power term
        for(int ix = 0; ix < numX; ix++) {
            for(int iy = 0; iy < numY; iy++) {
                //if((ix+iy) % 2 != 0) c[ix][iy].x *= -1;
                if(((ix+iy) & 0x01) != 0) c[ix][iy].x = -c[ix][iy].x;
            }
        }
    }
    
    private void updateFaceNormals() {
        float xStep = scaleX/numX;
        float yStep = scaleZ/numY;
        
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
                vPositions[ix][iy].z = (float) iy/numY * scaleZ + mDeltaY[ix][iy].y;
            }
        }
        
        for(int iy = 0; iy < numVertexY-1; iy++) {
            vPositions[numVertexX-1][iy].set(vPositions[0][iy].x+scaleX, vPositions[0][iy].y, vPositions[0][iy].z);
        }
        for(int ix = 0; ix < numVertexX-1; ix++) {
            vPositions[ix][numVertexY-1].set(vPositions[ix][0].x, vPositions[ix][0].y, vPositions[ix][0].z+scaleZ);
        }
        vPositions[numVertexX-1][numVertexY-1].set(vPositions[0][0].x+scaleX, vPositions[0][0].y, vPositions[0][0].z+scaleZ);
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
        
        // add vertices for index2Buffer
        positionBuffer.put(0).put(0).put(0);
        positionBuffer.put(0).put(0).put(scaleZ);
        positionBuffer.put(scaleX).put(0).put(0);
        positionBuffer.put(scaleX).put(0).put(scaleZ);
        put(normalBuffer, Vector3f.UNIT_Y);
        put(normalBuffer, Vector3f.UNIT_Y);
        put(normalBuffer, Vector3f.UNIT_Y);
        put(normalBuffer, Vector3f.UNIT_Y);
        
        positionBuffer.rewind();
        normalBuffer.rewind();
        
        positionVBO.updateData(positionBuffer);
        normalVBO.updateData(normalBuffer);
    }
    
    private int getIndexFor(int ix, int iy) {
        return ix * numVertexY + iy;
    }
    
    private static void put(FloatBuffer buffer, Vector3f v) {
        buffer.put(v.x);
        buffer.put(v.y);
        buffer.put(v.z);
    }
    
    private class FFTUpdater implements Callable<Void> {
        
        private FFT threadFFT;
        
        private Vector2f[][] store;
        
        // hand over target store by reference
        public FFTUpdater(Vector2f[][] store) {
            this.store = store;
            System.out.println(store);
            
            // we need a FFT instance per thread because FFT is not thread-safe!
            threadFFT = new FFT();
        }
        
        @Override
        public Void call() throws Exception {
            threadFFT.iFFT2D(store);
            return null;
        }
    }
}
