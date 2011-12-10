package sed;

import java.io.IOException;

import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetLoader;

public class XMLLoader implements AssetLoader {
    
    @Override
    public Object load(AssetInfo assetInfo) throws IOException {
        SAXBuilder builder = new SAXBuilder();
        try {
            return builder.build(assetInfo.openStream()).getRootElement();
        } catch(JDOMException ex) {
            throw new IOException(ex);
        }
    }
    
}
