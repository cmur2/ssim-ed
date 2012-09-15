package ssim.util;

import javax.vecmath.Tuple2f;

import com.jme3.math.Vector2f;

/**
 *
 * @author Ch. Nicolai
 */
public class FFT {
    
    private int n = -1;
    private int m;
    
    private float[] tmpReRow;
    private float[] tmpImRow;
    private float[] tmpReCol;
    private float[] tmpImCol;
    
    private float[] fUABuffer;
    private float[] fUBBuffer;
    private float[] iUABuffer;
    private float[] iUBBuffer;
    
    public FFT() {}
    
    public FFT(int n) {
        setN(n);
    }
    
    private void reinitTables() {
        tmpReRow = new float[n];
        tmpImRow = new float[n];
        tmpReCol = new float[n];
        tmpImCol = new float[n];
        
        fUABuffer = new float[n];
        fUBBuffer = new float[n];
        int idx = 0;
        float c1 = -1, c2 = 0;
        int n4 = 1;
        for(int i = 0; i < m; i++) {
            int n3 = n4;
            n4 <<= 1; // c *= 2;
            float u1 = 1, u2 = 0;
            for(int j = 0; j < n3; j++) {
                fUABuffer[idx] = u1;
                fUBBuffer[idx] = u2;
                idx++;
                float z = u1*c1 - u2*c2;
                u2 = u1*c2 + u2*c1;
                u1 = z;
            }
            c2 = -(float) Math.sqrt((1-c1)/2);
            c1 = (float) Math.sqrt((1+c1)/2);
        }
        
        iUABuffer = new float[n];
        iUBBuffer = new float[n];
        idx = 0;
        c1 = -1; c2 = 0;
        n4 = 1;
        for(int i = 0; i < m; i++) {
            int n3 = n4;
            n4 <<= 1; // c *= 2;
            float u1 = 1, u2 = 0;
            for(int j = 0; j < n3; j++) {
                iUABuffer[idx] = u1;
                iUBBuffer[idx] = u2;
                idx++;
                float z = u1*c1 - u2*c2;
                u2 = u1*c2 + u2*c1;
                u1 = z;
            }
            c2 = (float) Math.sqrt((1-c1)/2);
            c1 = (float) Math.sqrt((1+c1)/2);
        }
    }
    
    private void setN(int n) {
        if(this.n != n) {
            // n = m^2  <=>  m = log(n)/log(2)
            m = (int) (Math.log(n)/Math.log(2));
            // n != 2^m
            if(n != (1 << m))
                throw new IllegalArgumentException("FFT: n must be a power of 2! (n = "+n+")");
            this.n = n;
            reinitTables();
        }
    }
    
    // ==================== 1D Part ====================
    
    /**
     * This computes an <u>in-place complex-to-complex FFT</u> with
     * use of <u>float precision</u>.<br>
     * <br>
     * Formula: forward <i>(n = 0..N-1)</i>
     * <pre>
     *             N-1
     *             ---
     *         1   \          - j k 2 pi n / N
     * X(n) = ---   >   x(k) e
     *         N   /
     *             ---
     *             k=0
     * </pre>
     * Formula: reverse <i>(n = 0..N-1)</i>
     * <pre>
     *             N-1
     *             ---
     *             \          j k 2 pi n / N
     * X(n) =       >   x(k) e
     *             /
     *             ---
     *             k=0
     * </pre>
     * @param x real array of 2^m points
     * @param y imaginary array of 2^m points
     */
    public void fFFT1D(float[] x, float[] y) {
        if(x.length != y.length)
            throw new IllegalArgumentException("fFFT1D: length of real and imaginary arrays must be equal!" +
                " (real: "+x.length+" imag:"+y.length+")");
        setN(x.length);
        // Do the bit reversal
        int n2 = n/2;
        for(int i = 0, j = 0; i < n-1; i++) {
            if(i < j) {
                float tx = x[i], ty = y[i];
                x[i] = x[j]; y[i] = y[j];
                x[j] = tx; y[j] = ty;
            }
            int n1 = n2;
            while(n1 <= j) {
                j -= n1;
                n1 >>= 1; // n1 /= 2
            }
            j += n1;
        }
        // Compute the FFT
        float c1 = -1, c2 = 0;
        int n4 = 1;
        int idx = 0;
        for(int i = 0; i < m; i++) {
            int n3 = n4;
            n4 <<= 1; // c *= 2;
            for(int j = 0; j < n3; j++) {
                float u1 = fUABuffer[idx], u2 = fUBBuffer[idx];
                for(int k = j; k < n; k += n4) {
                    int a = k + n3;
                    float t1 = u1*x[a] - u2*y[a];
                    float t2 = u1*y[a] + u2*x[a];
                    x[a] = x[k]-t1;
                    y[a] = y[k]-t2;
                    x[k] += t1;
                    y[k] += t2;
                }
                idx++;
            }
        }
        // Scaling for forward transform
        for(int i = 0; i < n; i++) {
            x[i] /= n; y[i] /= n;
        }
    }
    
    public void fFFT1D_old(float[] x, float[] y) {
        if(x.length != y.length)
            throw new IllegalArgumentException("fFFT1D: length of real and imaginary arrays must be equal!" +
                " (real: "+x.length+" imag:"+y.length+")");
        setN(x.length);
        // Do the bit reversal
        int n2 = n/2;
        for(int i = 0, j = 0; i < n-1; i++) {
            if(i < j) {
                float tx = x[i], ty = y[i];
                x[i] = x[j]; y[i] = y[j];
                x[j] = tx; y[j] = ty;
            }
            int n1 = n2;
            while(n1 <= j) {
                j -= n1;
                n1 >>= 1; // n1 /= 2
            }
            j += n1;
        }
        // Compute the FFT
        float c1 = -1, c2 = 0;
        int n4 = 1;
        for(int i = 0; i < m; i++) {
            int n3 = n4;
            n4 <<= 1; // c *= 2;
            float u1 = 1, u2 = 0;
            for(int j = 0; j < n3; j++) {
                for(int k = j; k < n; k += n4) {
                    int a = k + n3;
                    float t1 = u1*x[a] - u2*y[a];
                    float t2 = u1*y[a] + u2*x[a];
                    x[a] = x[k]-t1;
                    y[a] = y[k]-t2;
                    x[k] += t1;
                    y[k] += t2;
                }
                float z = u1*c1 - u2*c2;
                u2 = u1*c2 + u2*c1;
                u1 = z;
            }
            c2 = -(float) Math.sqrt((1-c1)/2);
            c1 = (float) Math.sqrt((1+c1)/2);
        }
        // Scaling for forward transform
        for(int i = 0; i < n; i++) {
            x[i] /= n; y[i] /= n;
        }
    }
    
    public void iFFT1D(float[] x, float[] y) {
        if(x.length != y.length)
            throw new IllegalArgumentException("iFFT1D: length of real and imaginary arrays must be equal!" +
                " (real: "+x.length+" imag:"+y.length+")");
        setN(x.length);
        // Do the bit reversal
        int n2 = n/2;
        for(int i = 0, j = 0; i < n-1; i++) {
            if(i < j) {
                float tx = x[i], ty = y[i];
                x[i] = x[j]; y[i] = y[j];
                x[j] = tx; y[j] = ty;
            }
            int n1 = n2;
            while(n1 <= j) {
                j -= n1;
                n1 >>= 1; // n1 /= 2
            }
            j += n1;
        }
        // Compute the FFT
        float c1 = -1, c2 = 0;
        int n4 = 1;
        int idx = 0;
        for(int i = 0; i < m; i++) {
            int n3 = n4;
            n4 <<= 1; // c *= 2;
            for(int j = 0; j < n3; j++) {
                float u1 = iUABuffer[idx], u2 = iUBBuffer[idx];
                for(int k = j; k < n; k += n4) {
                    int a = k + n3;
                    float t1 = u1*x[a] - u2*y[a];
                    float t2 = u1*y[a] + u2*x[a];
                    x[a] = x[k]-t1;
                    y[a] = y[k]-t2;
                    x[k] += t1;
                    y[k] += t2;
                }
                idx++;
            }
        }
    }
    
    public void iFFT1D_old(float[] x, float[] y) {
        if(x.length != y.length)
            throw new IllegalArgumentException("iFFT1D: length of real and imaginary arrays must be equal!" +
                " (real: "+x.length+" imag:"+y.length+")");
        setN(x.length);
        // Do the bit reversal
        int n2 = n/2;
        for(int i = 0, j = 0; i < n-1; i++) {
            if(i < j) {
                float tx = x[i], ty = y[i];
                x[i] = x[j]; y[i] = y[j];
                x[j] = tx; y[j] = ty;
            }
            int n1 = n2;
            while(n1 <= j) {
                j -= n1;
                n1 >>= 1; // n1 /= 2
            }
            j += n1;
        }
        // Compute the FFT
        float c1 = -1, c2 = 0;
        int n4 = 1;
        for(int i = 0; i < m; i++) {
            int n3 = n4;
            n4 <<= 1; // c *= 2;
            float u1 = 1, u2 = 0;
            for(int j = 0; j < n3; j++) {
                for(int k = j; k < n; k += n4) {
                    int a = k + n3;
                    float t1 = u1*x[a] - u2*y[a];
                    float t2 = u1*y[a] + u2*x[a];
                    x[a] = x[k]-t1;
                    y[a] = y[k]-t2;
                    x[k] += t1;
                    y[k] += t2;
                }
                float z = u1*c1 - u2*c2;
                u2 = u1*c2 + u2*c1;
                u1 = z;
            }
        }
    }
    
    // ==================== 2D Part ====================
    
    // 1. dim: x-es mit nx, 2. dim: y-sis mit ny
    public void fFFT2D(float[][] real, float[][] imag) {
        if(real.length != imag.length)
            throw new IllegalArgumentException(
                "fFFT2D: length of real and imaginary arrays (x dimension) must be equal!" +
                " (real: "+real.length+" imag:"+imag.length+")");
        assert real.length > 0;
        assert imag.length > 0;
        if(real[0].length != imag[0].length)
            throw new IllegalArgumentException(
                "fFFT2D: length of real and imaginary arrays (y dimension) must be equal!" +
                " (real: "+real[0].length+" imag:"+imag[0].length+")");
        int nx = real.length;
        int ny = real[0].length;
        if(nx != ny)
            throw new IllegalArgumentException(
                "iFFT2D: length of row and column arrays must be equal! (row: "+nx+" column:"+ny+")");
        setN(nx);
        // Transform the rows (nx)
        for(int j = 0; j < ny; j++) {
            for(int i = 0; i < nx; i++) {
                tmpReRow[i] = real[i][j];
                tmpImRow[i] = imag[i][j];
            }
            fFFT1D(tmpReRow, tmpImRow);
            for(int i = 0; i < nx; i++) {
                real[i][j] = tmpReRow[i];
                imag[i][j] = tmpImRow[i];
            }
        }
        // Transform the columns (ny)
        for(int i = 0; i < nx; i++) {
            for(int j = 0; j < ny; j++) {
                tmpReRow[j] = real[i][j];
                tmpImRow[j] = imag[i][j];
            }
            fFFT1D(tmpReRow, tmpImRow);
            for(int j = 0; j < ny; j++) {
                real[i][j] = tmpReRow[j];
                imag[i][j] = tmpImRow[j];
            }
        }
    }
    
    // OPT: Create additional caching tables
    
    // 1. dim: x-es mit nx, 2. dim: y-sis mit ny
    public void iFFT2D(float[][] real, float[][] imag) {
        if(real.length != imag.length)
            throw new IllegalArgumentException(
                "iFFT2D: length of real and imaginary arrays (x dimension) must be equal!" +
                " (real: "+real.length+" imag:"+imag.length+")");
        if(real[0].length != imag[0].length)
            throw new IllegalArgumentException(
                "iFFT2D: length of real and imaginary arrays (y dimension) must be equal!" +
                " (real: "+real[0].length+" imag:"+imag[0].length+")");
        int nx = real.length;
        int ny = real[0].length;
        if(nx != ny)
            throw new IllegalArgumentException(
                "iFFT2D: length of row and column arrays must be equal! (row: "+nx+" column:"+ny+")");
        setN(nx);
        // Transform the rows (nx)
        for(int j = 0; j < ny; j++) {
            for(int i = 0; i < nx; i++) {
                tmpReRow[i] = real[i][j];
                tmpImRow[i] = imag[i][j];
            }
            iFFT1D(tmpReRow, tmpImRow);
            for(int i = 0; i < nx; i++) {
                real[i][j] = tmpReRow[i];
                imag[i][j] = tmpImRow[i];
            }
        }
        // Transform the columns (ny)
        for(int i = 0; i < nx; i++) {
            for(int j = 0; j < ny; j++) {
                tmpReRow[j] = real[i][j];
                tmpImRow[j] = imag[i][j];
            }
            iFFT1D(tmpReRow, tmpImRow);
            for(int j = 0; j < ny; j++) {
                real[i][j] = tmpReRow[j];
                imag[i][j] = tmpImRow[j];
            }
        }
    }
    
    // 1. dim: x-es mit nx, 2. dim: y-sis mit ny
    public void fFFT2D(Tuple2f[][] complex) {
        int nx = complex.length;
        int ny = complex[0].length;
        if(nx != ny)
            throw new IllegalArgumentException(
                "iFFT2D: length of row and column arrays must be equal! (row: "+nx+" column:"+ny+")");
        setN(nx);
        // Transform the rows (nx)
        for(int j = 0; j < ny; j++) {
            for(int i = 0; i < nx; i++) {
                tmpReRow[i] = complex[i][j].x;
                tmpImRow[i] = complex[i][j].y;
            }
            fFFT1D(tmpReRow, tmpImRow);
            for(int i = 0; i < nx; i++) {
                complex[i][j].x = tmpReRow[i];
                complex[i][j].y = tmpImRow[i];
            }
        }
        // Transform the columns (ny)
        for(int i = 0; i < nx; i++) {
            for(int j = 0; j < ny; j++) {
                tmpReCol[j] = complex[i][j].x;
                tmpImCol[j] = complex[i][j].y;
            }
            fFFT1D(tmpReCol, tmpImCol);
            for(int j = 0; j < ny; j++) {
                complex[i][j].x = tmpReCol[j];
                complex[i][j].y = tmpImCol[j];
            }
        }
    }    
    
    // 1. dim: x-es mit nx, 2. dim: y-sis mit ny
    public void iFFT2D(Tuple2f[][] complex) {
        int nx = complex.length;
        assert complex.length > 0;
        int ny = complex[0].length;
        if(nx != ny)
            throw new IllegalArgumentException(
                "iFFT2D: length of row and column arrays must be equal! (row: "+nx+" column:"+ny+")");
        setN(nx);
        // Transform the rows (nx)
        for(int j = 0; j < ny; j++) {
            for(int i = 0; i < nx; i++) {
                tmpReRow[i] = complex[i][j].x;
                tmpImRow[i] = complex[i][j].y;
            }
            iFFT1D(tmpReRow, tmpImRow);
            for(int i = 0; i < nx; i++) {
                complex[i][j].x = tmpReRow[i];
                complex[i][j].y = tmpImRow[i];
            }
        }
        // Transform the columns (ny)
        for(int i = 0; i < nx; i++) {
            for(int j = 0; j < ny; j++) {
                tmpReCol[j] = complex[i][j].x;
                tmpImCol[j] = complex[i][j].y;
            }
            iFFT1D(tmpReCol, tmpImCol);
            for(int j = 0; j < ny; j++) {
                complex[i][j].x = tmpReCol[j];
                complex[i][j].y = tmpImCol[j];
            }
        }
    }
    
    // 1. dim: x-es mit nx, 2. dim: y-sis mit ny
    public void iFFT2D(Vector2f[][] complex) {
        int nx = complex.length;
        assert complex.length > 0;
        int ny = complex[0].length;
        if(nx != ny)
            throw new IllegalArgumentException(
                "iFFT2D: length of row and column arrays must be equal! (row: "+nx+" column:"+ny+")");
        setN(nx);
        // Transform the rows (nx)
        for(int j = 0; j < ny; j++) {
            for(int i = 0; i < nx; i++) {
                tmpReRow[i] = complex[i][j].x;
                tmpImRow[i] = complex[i][j].y;
            }
            iFFT1D(tmpReRow, tmpImRow);
            for(int i = 0; i < nx; i++) {
                complex[i][j].x = tmpReRow[i];
                complex[i][j].y = tmpImRow[i];
            }
        }
        // Transform the columns (ny)
        for(int i = 0; i < nx; i++) {
            for(int j = 0; j < ny; j++) {
                tmpReCol[j] = complex[i][j].x;
                tmpImCol[j] = complex[i][j].y;
            }
            iFFT1D(tmpReCol, tmpImCol);
            for(int j = 0; j < ny; j++) {
                complex[i][j].x = tmpReCol[j];
                complex[i][j].y = tmpImCol[j];
            }
        }
    }
}
