package de.mycrobase.ssim.ed.pre.ocean;

import java.util.Random;

import com.jme3.math.Vector2f;

import ssim.util.MathExt;

public class SineWavesParamGenerator {

    //private static final int WindScaleFactor = 8;

    private float ratioAmpOverLambda;
    private int bumpMapSize; // in texels
    private float minLambda; // in texels
    private float maxLambda; // in texels
    private float meanPhiFactor;
    private float phiFactorDeviation;

    public final int numActiveWaves;

    private float[] amplitudes;
    private float[] lambdas;
    private Vector2f[] directions;
    private float[] omegas;
    private float[] phis;

    private Random r = new Random();

    public SineWavesParamGenerator(int numActiveWaves) {
        this.numActiveWaves = numActiveWaves;
        amplitudes = new float[numActiveWaves];
        lambdas    = new float[numActiveWaves];
        directions = new Vector2f[numActiveWaves];
        omegas     = new float[numActiveWaves];
        phis       = new float[numActiveWaves];
    }

    // Constraints

    public void setBumpMapSize(int texels) {
        bumpMapSize = texels;
    }

    public void setRatioAmpOverLambda(float ratio) {
        ratioAmpOverLambda = ratio;
    }

    public void setMinLambda(int texels) {
        minLambda = texels;
    }

    public void setMaxLambda(int texels) {
        maxLambda = texels;
    }

    public void setMeanPhiFactor(float phi) {
        meanPhiFactor = phi;
    }

    public void setPhiFactorDeviation(float dev) {
        phiFactorDeviation = dev;
    }

    // Controlling

    // CUR: Implement SineWaveParamGenerator's controlling functions

    public void letWaveDie(int num) {
        assert num >= 0 && num < numActiveWaves;
        // fade out amplitude of old set over time
        // delete old set
        // generate new set
        // fade in new set
    }

    public void update0() {
        for(int i = 0; i < numActiveWaves; i++) {
            generateParamSet(i);
        }
    }

    public void update() {
        // do nothing
    }

    // Querying

    public int getNumActiveWaves() { return numActiveWaves; }
    public float[] getAmplitudes() { return amplitudes; }
    public float[] getLambdas() { return lambdas; }
    public Vector2f[] getDirections() { return directions; }
    public float[] getOmegas() { return omegas; }
    public float[] getPhis() { return phis; }

    // Private

    private void generateParamSet(int waveIdx) {
        int rndLambda = (int)(minLambda+Math.random()*(maxLambda-minLambda));
        float lambda = (float)rndLambda/bumpMapSize;
        float amp = ratioAmpOverLambda*lambda;
        // Dir anhand von Abweichung vom x/y-Verhaeltnis:
        //assert windDirection.y != 0;
        //float windXoverY = windDirection.x / windDirection.y;
        //windXoverY = (float)(windXoverY + (Math.random()-0.5) * windXoverY*1.5);
        //Vector2f dir = new Vector2f(WindScaleFactor, Math.round(WindScaleFactor/windXoverY));
        Vector2f dir = new Vector2f();
        dir.x = r.nextInt(5);
        dir.y = (int) ((Math.random()-0.5) * 6);
        dir.multLocal(lambda);
        float omega = 2f*MathExt.PI/lambda;
        float phiFactor = (float)(meanPhiFactor + phiFactorDeviation * (Math.random()-0.5));
        amplitudes[waveIdx] = Float.valueOf(amp);
        lambdas[waveIdx] = Float.valueOf(lambda);
        directions[waveIdx] = dir;
        omegas[waveIdx] = Float.valueOf(omega);
        phis[waveIdx] = Float.valueOf(phiFactor*omega);
    }
}
