package de.mycrobase.ssim.ed.weather.ext;

import org.apache.log4j.Logger;

import de.mycrobase.ssim.ed.weather.BasicWeatherController;
import de.mycrobase.ssim.ed.weather.PropertySet;
import de.mycrobase.ssim.ed.weather.WeatherInterpolator;
import de.mycrobase.ssim.ed.weather.WeatherProperty;

public class AlternateWeatherController extends BasicWeatherController {
    
    private static final Logger logger = Logger.getLogger(AlternateWeatherController.class);
    
    private float intervalTime;
    private String[] weatherNames;
    private WeatherProperty[] properties;
    
    private float time;
    
    private int current;
    private int next;
    private PropertySet currentValues;
    private PropertySet nextValues;
    
    public AlternateWeatherController(float intervalTime, String[] weatherNames, WeatherProperty[] properties) {
        this.intervalTime = intervalTime;
        this.weatherNames = weatherNames;
        this.properties = properties;
        
        logger.debug("Given "+weatherNames.length+" weathers");
        
        chooseNext(0, null);
        logger.debug("Initial weather: "+getCurrentWeather());
        logger.debug("New weather: "+getNextWeather());
        
        for(PropertySet.Entry e : currentValues) {
            state.put(e.getKey(), e.getValue(), e.getClazz());
        }
    }
    
    @Override
    public void update(float dt) {
        if(time >= intervalTime) {
            time -= intervalTime;
            
            chooseNext(next, nextValues);
            logger.debug("New weather: "+getNextWeather());
        }
        
        float ratio = time/intervalTime;
        
        for(WeatherProperty p : properties) {
            String key = p.getKey();

            WeatherInterpolator wi = getInterpolator(key);
            if(wi == null) {
                throw new UnsupportedOperationException(
                    String.format("No interpolator found for property %s!", key));
            }
            
            Object res = wi.interpolate(currentValues.get(key), nextValues.get(key), ratio);
            state.set(key, res);
        }
        
        time += dt;
    }
    
    private void chooseNext(int newCurrent, PropertySet newCurrentValues) {
        // choose new weather
        current = newCurrent;
        next = (current+1) % weatherNames.length;
        // get values for chosen weather
        currentValues = newCurrentValues == null ?
            getValues(getCurrentWeather()) : newCurrentValues;
        nextValues = getValues(getNextWeather());
    }
    
    private String getCurrentWeather() {
        return weatherNames[current];
    }
    
    private String getNextWeather() {
        return weatherNames[next];
    }
    
    private PropertySet getValues(String weather) {
        PropertySet ps = new PropertySet(weather);
        // query each WeatherProperty for it's value for the given weather
        for(WeatherProperty p : properties) {
            ps.put(p.getKey(), p.getValue(weather), p.getType());
        }
        return ps;
    }
}
