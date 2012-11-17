package de.mycrobase.ssim.ed.app;

import ssim.util.MathExt;

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
    private float rainMediumVolume;
    private AudioNode rainHeavy;
    private float rainHeavyVolume;
    
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
        rainMedium.play();
        rainHeavy.play();
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
    
    private float getSoundEffectVolume() {
        return getApp().getSettingsManager().getFloat("sound.effect.volume");
    }
    
    private void updateWind() {
        //wind.setVolume(getWeather().getFloat("wind.strength")/10f);
        //wind.setVolume(getSoundEffectVolume());
    }
    
    private void updateRain() {
        PrecipitationType curType = 
            PrecipitationType.fromId(getWeather().getInt("precipitation.form"));
        float intensity = getWeather().getFloat("precipitation.intensity");
        
        if(curType == PrecipitationType.Rain) {
            System.out.println(intensity);
            rainMediumVolume = getRainMediumVolume(intensity);
            rainHeavyVolume  = getRainHeavyVolume(intensity);
        } else {
            rainMediumVolume = 0f;
            rainHeavyVolume  = 0f;
        }
        
        rainMedium.setVolume(rainMediumVolume * getSoundEffectVolume());
        rainHeavy.setVolume(rainHeavyVolume * getSoundEffectVolume());
    }
    
    private AudioNode loadEnvSound(String file) {
        AudioNode a = new AudioNode(
            getApp().getAssetManager(), file, false);
        a.setLooping(true);
        a.setPositional(false);
        return a;
    }
    
    private float getRainMediumVolume(float intensity) {
        if(0.0f <= intensity && intensity < 0.2f) {
            return (float) MathExt.interpolateLinear(0f, 1f, (intensity-0.0f) / (0.2f-0.0f));
        }
        if(0.2f <= intensity && intensity < 0.7f) {
            return 1f;
        }
        if(0.7f <= intensity && intensity < 0.8f) {
            return (float) MathExt.interpolateLinear(1f, 0f, (intensity-0.7f) / (0.8f-0.7f));
        }
        if(0.8f <= intensity && intensity <= 1.0f) {
            return 0f;
        }
        throw new IllegalArgumentException("intensity not in range [0,1]");
    }
    
    private float getRainHeavyVolume(float intensity) {
        if(0.0f <= intensity && intensity < 0.7f) {
            return 0f;
        }
        if(0.7f <= intensity && intensity < 0.8f) {
            return (float) MathExt.interpolateLinear(0f, 1f, (intensity-0.7f) / (0.8f-0.7f));
        }
        if(0.8f <= intensity && intensity <= 1.0f) {
            return 1f;
        }
        throw new IllegalArgumentException("intensity not in range [0,1]");
    }
}
