package sed.terrain;

public class BinaryMap {
    
    public double woDiff;
    public double nsDiff;
    public int woNum;
    public int nsNum;

    public short[][] elevs;
    public String name;
    
    public BinaryMap(double woDiff, double nsDiff, int woNum, int nsNum, short[][] elevs, String name) {
        this.woDiff = woDiff;
        this.nsDiff = nsDiff;
        this.woNum = woNum;
        this.nsNum = nsNum;
        this.elevs = elevs;
        this.name = name;
    }
    
    @Override
    public String toString() {
        return String.format("Map(\"%s\", WE: %gm * %d, NS: %gm * %d)", name, woDiff, woNum, nsDiff, nsNum);
    }
}
