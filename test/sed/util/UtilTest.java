package sed.util;

import static org.junit.Assert.*;

import org.junit.Test;

import sed.util.Util;

public class UtilTest {
    
    @Test
    public void testUnpackUnsignedByte() {
        for(int i = 0; i < 256; i++) {
            assertEquals(i, Util.unpackUnsignedByte((byte) i));
        }
    }
}
