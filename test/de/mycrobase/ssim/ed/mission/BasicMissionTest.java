package de.mycrobase.ssim.ed.mission;

import static org.junit.Assert.*;

import org.junit.Test;

public class BasicMissionTest {
    
    @Test
    public void testToString() {
        assertFalse(new BasicMission("lala").toString().isEmpty());
    }
}
