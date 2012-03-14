package sed.terrain;

public class BinaryMap {
    
    public short[][] elevs;
    public float woDiff;
    public float nsDiff;
    public int woNum;
    public int nsNum;

    public BinaryMap(short[][] elevs, float woDiff, float nsDiff, int woNum, int nsNum) {
        this.elevs = elevs;
        this.woDiff = woDiff;
        this.nsDiff = nsDiff;
        this.woNum = woNum;
        this.nsNum = nsNum;
    }
    
    @Override
    public String toString() {
        return "Map(WO: "+woDiff+" m * "+woNum+" tiles; NS: "+nsDiff+" m * "+nsNum+" tiles)";
    }
}
