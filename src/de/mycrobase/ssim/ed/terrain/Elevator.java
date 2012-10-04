package de.mycrobase.ssim.ed.terrain;

import ssim.util.MathExt;

public class Elevator {
    
    private BinaryMap map;
    private float defaultElevation;
    
    public Elevator(BinaryMap map, float defaultElevation) {
        this.map = map;
        this.defaultElevation = defaultElevation;
    }

    public float getElevation(float z, float x) {
        double gridx = x / map.weDiff;
        double gridz = z / map.nsDiff;
        int x0 = (int) Math.floor(gridx);
        int z0 = (int) Math.floor(gridz);
        int x1 = x0 + 1;
        int z1 = z0 + 1;
        // y10 --- y11
        //  |       |
        // y00 --- y01
        float y00 = getElevation(z0, x0);
        float y01 = getElevation(z0, x1);
        float y10 = getElevation(z1, x0);
        float y11 = getElevation(z1, x1);
        float r1 = (float) MathExt.frac(gridx-x0);
        float r2 = (float) MathExt.frac(gridz-z0);
        double y1 = MathExt.interpolateLinear(y00, y01, r1);
        double y2 = MathExt.interpolateLinear(y10, y11, r1);
        return (float) MathExt.interpolateLinear(y1, y2, r2);
//        double y1 = MathExt.interpolateCosine(y00, y01, r1);
//        double y2 = MathExt.interpolateCosine(y10, y11, r1);
//        return (float) MathExt.interpolateCosine(y1, y2, r2);
    }
    
    public float getElevation(int iz, int ix) {
        if(iz >= 0 && ix >= 0 && iz < map.nsNum && ix < map.weNum) {
            return map.elevs[iz][ix];
        } else {
            return defaultElevation;
        }
    }
}
