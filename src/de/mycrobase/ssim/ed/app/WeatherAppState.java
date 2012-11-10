package de.mycrobase.ssim.ed.app;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;

import de.mycrobase.ssim.ed.weather.PropertySet;
import de.mycrobase.ssim.ed.weather.Weather;
import de.mycrobase.ssim.ed.weather.WeatherController;
import de.mycrobase.ssim.ed.weather.WeatherProperty;
import de.mycrobase.ssim.ed.weather.ext.AlternateWeatherController;
import de.mycrobase.ssim.ed.weather.ext.Generators;
import de.mycrobase.ssim.ed.weather.ext.Interpolators;
import de.mycrobase.ssim.ed.weather.ext.WindInterpolator;
import de.mycrobase.ssim.ed.weather.ext.XMLPropertySetBuilder;

/**
 * <b>Base layer</b> {@link AppState} exposing the {@link Weather} interface
 * for all interested entities and responsible for weather changes.
 * 
 * @author cn
 */
public class WeatherAppState extends BasicAppState {
    
    private String[] setNames;
    
    // exists only while AppState is attached
    private WeatherController weatherController;
    
    public WeatherAppState(String... setNames) {
        this.setNames = setNames;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);

        XMLPropertySetBuilder builder = new XMLPropertySetBuilder(getApp().getAssetManager(), setNames);
        builder.put("air.turbidity", Float.class);
        //builder.put("air.temperature", Float.class, new Generators.RandomFloatGenerator());
        //builder.put("air.pressure", Float.class, new Generators.RandomFloatGenerator());
        builder.put("sky.light", Vector3f.class);
        builder.put("sun.lensflare.enabled", Boolean.class);
        builder.put("sun.lensflare.shininess", Float.class);
        builder.put("ocean.a-factor", Float.class);
        builder.put("ocean.wave-cutoff", Float.class);
        builder.put("ocean.height-scale", Float.class);
        builder.put("ocean.choppiness", Float.class);
        builder.put("cloud.cover", Float.class);
        builder.put("cloud.sharpness", Float.class);
        builder.put("cloud.way-factor", Float.class);
        builder.put("precipitation.form", Integer.class);
        builder.put("precipitation.intensity", Float.class);
        builder.put("wind.direction", Float.class, new Generators.RandomFloatGenerator(0f, 360f));
        builder.put("wind.strength", Float.class, new Generators.RandomFloatGenerator(0f, 20f));
        
        weatherController = new AlternateWeatherController(5 * 60f, builder.getWeatherNames(), builder.getProperties());
        weatherController.registerInterpolator(new Interpolators.FloatInterpolator(), Float.class);
        weatherController.registerInterpolator(new Interpolators.DiscreteValueInterpolator(), Boolean.class);
        weatherController.registerInterpolator(new Interpolators.Vec3Interpolator(), Vector3f.class);
        weatherController.registerInterpolator(new Interpolators.IntInterpolator(), Integer.class);
        
        weatherController.registerInterpolator(new Interpolators.DiscreteValueInterpolator(), "precipitation.form");
        weatherController.registerInterpolator(new WindInterpolator(), "wind.direction");
    }
    
    @Override
    public void update(float dt) {
        // Don't call super since we don't need intervalUpdate()
        //super.update(dt);
        
        weatherController.update(dt);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        weatherController = null;
    }
    
    // public API
    
    /**
     * @return only the {@link Weather} part of the applications
     *         {@link WeatherController} since all other part should only
     *         read weather information
     */
    public Weather getWeather() {
        return weatherController;
    }
}
