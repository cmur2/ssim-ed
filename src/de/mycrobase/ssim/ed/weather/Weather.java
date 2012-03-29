package de.mycrobase.ssim.ed.weather;

import com.jme3.math.Vector3f;

public interface Weather {
    
    /**
     * Queries the value of the given property key.
     * 
     * @throws IllegalArgumentException if property has not the specified type
     * @param key used to find the value
     * @return the value or {@code null} if property doesn't exist
     */
    public Float getFloat(String key);
    
    /**
     * Queries the value of the given property key.
     * 
     * @throws IllegalArgumentException if property has not the specified type
     * @param key used to find the value
     * @return the value or {@code null} if property doesn't exist
     */
    public Vector3f getVec3(String key);
    
    /**
     * Queries the value of the given property key.
     * 
     * @throws IllegalArgumentException if property has not the specified type
     * @param key used to find the value
     * @return the value or {@code null} if property doesn't exist
     */
    public Integer getInt(String key);
    
    /**
     * Queries the value of the given property key.
     * 
     * @throws IllegalArgumentException if property has not the specified type
     * @param key used to find the value
     * @return the value or {@code null} if property doesn't exist
     */
    public Integer[] getIntArray(String key);
    
    /**
     * Queries the value of the given property key.
     * 
     * @throws IllegalArgumentException if property has not the specified type
     * @param key used to find the value
     * @return the value or {@code null} if property doesn't exist
     */
    public Boolean getBool(String key);
}
