package de.mycrobase.ssim.ed.app;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioNode;
import com.jme3.scene.Node;

import de.mycrobase.ssim.ed.weather.Weather;
import de.mycrobase.ssim.ed.weather.ext.PrecipitationType;

public class AudioAppState extends BasicAppState {

    private static final float UpdateInterval = 0.1f; // in seconds
    
    // exists only while AppState is attached
    private Node envAudio;
    private AudioNode wind;
    private AudioNode rainMedium;
    private AudioNode rainHeavy;
    
    public AudioAppState() {
        super(UpdateInterval);
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        envAudio = new Node("EnvAudio");
        getApp().getRootNode().attachChild(envAudio);
        
//        wind = new AudioNode(getApp().getAssetManager(), "audio/wind-01.wav", false);
//        wind.setLooping(true);
//        wind.setPositional(false);
//        envAudio.attachChild(wind);
//        updateWind();
        
        rainMedium = loadEnvSound("audio/rain-medium.ogg");
        rainHeavy = loadEnvSound("audio/rain-heavy.ogg");
        envAudio.attachChild(rainMedium);
        envAudio.attachChild(rainHeavy);
        updateRain();
    }
    
    // TODO: pause active sounds on GameMode.Paused?
    
    @Override
    public void update(float dt) {
        super.update(dt);
        
        // update listener (OpenAL term for ears) by camera position
        getApp().getListener().setLocation(getApp().getCamera().getLocation());
        getApp().getListener().setRotation(getApp().getCamera().getRotation());
    }
    
    @Override
    protected void intervalUpdate(float dt) {
        updateWind();
        updateRain();
    }
    
    @Override
    public void cleanup() {
        super.cleanup();

//        wind.stop();
        rainMedium.stop();
        rainHeavy.stop();
      
        getApp().getRootNode().detachChild(envAudio);
        
        envAudio = null;
        wind = null;
        rainMedium = null;
        rainHeavy = null;
    }

    private Weather getWeather() {
        return getState(WeatherAppState.class).getWeather();
    }
    
    private void updateWind() {
        //wind.setVolume(getWeather().getFloat("wind.strength")/10f);
        //wind.setVolume(getApp().getSettingsManager().getFloat("sound.effect.volume"));
    }
    
    private void updateRain() {
        rainMedium.setVolume(getApp().getSettingsManager().getFloat("sound.effect.volume"));
        rainHeavy.setVolume(getApp().getSettingsManager().getFloat("sound.effect.volume"));
        
        PrecipitationType curType = 
            PrecipitationType.fromId(getWeather().getInt("precipitation.form"));
        float intensity = getWeather().getFloat("precipitation.intensity");
        
        if(curType == PrecipitationType.Rain) {
            if(intensity >= 0.75f) {
                rainMedium.stop();
                rainHeavy.play();
            } else {
                rainHeavy.stop();
                rainMedium.play();
            }
        } else {
            // one of those is currently playing
            rainMedium.stop();
            rainHeavy.stop();
        }
    }
    
    private AudioNode loadEnvSound(String file) {
        AudioNode a = new AudioNode(
            getApp().getAssetManager(), file, false);
        a.setLooping(true);
        a.setPositional(false);
        return a;
    }
}
