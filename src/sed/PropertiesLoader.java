package sed;

import java.io.IOException;
import java.util.Properties;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;

public class PropertiesLoader implements AssetLoader {
    
    @Override
    public Object load(AssetInfo assetInfo) throws IOException {
        Properties properties = new Properties();
        properties.load(assetInfo.openStream());
        return properties;
    }
}
