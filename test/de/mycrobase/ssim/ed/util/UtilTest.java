package de.mycrobase.ssim.ed.util;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import de.mycrobase.ssim.ed.helper.categories.Fast;

@Category(Fast.class)
public class UtilTest {
    
    @Test
    public void testUnpackUnsignedByte() {
        for(int i = 0; i < 256; i++) {
            assertEquals(i, Util.unpackUnsignedByte((byte) i));
        }
    }
}
