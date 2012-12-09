package de.mycrobase.ssim.ed.helper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import com.jme3.asset.AssetInfo;
import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetLoadException;
import com.jme3.asset.AssetLocator;
import com.jme3.asset.AssetManager;

/**
 * Use this special AssetLocator when you need to serve assets from
 * memory/strings e.g. for test cases.
 * 
 * @author cn
 */
public class InMemoryLocator implements AssetLocator {
    
    private static HashMap<String, String> files = new HashMap<String, String>();
    
    private String rootPath = "";
    
    public InMemoryLocator() {
    }
    
    @Override
    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
        if(rootPath.equals("/")) {
            rootPath = "";
        } else if(rootPath.length() > 1){
            if(rootPath.startsWith("/")){
                rootPath = rootPath.substring(1);
            }
            if(!rootPath.endsWith("/")) {
                rootPath += "/";
            }
        }
    }
    
    @Override
    public AssetInfo locate(AssetManager manager, AssetKey key) {
        String path = key.getName();
        if(path.startsWith("/")) {
            path = path.substring(1);
        }
        path = rootPath + path;
        if(!files.containsKey(path)) {
            return null;
        }
        return new InMemoryAssetInfo(manager, key, files.get(path));
    }
    
    public static void registerFile(String path, String content) {
        files.put(path, content);
    }
    
    private static class InMemoryAssetInfo extends AssetInfo {
        
        private String content;
        
        public InMemoryAssetInfo(AssetManager manager, AssetKey<?> key, String content) {
            super(manager, key);
            this.content = content;
        }
        
        @Override
        public InputStream openStream() {
            try {
                return new ByteArrayInputStream(content.getBytes("UTF-8"));
            } catch(UnsupportedEncodingException ex) {
                throw new AssetLoadException("Failed to convert asset content to UTF-8", ex);
            }
        }
    }
}
