package de.mycrobase.ssim.ed.app.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.KeyInputHandler;
import de.lessvoid.nifty.screen.Screen;

public class OptionsScreenAppState extends BasicScreenAppState implements KeyInputHandler {
    
    private static final Logger logger = Logger.getLogger(OptionsScreenAppState.class);
    
    // exists only while AppState is attached
    private HashMap<String,String> changedSettings;
    
    // exists only while Controller is bound
    private DropDown<InternalDataListModel> localeUIDropDown;
    private DropDown<InternalDataListModel> localeInputDropDown;
    
    // needed by Nifty
    public OptionsScreenAppState() {
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        changedSettings = new HashMap<String, String>();
    }
    
    @Override
    public void cleanup() {
        super.cleanup();
        
        changedSettings = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void bind(Nifty nifty, Screen screen) {
        super.bind(nifty, screen);
        
        localeUIDropDown = getScreen().findNiftyControl("opt_locale_ui_dropdown", DropDown.class);
        localeUIDropDown.addAllItems(loadLocaleUIList());
        
        localeInputDropDown = getScreen().findNiftyControl("opt_locale_input_dropdown", DropDown.class);
        localeInputDropDown.addAllItems(loadLocaleInputList());
    }
    
    @Override
    public void onStartScreen() {
        super.onStartScreen();
        
        // select current active after setting
        String localeUISetting = getApp().getSettingsManager().getString("locale.ui");
        for(InternalDataListModel m : localeUIDropDown.getItems()) {
            if(m.getInternalData().equals(localeUISetting)) {
                localeUIDropDown.selectItem(m);
                break;
            }
        }
        
        // select current active after setting
        String localeInputSetting = getApp().getSettingsManager().getString("locale.input");
        for(InternalDataListModel m : localeInputDropDown.getItems()) {
            if(m.getInternalData().equals(localeInputSetting)) {
                localeInputDropDown.selectItem(m);
                break;
            }
        }
    }
    
    @Override
    public boolean keyEvent(NiftyInputEvent inputEvent) {
        if(inputEvent == NiftyInputEvent.Escape) {
            doAbort();
            return true;
        }
        return false;
    }
    
    @NiftyEventSubscriber(id="opt_locale_ui_dropdown")
    public void onLocaleUIDropDownSelectionChanged(String id, DropDownSelectionChangedEvent<String> event) {
        String localeSetting = localeUIDropDown.getSelection().getInternalData();
        changedSettings.put("locale.ui", localeSetting);
    }
    
    @NiftyEventSubscriber(id="opt_locale_input_dropdown")
    public void onLocaleInputDropDownSelectionChanged(String id, DropDownSelectionChangedEvent<String> event) {
        String localeSetting = localeInputDropDown.getSelection().getInternalData();
        changedSettings.put("locale.input", localeSetting);
    }
    
    @Override
    public String translate(String key) {
        return super.translate("options." + key);
    }
    
    // interact
    
    public void doApply() {
        logger.debug("doApply");
        
        for(Map.Entry<String, String> entry : changedSettings.entrySet()) {
            getApp().getSettingsManager().setString(entry.getKey(), entry.getValue());
        }
        getApp().getSettingsManager().flush();
        changedSettings.clear();
        
        getNifty().gotoScreen("main");
    }
    
    public void doAbort() {
        logger.debug("doAbort");
        
        changedSettings.clear();
        
        getNifty().gotoScreen("main");
    }
    
    // helper

    private List<InternalDataListModel> loadLocaleUIList() {
        ArrayList<InternalDataListModel> list = new ArrayList<InternalDataListModel>();
        list.add(new InternalDataListModel("auto", "auto"));
        list.add(new InternalDataListModel("English", "en_US"));
        list.add(new InternalDataListModel("Deutsch", "de_DE"));
        return list;
    }

    private List<InternalDataListModel> loadLocaleInputList() {
        ArrayList<InternalDataListModel> list = new ArrayList<InternalDataListModel>();
        list.add(new InternalDataListModel("auto", "auto"));
        list.add(new InternalDataListModel("US", "us"));
        list.add(new InternalDataListModel("Deutsch", "de"));
        return list;
    }
    
    private static class InternalDataListModel {
        
        private String label;
        private String internalData;
        
        public InternalDataListModel(String label, String internalData) {
            super();
            this.label = label;
            this.internalData = internalData;
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getInternalData() {
            return internalData;
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
}
