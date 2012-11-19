package de.mycrobase.ssim.ed;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioRenderer;
import com.jme3.input.InputManager;
import com.jme3.niftygui.NiftyJmeDisplay;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;

public class ReentrantNiftyJmeDisplay extends NiftyJmeDisplay {
    
    // after executing the constructor, the rawInputListener is present once!
    private boolean rawInputListenerAdded;
    
    // TODO: report bug in NiftyJmeDisplay upstream #jme-upgrade
    
    public ReentrantNiftyJmeDisplay(AssetManager assetManager, 
            InputManager inputManager,
            AudioRenderer audioRenderer,
            ViewPort vp)
    {
        super(assetManager, inputManager, audioRenderer, vp);
        
        rawInputListenerAdded = true;
    }
    
    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
        if(inputManager != null && !rawInputListenerAdded) {
            inputManager.addRawInputListener(inputSys);
        }
        
        super.initialize(rm, vp);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        rawInputListenerAdded = false;
    }
}
