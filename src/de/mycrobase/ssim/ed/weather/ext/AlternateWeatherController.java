package de.mycrobase.ssim.ed.weather.ext;

import org.apache.log4j.Logger;

import de.mycrobase.ssim.ed.weather.BasicWeatherController;
import de.mycrobase.ssim.ed.weather.PropertySet;
import de.mycrobase.ssim.ed.weather.WeatherInterpolator;

public class AlternateWeatherController extends BasicWeatherController {
    
    private static final Logger logger = Logger.getLogger(AlternateWeatherController.class);
    
    private PropertySet[] sets;
    private float intervalTime;
    
    private float time;
    private int current;
    private int next;
    
    public AlternateWeatherController(float intervalTime, PropertySet... sets) {
        this.intervalTime = intervalTime;
        this.sets = sets;
        
        logger.debug("Given "+sets.length+" weathers");
        
        current = 0;
        next = (current+1) % sets.length;
        logger.debug("Initial weather: "+sets[current].getName());
        logger.debug("New weather: "+sets[next].getName());
        
        for(PropertySet.Entry e : sets[current]) {
            state.put(e.getKey(), e.getValue(), e.getClazz());
        }
    }
    
    @Override
    public void update(float dt) {
        if(time >= intervalTime) {
            time -= intervalTime;
            current = next;
            next = (current+1) % sets.length;
            System.out.println(current);
            System.out.println(next);
            logger.debug("New weather: "+sets[next].getName());
        }
        
        float ratio = time/intervalTime;
        
        for(PropertySet.Entry e : state) {
            String key = e.getKey();
            WeatherInterpolator wi = getInterpolator(key);
            if(wi == null) {
                throw new UnsupportedOperationException(String.format("No interpolator found for property %s!", key));
            }
            
            Object res = wi.interpolate(sets[current].get(key), sets[next].get(key), ratio);
            
            state.set(key, res);
        }
        
        time += dt;
    }
}
