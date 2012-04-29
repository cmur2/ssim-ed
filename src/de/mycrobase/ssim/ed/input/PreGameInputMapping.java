package de.mycrobase.ssim.ed.input;

import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.input.NiftyInputMapping;
import de.lessvoid.nifty.input.keyboard.KeyboardInputEvent;

public class PreGameInputMapping implements NiftyInputMapping {
    
    /** {@inheritDoc} */
    public NiftyInputEvent convert(final KeyboardInputEvent inputEvent) {
        if(inputEvent.isKeyDown()) {
            if(inputEvent.getKey() == KeyboardInputEvent.KEY_RETURN) {
                return NiftyInputEvent.Activate;
            } else if(inputEvent.getKey() == KeyboardInputEvent.KEY_SPACE) {
                return NiftyInputEvent.Activate;
            }
            else if(inputEvent.getKey() == KeyboardInputEvent.KEY_ESCAPE) {
                return NiftyInputEvent.Escape;
            }
            else if(inputEvent.getKey() == KeyboardInputEvent.KEY_LEFT) {
                return NiftyInputEvent.MoveCursorLeft;
            } else if(inputEvent.getKey() == KeyboardInputEvent.KEY_RIGHT) {
                return NiftyInputEvent.MoveCursorRight;
            } else if(inputEvent.getKey() == KeyboardInputEvent.KEY_UP) {
                return NiftyInputEvent.MoveCursorUp;
            } else if(inputEvent.getKey() == KeyboardInputEvent.KEY_DOWN) {
                return NiftyInputEvent.MoveCursorDown;
            }
            else if(inputEvent.getKey() == KeyboardInputEvent.KEY_TAB) {
                if(inputEvent.isShiftDown()) {
                    return NiftyInputEvent.PrevInputElement;
                } else {
                    return NiftyInputEvent.NextInputElement;
                }
            }
        }
        return null;
    }
}
