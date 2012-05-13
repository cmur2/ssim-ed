package de.mycrobase.ssim.ed.ocean;

import ssim.sim.SimConst;

import com.jme3.math.Vector3f;

public class PhillipsSpectrum implements WaveSpectrum {
    
    private boolean suppressSmallWaves;
    
    // wave length in m, waves with smaller wave length are eliminated
    private float smallWaveCutoff;
    private float aConstant;
    private Vector3f windVelocity;
    
    public PhillipsSpectrum(boolean suppressSmallWaves) {
        this.suppressSmallWaves = suppressSmallWaves;
    }
    
    public float getSmallWaveCutoff() {
        return smallWaveCutoff;
    }

    public void setSmallWaveCutoff(float smallWaveCutoff) {
        this.smallWaveCutoff = smallWaveCutoff;
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
    
    @Override
    public float getWaveCoefficient(Vector3f vK) {
        // k = length(vector(K))
        float k = vK.z;
        
        if(k == 0) return 0;
        
        // L = V*V / g
        float L = windVelocity.lengthSquared() / SimConst.g;

        // dot product between K and windVelocity on the XZ plane
        float dotKW = (vK.x * windVelocity.x + vK.y * windVelocity.z);
        
        double f = aConstant;
        
        f *= Math.exp( -1d / ((k*L)*(k*L)) );
        
        f *= 1d / (k*k*k*k);
        
        //f *= dotKW * dotKW;
        f *= dotKW*dotKW / (k*k * windVelocity.lengthSquared()); 
        
        if(suppressSmallWaves) {
            f *= Math.exp(-k*k * smallWaveCutoff*smallWaveCutoff);
        }
        
        // I don't know where this term comes from, it's not in the original
        // Tessendorf paper and it eliminates all small waves...
        //f *= Math.exp(-k*k * windVelocity.lengthSquared());
        
        return (float) f;
    }
    
}
