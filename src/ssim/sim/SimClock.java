/*
 * SSim - a realtime Submarine Simulator
 *
 * Copyright (C) 2006-2011  Ch. Nicolai
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 3 only,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ssim.sim;

import org.apache.log4j.Logger;

/**
 * This is an independent component managing a simulations time
 * and it's elapse with (fractional!) millisecond precision.
 * The time scale raises up to number of elapsed hours.
 * <br/>
 * Creating a {@link SimClock} is possible by calling factory methods
 * like {@link #createClock(double)} or {@link #createClock(int, int)}.
 *
 * @author Ch. Nicolai
 */
public class SimClock {
    
    private static final Logger logger = Logger.getLogger(SimClock.class);
    
    private int hours = 0;
    private int minutes = 0;
    private int seconds = 0;
    private double millis = 0;
    
    /**
     * Private constructor without parameter checking!
     *
     * @param hours hours in range 0 to 23
     * @param minutes minutes in range 0 to 59
     */
    private SimClock(int hours, int minutes) {
        logger.info("Initialize simulation clock");
        assert hours >= 0 && hours < 24;
        assert minutes >= 0 && minutes < 60;
        this.hours = hours;
        this.minutes = minutes;
    }
    
    /**
     * Let the internal time move <b>forward</b> about the given amount
     * in seconds.
     *
     * @param stepInSeconds time step to be performed in seconds
     *                      (with fraction allowing millisecond steps),
     *                      negative parameter values are not allowed!
     */
    public void step(double stepInSeconds) {
        if(stepInSeconds < 0) {
            logger.warn(String.format(
                "SimClock encountered back tick (%s s)! Tick ignored.",
                stepInSeconds));
            return;
        }
        assert stepInSeconds >= 0;
        millis += stepInSeconds*1000;
        
        double newmillis = millis % 1000;
        seconds += (millis-newmillis)/1000;
        millis = newmillis;
        
        int newseconds = seconds % 60;
        minutes += (seconds-newseconds)/60;
        seconds = newseconds;
        
        int newminutes = minutes % 60;
        hours += (minutes-newminutes)/60;
        minutes = newminutes;
        
        int newhours = hours % 24;
        // ignore days
        hours = newhours;
    }
    
    /**
     * @return hours since midnight
     */
    public int getHours() {
        return hours;
    }
    
    /**
     * @return minutes since last full hour
     */
    public int getMinutes() {
        return minutes;
    }
    
    /**
     * @return seconds since last full minute
     */
    public int getSeconds() {
        return seconds;
    }
    
    /**
     * @return milliseconds since last full second
     */
    public long getMillis() {
        return (long) millis;
    }
    
    /**
     * @return seconds since midnight
     */
    public int secondTime() {
        return hours*3600+minutes*60+seconds;
    }
    
    /**
     * @return minutes since midnight
     */
    public int minuteTime() {
        return hours*60+minutes;
    }
    
    /**
     * @return hours since midnight (with fraction calculated from minutes)
     */
    public float hourTime() {
        return hours+minutes/60f;
    }
    
    @Override
    public String toString() {
        return String.format("SimClock(time state %d:%d:%d.%g)",
                hours, minutes, seconds, millis);
    }
    
    /**
     * This routine is an in-place formatter for minimizing
     * {@link SimClock}s dependencies. It allows to fetch the internal time
     * in a 'mixed' format.
     * <br/>
     * For example: an internal time of '1:30' delivers '0130.34'.
     *
     * @param withSeconds if true seconds part is appended as '.xx'
     *                    for xx seconds passed
     * @return the 'mixed' time
     */
    public String mixedTime(boolean withSeconds) {
        StringBuilder sb = new StringBuilder();
        if(hours < 10) {
            sb.append('0');
        }
        sb.append(hours);
        if(minutes < 10) {
            sb.append('0');
        }
        sb.append(minutes);
        if(withSeconds) {
            sb.append('.');
            if(seconds < 10) {
                sb.append('0');
            }
            sb.append(seconds);
        }
        return sb.toString();
    }
    
    // === Static Section ===
    
    public static String minuteTimeToMixedTime(int minuteTime) {
        StringBuilder sb = new StringBuilder();
        int hours = minuteTime/60;
        int minutes = (int) ((minuteTime/60f - hours)*60f);
        if(hours < 10) {
            sb.append('0');
        }
        sb.append(hours);
        if(minutes < 10) {
            sb.append('0');
        }
        sb.append(minutes);
        return sb.toString();
    }
    
    /**
     * Creates a new {@link SimClock} instance with given hour and minutes.
     *
     * @param hours hours in range 0 to 23
     * @param minutes minutes in range 0 to 59
     * @return the new SimClock instance
     */
    public static SimClock createClock(int hours, int minutes) {
        if(hours < 0 || hours > 23) {
            return null;
        }
        if(minutes < 0 || minutes > 59) {
            return null;
        }
        SimClock c = new SimClock(hours, minutes);
        logger.info(String.format("Internal start time directly given is %s",
            c.mixedTime(false)));
        return c;
    }
    
    /**
     * Creates a new {@link SimClock} instance with given time of day.
     *
     * @param timeOfDay (aka hourTime) the time of day since midnight
     *                  in the following format: 14.25 means 2:15 PM,
     *                  3.50 means 3:30 AM.<br/>
     *                  NOTE:
     *                  Only values in range <i>[0,24[</i> are accepted!
     * @return the new SimClock instance
     */
    public static SimClock createClock(double timeOfDay) {
        if(timeOfDay < 0 || timeOfDay >= 24) {
            return null;
        }
        int hours = (int)timeOfDay;
        int minutes = (int)((timeOfDay-hours)*60d);
        SimClock c = new SimClock(hours, minutes);
        logger.info(String.format(
            "Internal start time parsed from timeOfDay '%s' is %s",
            timeOfDay, c.mixedTime(false)));
        return c;
    }
}
