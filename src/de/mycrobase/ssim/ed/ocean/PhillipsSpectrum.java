package de.mycrobase.ssim.ed.ocean;

import ssim.sim.SimConst;

import com.jme3.math.Vector3f;

public class PhillipsSpectrum implements WaveSpectrum {
    
    private boolean suppressSmallWaves;
    
    private float convergenceConstant;
    private float aConstant;
    private Vector3f windVelocity;
    
    public PhillipsSpectrum(boolean suppressSmallWaves) {
        this.suppressSmallWaves = suppressSmallWaves;
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
        
        f *= Math.exp(-k * convergenceConstant);
        
        f *= Math.exp(-k*k * windVelocity.lengthSquared());
        
        return (float) f;
    }
    
}
