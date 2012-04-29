package de.mycrobase.ssim.ed.input;

import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyInputMapping;
import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;

public class AnyKeyInputMapping implements NiftyInputMapping {
    
    /** {@inheritDoc} */
    public NiftyInputEvent convert(final KeyboardInputEvent inputEvent) {
        if(inputEvent.isKeyDown()) {
            return NiftyInputEvent.Activate;
        }
        return null;
    }
}
