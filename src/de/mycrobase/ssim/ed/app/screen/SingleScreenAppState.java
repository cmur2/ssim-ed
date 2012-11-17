package de.mycrobase.ssim.ed.app.screen;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;

import de.altimos.util.translator.Translator;
import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.Label;
import de.lessvoid.nifty.controls.ListBox;
import de.lessvoid.nifty.controls.ListBoxSelectionChangedEvent;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.KeyInputHandler;
import de.lessvoid.nifty.screen.Screen;
import de.mycrobase.ssim.ed.mission.Mission;
import de.mycrobase.ssim.ed.mission.MissionParser;

public class SingleScreenAppState extends BasicScreenAppState implements KeyInputHandler {
    
    private static final Logger logger = Logger.getLogger(SingleScreenAppState.class);
    private static final Translator translator = Translator.getGlobal();
    
    // exists only while AppState is attached
    
    // exists only while Controller is bound
    private ListBox<MissionListModel> missionList;
    private Label missionText;
    
    // needed by Nifty
    public SingleScreenAppState() {
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        // ...
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        // ...
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void bind(Nifty nifty, Screen screen) {
        super.bind(nifty, screen);
        
        missionList = getScreen().findNiftyControl("mission_list", ListBox.class);
        Mission[] missions = loadMissions();
        missionList.addAllItems(asModelList(missions));
        
        missionText = getScreen().findNiftyControl("mission_text", Label.class);
    }
    
    @Override
    public void onStartScreen() {
        super.onStartScreen();
        
        // try to select first item by default, fails silently if there are no items
        // TODO: select last played? or successor of last won?
        missionList.selectItemByIndex(0);
    }
    
    @Override
    public boolean keyEvent(NiftyInputEvent inputEvent) {
        if(inputEvent == NiftyInputEvent.Escape) {
            doReturn();
            return true;
        }
        return false;
    }
    
    @NiftyEventSubscriber(id="mission_list")
    public void onMissionListSelectionChanged(String id, ListBoxSelectionChangedEvent<String> event) {
        updateMissionText();
    }

    @Override
    public String translate(String key) {
        return super.translate("single." + key);
    }
    
    // interact
    
    public void doGame() {
        logger.debug("doGame");
        List<MissionListModel> selection = missionList.getSelection();
        if(selection.size() > 0) {
            // pass info about selected Mission to App
            Mission m = selection.get(0).getMission();
            // TODO: need loading screen
            getApp().doGameInit(m);
        }
    }
    
    public void doReturn() {
        logger.debug("doReturn");
        getNifty().gotoScreen("main");
    }
    
    // helper
    
    private void updateMissionText() {
        List<MissionListModel> selection = missionList.getSelection();
        if(selection.size() > 0) {
            Mission m = selection.get(0).getMission();
            String text = translator.translate(m.getDescription());
            missionText.setText(formatText(text));
            // relayout for wrapping
            missionText.getElement().getParent().layoutElements();
        }
    }
    
    private Mission[] loadMissions() {
        return new Mission[] {
            MissionParser.load(getApp().getAssetManager(), "missions/mission_01.xml"),
            MissionParser.load(getApp().getAssetManager(), "missions/mission_02.xml")
        };
    }
    
    private List<MissionListModel> asModelList(Mission[] missions) {
        ArrayList<MissionListModel> list = new ArrayList<SingleScreenAppState.MissionListModel>(missions.length);
        for(Mission m : missions) {
            list.add(new MissionListModel(m));
        }
        return list;
    }

    private static String formatText(String text) {
        StringBuffer s = new StringBuffer();
        String[] lines = text.split("\\n");
        for(int i = 0; i < lines.length; i++) {
            int len = lines[i].length();
            // preserve double newlines
            if(len == 0) {
                s.append('\n');
                continue;
            }
            s.append(lines[i]);
        }
        return s.toString();
    }
    
    private static class MissionListModel {
        
        private Mission mission;
        
        public MissionListModel(Mission mission) {
            this.mission = mission;
        }
        
        public Mission getMission() {
            return mission;
        }
        
        @Override
        public String toString() {
            return String.format(" %s", translator.translate(mission.getTitle()));
        }
    }
}
