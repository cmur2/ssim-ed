package de.mycrobase.ssim.ed.weather.ext;

import java.util.Random;

import org.apache.log4j.Logger;

import de.mycrobase.ssim.ed.weather.BasicWeatherController;
import de.mycrobase.ssim.ed.weather.WeatherInterpolator;

/**
 * 
 * @author cn
 */
public class RandomWeatherController extends BasicWeatherController {

    private static final Logger logger = Logger.getLogger(RandomWeatherController.class);
    
    private static Random random = new Random();
    
    private PropertySet[] sets;
    private float intervalTime;
    
    private float time;
    private PropertySet current;
    private PropertySet next;
    
    public RandomWeatherController(float intervalTime, PropertySet... sets) {
        this.intervalTime = intervalTime;
        this.sets = sets;
        
        logger.debug("Given "+sets.length+" weathers");
        
        current = selectRandom();
        next = selectRandom();
        logger.debug("Initial weather: "+current.getName());
        logger.debug("New weather: "+next.getName());
        
        for(PropertySet.Entry e : current) {
            state.put(e.getKey(), e.getValue(), e.getClazz());
        }
    }
    
    @Override
    public void update(float dt) {
        if(time >= intervalTime) {
            time -= intervalTime;
            current = next;
            next = selectRandom();
            logger.debug("New weather: "+next.getName());
        }
        
        float ratio = time/intervalTime;
        
        for(PropertySet.Entry e : state) {
            String key = e.getKey();
            WeatherInterpolator wi = getInterpolator(key);
            if(wi == null) {
                throw new UnsupportedOperationException(String.format("No interpolator found for property %s!", key));
            }
            
            Object res = wi.interpolate(current.get(key), next.get(key), ratio);
            state.set(key, res);
        }
        
        time += dt;
    }
    
    private PropertySet selectRandom() {
        return sets[random.nextInt(sets.length)];
    }
}
