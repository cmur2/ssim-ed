package de.mycrobase.ssim.ed.terrain;

public class BinaryMap {
    
    public double weDiff;
    public double nsDiff;
    public int weNum;
    public int nsNum;

    public float[][] elevs;
    public String name;
    
    public BinaryMap(double weDiff, double nsDiff, int weNum, int nsNum, float[][] elevs, String name) {
        this.weDiff = weDiff;
        this.nsDiff = nsDiff;
        this.weNum = weNum;
        this.nsNum = nsNum;
        this.elevs = elevs;
        this.name = name;
    }
    
    @Override
    public String toString() {
        return String.format("Map(\"%s\", WE: %gm * %d, NS: %gm * %d)", name, weDiff, weNum, nsDiff, nsNum);
    }
}
