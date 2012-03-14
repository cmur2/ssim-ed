package sed;

import java.io.IOException;

import org.apache.log4j.Logger;

import sed.terrain.BinaryMap;

import chlib.streams.PrimitiveReader;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;

public class MapLoader implements AssetLoader {
    
    private static final Logger logger = Logger.getLogger(MapLoader.class);
    
    public static final float scaleXZ = 1/50f;
    
    @Override
    public Object load(AssetInfo assetInfo) throws IOException {
        PrimitiveReader reader = new PrimitiveReader(assetInfo.openStream());
        
        int version = reader.readInt();
        logger.info("Identified map version: "+version);
        
        BinaryMap map = null;
        if(version == 1) {
            // file header
            StringBuffer name = new StringBuffer(12);
            for(int i = 0; i < 12; i++) {
                name.append(reader.readChar());
            }
            float woDiff = (float) reader.readDouble()*scaleXZ;
            float nsDiff = (float) reader.readDouble()*scaleXZ;
            int woNum = reader.readInt();
            int nsNum = reader.readInt();
            
            logger.info("Map '"+name+"' properties: WO: "+woDiff+" m * "+woNum+" tiles; NS: "+nsDiff+" m * "+nsNum+" tiles");
            
            // file body
            logger.info("Create and load map data field, will occupy approx. "+Math.round(nsNum*woNum*2/1024f)+" KiB of memory");
            short[][] elevs = new short[nsNum][woNum];
            for(int i = 0; i < nsNum; i++) {
                for(int j = 0; j < woNum; j++) {
                    elevs[i][j] = reader.readShort();
                }
            }
            
            map = new BinaryMap(elevs, woDiff, nsDiff, woNum, nsNum);
        } else {
            logger.fatal("Error reading map '"+assetInfo.getKey()+"': unknown map version "+version);
            map = null;
        }
        
        return map;
    }
}
