package sed;

import java.io.IOException;

import org.apache.log4j.Logger;

import sed.terrain.BinaryMap;

import chlib.streams.PrimitiveReader;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;

public class MapLoader implements AssetLoader {

    public static final float ScaleXZ = 1/25f;
    public static final float ScaleY = 1f;
    
    private static final Logger logger = Logger.getLogger(MapLoader.class);

    private static final int MaxNameLength = 12;
    
    @Override
    public Object load(AssetInfo assetInfo) throws IOException {
        PrimitiveReader reader = new PrimitiveReader(assetInfo.openStream());
        
        int version = reader.readInt();
        logger.info("Identified map version: "+version);
        
        BinaryMap map = null;
        if(version == 1) {
            // file header
            StringBuffer name = new StringBuffer(MaxNameLength);
            for(int i = 0; i < MaxNameLength; i++) {
                name.append(reader.readChar());
            }
            double weDiff = reader.readDouble()*ScaleXZ;
            double nsDiff = reader.readDouble()*ScaleXZ;
            int weNum = reader.readInt();
            int nsNum = reader.readInt();
            
            // file body
            float[][] elevs = new float[nsNum][weNum];
            for(int i = 0; i < nsNum; i++) {
                for(int j = 0; j < weNum; j++) {
                    elevs[i][j] = (float) (reader.readShort()) * ScaleY;
                }
            }
            
            map = new BinaryMap(weDiff, nsDiff, weNum, nsNum, elevs, name.toString().trim());
            
            logger.info(String.format("Loaded map: %s", map.toString()));
        } else {
            logger.fatal(String.format("Error reading map '%s': unknown map version %d",
                assetInfo.getKey().toString(), version));
            map = null;
        }
        
        return map;
    }
}
