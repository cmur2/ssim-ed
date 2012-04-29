package de.mycrobase.ssim.ed.util.lang;

import org.jdom.Element;

import com.jme3.asset.AssetKey;
import com.jme3.asset.AssetManager;
import com.jme3.asset.AssetNotFoundException;

import de.altimos.util.translator.TranslationSource;
import de.altimos.util.translator.Translator;

public class XmlTranslation extends de.altimos.util.translator.XmlTranslation {
    
    private AssetManager assetManager;
    
    public XmlTranslation(AssetManager assetManager) {
        super();
        this.assetManager = assetManager;
    }
    
    public XmlTranslation(AssetManager assetManager, String delimiter) {
        super(delimiter);
        this.assetManager = assetManager;
    }
    
    /**
     * Completely override the {@link XmlTranslation#loadRoot(Translator, String, String)}
     * since we want to replace the {@link TranslationSource} XML loading
     * functionality with one that uses the {@link AssetManager} and it's
     * registered loaders.
     */
    @Override
    protected Element loadRoot(Translator translator, String locale, String domain) {
        if(domain.length() > 0 && locale.length() > 0) {
            domain += "_";
        }
        try {
            return assetManager.loadAsset(new AssetKey<Element>(domain + locale + ".xml"));
        } catch(AssetNotFoundException ex) {
            return null;
        }
    }
}
