package de.mycrobase.ssim.ed.app;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioNode;
import com.jme3.scene.Node;

import de.mycrobase.ssim.ed.weather.Weather;

public class AudioAppState extends BasicAppState {

    private static final float UpdateInterval = 0.1f; // in seconds
    
    // exists only while AppState is attached
    private Node envAudio;
    private AudioNode wind;
    private AudioNode rain;
    
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
//        
//        rain = new AudioNode(getApp().getAssetManager(), "audio/rain-heavy-01.wav", false);
//        rain.setLooping(true);
//        rain.setPositional(false);
//        envAudio.attachChild(rain);
//        
//        rain.play();
//        wind.play();
    }
    
    @Override
    public void update(float dt) {
        super.update(dt);
        
        // update listener (OpenAL term for ears) by camera position
        getApp().getListener().setLocation(getApp().getCamera().getLocation());
        getApp().getListener().setRotation(getApp().getCamera().getRotation());
    }
    
    @Override
    protected void intervalUpdate(float dt) {
//        wind.setVolume(getWeather().getFloat("wind.strength")/10f);
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        wind = null;
    }
    
    private Weather getWeather() {
        return getState(WeatherAppState.class).getWeather();
    }
}
