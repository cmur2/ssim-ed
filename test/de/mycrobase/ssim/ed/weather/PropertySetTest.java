package de.mycrobase.ssim.ed.weather;

import static org.junit.Assert.*;

import org.junit.Test;

import com.jme3.math.Vector3f;

public class PropertySetTest {
    
    private static final float weakEps = 1e-4f;
    
    @Test
    public void testName() {
        PropertySet ps = new PropertySet("foo");
        assertEquals("foo", ps.getName());
    }
    
    @Test
    public void testPut() {
        PropertySet ps = new PropertySet("test");
        ps.put("ocean.temp", 20.5f, Float.class);
        ps.put("wind", new Vector3f(1,0,0), Vector3f.class);
        assertEquals(20.5f, (float) (Float) ps.get("ocean.temp"), weakEps);
        assertTrue(((Vector3f)ps.get("wind")).distance(new Vector3f(1,0,0)) < weakEps);
    }

    @Test
    public void testGet() {
        PropertySet ps = new PropertySet("test");
        ps.put("ocean.temp", 20.5f, Float.class);
        assertEquals(20.5f, (float) (Float) ps.get("ocean.temp"), weakEps);
        assertNull(ps.get("wind"));
    }

    @Test
    public void testGetAs() {
        PropertySet ps = new PropertySet("test");
        ps.put("ocean.temp", 20.5f, Float.class);
        assertEquals(20.5f, ps.getAs("ocean.temp", Float.class), weakEps);
        assertNull(ps.getAs("wind", Vector3f.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAsFail() {
        PropertySet ps = new PropertySet("test");
        ps.put("ocean.temp", 20.5f, Float.class);
        ps.getAs("ocean.temp", String.class);
    }

    @Test
    public void testGetClassOf() {
        PropertySet ps = new PropertySet("test");
        ps.put("ocean.temp", 20.5f, Float.class);
        assertEquals(Float.class, ps.getClassOf("ocean.temp"));
        assertNull(ps.getClassOf("wind"));
    }

    @Test
    public void testSet() {
        PropertySet ps = new PropertySet("test");
        ps.put("ocean.temp", 20.5f, Float.class);
        
        assertEquals(20.5f, ps.getAs("ocean.temp", Float.class), weakEps);
        ps.set("ocean.temp", 33.5f);
        assertEquals(33.5f, ps.getAs("ocean.temp", Float.class), weakEps);
        
        ps.set("wind", new Vector3f(0, 1, 1));
    }


    @Test
    public void testIterator() {
        PropertySet ps = new PropertySet("test");
        ps.put("ocean.temp", 20.5f, Float.class);
        for(PropertySet.Entry e : ps) {
            assertEquals("ocean.temp", e.getKey());
            assertEquals(20.5f, (float) (Float) e.getValue(), weakEps);
            assertEquals(Float.class, e.getClazz());
        }
    }
}
