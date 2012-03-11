package sed.terrain;

import java.io.IOException;

import sed.MapLoader;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Vector3f;
import com.jme3.terrain.geomipmap.TerrainGridTileLoader;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.HeightMap;

public class BinaryMapTileLoader implements TerrainGridTileLoader {

    private MapLoader.Map map;
    
    private int patchSize;
    private int quadSize;
    
    public BinaryMapTileLoader(MapLoader.Map map) {
        this.map = map;
    }
    
    @Override
    public TerrainQuad getTerrainQuadAt(Vector3f location) {
        HeightMap map = getHeightMapAt(location);
        TerrainQuad tile = new TerrainQuad(
            "TerrainTile" + location.toString(), patchSize, quadSize,
            map == null ? null : map.getHeightMap());
        return tile;
    }

    @Override
    public void setPatchSize(int patchSize) {
        this.patchSize = patchSize;
    }

    @Override
    public void setQuadSize(int quadSize) {
        this.quadSize = quadSize;
    }
    
    @Override
    public void write(JmeExporter ex) throws IOException {
        // Hell why do you enforce this?!
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        // Hell why do you enforce this?!
    }
    
    private HeightMap getHeightMapAt(Vector3f location) {
        BinaryMapBasedHeightMap heightmap =
            new BinaryMapBasedHeightMap(map, location, quadSize);
        try {
            heightmap.load();
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return heightmap;
    }
}
