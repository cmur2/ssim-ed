package sed.app;

import sed.weather.Interpolators;
import sed.weather.PropertySet;
import sed.weather.RandomWeatherController;
import sed.weather.Weather;
import sed.weather.WeatherController;
import sed.weather.WindInterpolator;
import sed.weather.XMLPropertySetBuilder;

import com.jme3.app.Application;
import com.jme3.app.state.AppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector3f;

/**
 * <b>Base layer</b> {@link AppState} exposing the {@link Weather} interface
 * for all interested entities and responsible for weather changes.
 * 
 * @author cn
 */
public class WeatherAppState extends BasicAppState {
    
    private String[] sets;
    
    // exists only while AppState is attached
    private WeatherController weatherController;
    
    public WeatherAppState(String... sets) {
        this.sets = sets;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);

        XMLPropertySetBuilder builder = new XMLPropertySetBuilder(getApp().getAssetManager(), sets);
        builder.putFloat("sky.turbidity");
        builder.putVec3("sky.light");
        builder.putBool("sun.lensflare.enabled");
        builder.putFloat("sun.lensflare.shininess");
        builder.putFloat("cloud.cover");
        builder.putFloat("cloud.sharpness");
        builder.putFloat("cloud.way-factor");
        builder.putInt("cloud.zoom");
        builder.putFloat("wind.direction");
        builder.putFloat("wind.strength");
        PropertySet[] ps = builder.getResults();
        
        // TODO: sky.turbidity should be used to determine fog thickness, aerial parameters
        // TODO: sky.turbidity -> sky.air?
        // TODO: add precipitation parameters
        // TODO: add air temperature and maybe air pressure
        
        weatherController = new RandomWeatherController(5 * 60f, ps);
        weatherController.registerInterpolator(new Interpolators.FloatInterpolator(), Float.class);
        weatherController.registerInterpolator(new Interpolators.BoolInterpolator(), Boolean.class);
        weatherController.registerInterpolator(new Interpolators.Vec3Interpolator(), Vector3f.class);
        weatherController.registerInterpolator(new Interpolators.IntInterpolator(), Integer.class);
        
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
