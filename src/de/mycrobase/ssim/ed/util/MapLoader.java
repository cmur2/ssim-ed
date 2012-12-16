package de.mycrobase.ssim.ed.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.log4j.Logger;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;
import com.jme3.export.binary.ByteUtils;

import de.mycrobase.ssim.ed.terrain.BinaryMap;

public class MapLoader implements AssetLoader {

    public static final float ScaleXZ = 1f;
    public static final float ScaleY = 1f;
    
    private static final Logger logger = Logger.getLogger(MapLoader.class);

    private static final int MaxNameLength = 12;
    
    @Override
    public Object load(AssetInfo assetInfo) throws IOException {
        // use jME ByteUtils to read in the stream at once and wrap it into a buffer
        ByteBuffer buffer = ByteBuffer.wrap(ByteUtils.getByteContent(assetInfo.openStream()));
        // map files are little endian
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        int version = buffer.getInt();
        logger.info("Identified map version: "+version);
        
        BinaryMap map = null;
        if(version == 1) {
            // file header
            StringBuffer name = new StringBuffer(MaxNameLength);
            for(int i = 0; i < MaxNameLength; i++) {
                name.append(buffer.getChar());
            }
            double weDiff = buffer.getDouble()*ScaleXZ;
            double nsDiff = buffer.getDouble()*ScaleXZ;
            int weNum = buffer.getInt();
            int nsNum = buffer.getInt();
            
            // file body
            float[][] elevs = new float[nsNum][weNum];
            for(int i = 0; i < nsNum; i++) {
                for(int j = 0; j < weNum; j++) {
                    elevs[i][j] = (float) (buffer.getShort()) * ScaleY;
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
