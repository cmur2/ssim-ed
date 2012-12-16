package de.mycrobase.ssim.ed;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.imageio.ImageIO;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import chlib.noise.NoiseUtil;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppState;
import com.jme3.system.AppSettings;

import de.altimos.util.logger.JLFBridge;
import de.altimos.util.translator.Translator;
import de.mycrobase.ssim.ed.app.AerialAppState;
import de.mycrobase.ssim.ed.app.AudioAppState;
import de.mycrobase.ssim.ed.app.CameraAppState;
import de.mycrobase.ssim.ed.app.CloudAppState;
import de.mycrobase.ssim.ed.app.DebugAppState;
import de.mycrobase.ssim.ed.app.GuiAppState;
import de.mycrobase.ssim.ed.app.InputMappingAppState;
import de.mycrobase.ssim.ed.app.LightingAppState;
import de.mycrobase.ssim.ed.app.NiftyAppState;
import de.mycrobase.ssim.ed.app.OceanAppState;
import de.mycrobase.ssim.ed.app.RainAppState;
import de.mycrobase.ssim.ed.app.ShadowAppState;
import de.mycrobase.ssim.ed.app.SimClockAppState;
import de.mycrobase.ssim.ed.app.SkyAppState;
import de.mycrobase.ssim.ed.app.SkyDomeAppState;
import de.mycrobase.ssim.ed.app.StarAppState;
import de.mycrobase.ssim.ed.app.SunAppState;
import de.mycrobase.ssim.ed.app.TerrainAppState;
import de.mycrobase.ssim.ed.app.WeatherAppState;
import de.mycrobase.ssim.ed.app.screen.CreditsScreenAppState;
import de.mycrobase.ssim.ed.app.screen.GameScreenAppState;
import de.mycrobase.ssim.ed.app.screen.IntroScreenAppState;
import de.mycrobase.ssim.ed.app.screen.MainScreenAppState;
import de.mycrobase.ssim.ed.app.screen.OptionsScreenAppState;
import de.mycrobase.ssim.ed.app.screen.PauseScreenAppState;
import de.mycrobase.ssim.ed.app.screen.SingleScreenAppState;
import de.mycrobase.ssim.ed.mission.Mission;
import de.mycrobase.ssim.ed.settings.FileLayer;
import de.mycrobase.ssim.ed.settings.LayeredSettingsManager;
import de.mycrobase.ssim.ed.settings.PropertiesLayer;
import de.mycrobase.ssim.ed.settings.SettingsManager;
import de.mycrobase.ssim.ed.util.MapLoader;
import de.mycrobase.ssim.ed.util.PropertiesLoader;
import de.mycrobase.ssim.ed.util.XMLLoader;
import de.mycrobase.ssim.ed.util.lang.TListener;
import de.mycrobase.ssim.ed.util.lang.XmlTranslation;

public class Main extends SimpleApplication implements GameModeListener, SSimApplication {
    
    private static final Logger logger = Logger.getLogger(Main.class);
    private static final float UpdateInterval = 5f; // in seconds
    
    private static final float MaxVisibility = 20000f;
    private static final long DebugSeed = 4569845;
    
    public static void main(String[] args) {
        //org.apache.log4j.BasicConfigurator.configure();
        {
            Logger root = Logger.getRootLogger();
            root.addAppender(new ConsoleAppender(new PatternLayout("%-3r [%t] %-5p %c: %m%n")));
        }
        JLFBridge.installBridge();
        // prevent NPE in VertexBuffer.toString() during NativeObjectManager.deleteUnused()
        java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.FINER);
        // disable scene graph Node attach/detach logging for performance
        // (an issue mostly for terrain)
        java.util.logging.Logger.getLogger("com.jme3.scene.Node").setLevel(java.util.logging.Level.OFF);
        
        CLI cli = new CLI(args);
        if(!cli.parse()) {
            return;
        }
        
        // initialize settings manager
        LayeredSettingsManager settingsManager = new LayeredSettingsManager();
        // add layer
        logger.info("Loading default properties");
        settingsManager.addLayer(PropertiesLayer.fromStream(Main.class.getResourceAsStream("default.properties")));
        settingsManager.addLayer(new FileLayer(new File("sed.properties"), false));
        settingsManager.addLayer(new FileLayer(new File(System.getProperty("user.home")+"/sed.properties"), true));
        settingsManager.addLayer(new PropertiesLayer(cli.getCmdLineProperties()));
        
        // derive AppSettings for jME and LWJGL (mostly regarding display
        // options) from our SettingsManager
        AppSettings as = new AppSettings(true);
        as.setTitle("SSim Environment Demo");
        as.setVSync(settingsManager.getBoolean("display.vsync"));
        {
            String[] wh = settingsManager.getString("display.resolution").split("x");
            as.setResolution(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]));
        }
        as.setFullscreen(settingsManager.getBoolean("display.fullscreen"));
        as.setSamples(settingsManager.getInteger("display.multisample"));
        
        try {
            // disable caching for faster startup
            ImageIO.setUseCache(false);
            
            as.setIcons(new BufferedImage[] {
                ImageIO.read(Main.class.getResourceAsStream("icon_32_tx.png"))
            });
            // TODO: more icons in different resolutions, see LWJGL recommendations
        } catch(IOException ex) {
            logger.error("Exception while loading icons follows...", ex);
        }
        
        // initialize Main
        Main main = new Main(settingsManager);
        main.setSettings(as);
        main.setShowSettings(false);
        main.start();
    }
    
    private float time = 0;
    
    private SettingsManager settingsManager;
    private ScheduledExecutorService executor;

    private GameMode currentMode = GameMode.Stopped;
    private List<GameModeListener> gameModeListeners = new ArrayList<GameModeListener>();
    
    private List<AppState> gameAppStates = new LinkedList<AppState>();
    
    public Main(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }
    
    @Override
    public void simpleInitApp() {
        setDisplayStatView(settingsManager.getBoolean("debug.stats"));
        
        if(settingsManager.getBoolean("debug.noise.seed")) {
            NoiseUtil.reinitialize(DebugSeed);
        } else {
            NoiseUtil.reinitialize();
        }
        
        assetManager.registerLoader(XMLLoader.class, "xml");
        assetManager.registerLoader(MapLoader.class, "map");
        assetManager.registerLoader(PropertiesLoader.class, "properties");
        
        int numWorker = (int) (1.5f * Runtime.getRuntime().availableProcessors());
        logger.info(String.format("Worker thread pool size: %d", numWorker));
        executor = Executors.newScheduledThreadPool(numWorker);

        logger.info("System default locale: " + Locale.getDefault());
        {
            String localeSetting = settingsManager.getString("locale.ui");
            if(!localeSetting.equals("auto")) {
                String[] lc = localeSetting.split("_");
                Locale.setDefault(new Locale(lc[0].toLowerCase(), lc[1].toUpperCase()));
                logger.info("System new locale: " + Locale.getDefault());
            } else {
                logger.info("locale.ui setting is 'auto'");
            }
        }
        {
            Translator translator = Translator.getGlobal();            
            // make sure the default locale will be used:
            translator.setLocale(Locale.getDefault())
                      .setTranslation(new XmlTranslation(assetManager))
                      .setSource(null)
                      .setDomain("lang/ssim")
                      .setListener(new TListener());
        }
        
        speed = 1f;

        // manual call to avoid code duplication
        gameModeChanged(null, getCurrentMode());
        
        stateManager.attach(new IntroScreenAppState());
        stateManager.attach(new MainScreenAppState());
        stateManager.attach(new CreditsScreenAppState());
        stateManager.attach(new OptionsScreenAppState());
        stateManager.attach(new SingleScreenAppState());
        stateManager.attach(new GameScreenAppState());
        stateManager.attach(new PauseScreenAppState());
        
        stateManager.attach(new NiftyAppState());
        stateManager.attach(new InputMappingAppState());
        
        addGameModeListener(this);
    }
    
    @Override
    public void simpleUpdate(float dt) {
        if(time > UpdateInterval) {
            time -= UpdateInterval;
            // ...
            //Util.printSceneGraph(rootNode);
        }
        
        time += dt;
    }
    
    @Override
    public void destroy() {
        super.destroy();
        
        // shutdown all (non daemon) worker pool threads
        executor.shutdown();
    }

    @Override
    public void gameModeChanged(GameMode oldMode, GameMode newMode) {
        if(newMode == GameMode.Running) {
            flyCam.setEnabled(true);
            flyCam.setDragToRotate(false);
            inputManager.setCursorVisible(false);
        } else {
            // disable for nifty
            flyCam.setEnabled(false);
            flyCam.setDragToRotate(true);
            inputManager.setCursorVisible(true);
        }
    }
    
    // public API
    
    public ScheduledExecutorService getExecutor() {
        return executor;
    }
    
    public float getSpeed() {
        return speed;
    }
    
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    
    public SettingsManager getSettingsManager() {
        return settingsManager;
    }
    
    public GameMode getCurrentMode() {
        return currentMode;
    }
    
    public void addGameModeListener(GameModeListener lis) {
        if(!gameModeListeners.contains(lis)) {
            gameModeListeners.add(lis);
        }
    }
    
    public void removeGameModeListener(GameModeListener lis) {
        if(gameModeListeners.contains(lis)) {
            gameModeListeners.remove(lis);
        }
    }

    /**
     * Triggers switch if old and new {@link GameMode}s are different and
     * notifies all {@link GameModeListener}s about change.
     * 
     * @param newMode
     */
    public void switchGameMode(GameMode newMode) {
        GameMode oldMode = currentMode;
        
        if(oldMode != newMode) {
            for(GameModeListener lis : gameModeListeners) {
                lis.gameModeChanged(oldMode, newMode);
            }
        }
        
        currentMode = newMode;
    }

    public void doGameInit(Mission mission) {
        // AppState base layer:
        // these serve as a common base for the higher AppStates
        gameAppStates.add(new SimClockAppState(mission));
        gameAppStates.add(new CameraAppState(MaxVisibility));
        gameAppStates.add(new WeatherAppState("clear"));
        gameAppStates.add(new SkyAppState(10000f, mission));
        gameAppStates.add(new AerialAppState());
        
        // AppState higher layer:
        // these have no dependencies to each other, just to the base layer
        gameAppStates.add(new SkyDomeAppState());
        gameAppStates.add(new SunAppState());
        gameAppStates.add(new LightingAppState());
        gameAppStates.add(new ShadowAppState());
        gameAppStates.add(new StarAppState());
        gameAppStates.add(new CloudAppState());
        gameAppStates.add(new TerrainAppState(mission));
        gameAppStates.add(new OceanAppState(MaxVisibility));
        gameAppStates.add(new RainAppState());
        gameAppStates.add(new GuiAppState());
        gameAppStates.add(new AudioAppState());
        gameAppStates.add(new DebugAppState());
        
        for(AppState state : gameAppStates) {
            stateManager.attach(state);
        }
        
        switchGameMode(GameMode.Running);
    }
    
    public void doGamePause() {
        for(AppState state : gameAppStates) {
            state.setEnabled(false);
        }
        
        switchGameMode(GameMode.Paused);
    }
    
    public void doGameResume() {
        for(AppState state : gameAppStates) {
            state.setEnabled(true);
        }
        
        switchGameMode(GameMode.Running);
    }
    
    public void doGameExit() {
        for(AppState state : gameAppStates) {
            stateManager.detach(state);
        }
        gameAppStates.clear();
        
        switchGameMode(GameMode.Stopped);
    }
}
