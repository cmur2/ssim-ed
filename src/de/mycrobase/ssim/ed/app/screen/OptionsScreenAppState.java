package de.mycrobase.ssim.ed.app.screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.NiftyEventSubscriber;
import de.lessvoid.nifty.controls.CheckBox;
import de.lessvoid.nifty.controls.CheckBoxStateChangedEvent;
import de.lessvoid.nifty.controls.DropDown;
import de.lessvoid.nifty.controls.DropDownSelectionChangedEvent;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.input.NiftyInputEvent;
import de.lessvoid.nifty.screen.KeyInputHandler;
import de.lessvoid.nifty.screen.Screen;

public class OptionsScreenAppState extends BasicScreenAppState implements KeyInputHandler {
    
    private static final Logger logger = Logger.getLogger(OptionsScreenAppState.class);
    private static final String UserSettingFormat = "<%s>";
    
    // exists only while AppState is attached
    private HashMap<String,Object> changedSettings;
    
    // exists only while Controller is bound
    private DropDown<InternalDataListModel> localeUIDropDown;
    private DropDown<InternalDataListModel> localeInputDropDown;
    private DropDown<InternalDataListModel> resolutionDropDown;
    private DropDown<InternalDataListModel> multisampleDropDown;
    private CheckBox fullscreenCheckBox;
    private CheckBox vsyncCheckBox;
    private Element applyPopup;
    
    // needed by Nifty
    public OptionsScreenAppState() {
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application baseApp) {
        super.initialize(stateManager, baseApp);
        
        changedSettings = new HashMap<String,Object>();
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
        
        resolutionDropDown = getScreen().findNiftyControl("opt_resolution_dropdown", DropDown.class);
        resolutionDropDown.addAllItems(loadResolutionList());
        
        multisampleDropDown = getScreen().findNiftyControl("opt_multisample_dropdown", DropDown.class);
        multisampleDropDown.addAllItems(loadMultisampleList());
        
        fullscreenCheckBox = getScreen().findNiftyControl("opt_fullscreen_checkbox", CheckBox.class);
        
        vsyncCheckBox = getScreen().findNiftyControl("opt_vsync_checkbox", CheckBox.class);
        
        applyPopup = getNifty().createPopup("popupApply");
    }
    
    @Override
    public void onStartScreen() {
        super.onStartScreen();
        
        selectItemBySetting(localeUIDropDown, "locale.ui");
        selectItemBySetting(localeInputDropDown, "locale.input");
        
        selectItemBySetting(resolutionDropDown, "display.resolution");
        fullscreenCheckBox.setChecked(getApp().getSettingsManager().getBoolean("display.fullscreen"));
        vsyncCheckBox.setChecked(getApp().getSettingsManager().getBoolean("display.vsync"));
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

    @NiftyEventSubscriber(id="opt_resolution_dropdown")
    public void onResolutionDropDownSelectionChanged(String id, DropDownSelectionChangedEvent<String> event) {
        String resolutionSetting = resolutionDropDown.getSelection().getInternalData();
        changedSettings.put("display.resolution", resolutionSetting);
    }

    @NiftyEventSubscriber(id="opt_multisample_dropdown")
    public void onMultisampleDropDownSelectionChanged(String id, DropDownSelectionChangedEvent<String> event) {
        String multisampleSetting = multisampleDropDown.getSelection().getInternalData();
        changedSettings.put("display.multisample", multisampleSetting);
    }
    
    @NiftyEventSubscriber(id="opt_fullscreen_checkbox")
    public void onFullscreenCheckBoxStateChanged(String id, CheckBoxStateChangedEvent event) {
        changedSettings.put("display.fullscreen", fullscreenCheckBox.isChecked());
    }
    
    @NiftyEventSubscriber(id="opt_vsync_checkbox")
    public void onVSyncCheckBoxStateChanged(String id, CheckBoxStateChangedEvent event) {
        changedSettings.put("display.vsync", vsyncCheckBox.isChecked());
    }
    
    @Override
    public String translate(String key) {
        return super.translate("options." + key);
    }
    
    // interact
    
    public void doApply() {
        logger.debug("doApply");
        
        getNifty().showPopup(getNifty().getCurrentScreen(), applyPopup.getId(), null);
    }
    
    public void doAbort() {
        logger.debug("doAbort");
        
        changedSettings.clear();
        
        getNifty().gotoScreen("main");
    }
    
    public void doPopupOk() {
        logger.debug("doPopupOk");
        
        saveChangedSettingsAndFlush();
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
    
    private List<InternalDataListModel> loadResolutionList() {
        ArrayList<InternalDataListModel> list = new ArrayList<InternalDataListModel>(); 
        
        // fall back
        DisplayMode[] modes = new DisplayMode[] { new DisplayMode(640, 480) };
        try {
            modes = Display.getAvailableDisplayModes();
        } catch(LWJGLException ex) {
            logger.error("Exception while getting list of available display modes...", ex);
        }
        
        // pre sort the modes array ascending by width and height to keep
        // our list sorted too
        Arrays.sort(modes, new Comparator<DisplayMode>() {
            @Override
            public int compare(DisplayMode o1, DisplayMode o2) {
                int w1 = o1.getWidth(), w2 = o2.getWidth();
                int h1 = o1.getHeight(), h2 = o2.getHeight();
                // width is weighted higher than height to group same width
                // values together with ascending height
                int i1 = (w1 < w2 ? -10 : (w1 == w2 ? 0 : 10));
                int i2 = (h1 < h2 ? -1 : (h1 == h2 ? 0 : 1));
                return i1 + i2;
            }
        });
        
        for(DisplayMode mode : modes) {
            String res = mode.getWidth() + "x" +  mode.getHeight();
            InternalDataListModel m = new InternalDataListModel(res, res);
            
            if(!list.contains(m)) {
                list.add(m);
            }
        }
        
        return list;
    }
    
    private List<InternalDataListModel> loadMultisampleList() {
        ArrayList<InternalDataListModel> list = new ArrayList<InternalDataListModel>();
        list.add(new InternalDataListModel("-", "0"));
        list.add(new InternalDataListModel("1x", "1"));
        list.add(new InternalDataListModel("2x", "2"));
        list.add(new InternalDataListModel("4x", "4"));
        list.add(new InternalDataListModel("8x", "8"));
        return list;
    }
    
    private void selectItemBySetting(DropDown<InternalDataListModel> dropDown, String key) {
        String value = getApp().getSettingsManager().getString(key);
        
        // create item
        InternalDataListModel item = new InternalDataListModel(
            String.format(UserSettingFormat, value), value);
        
        // check using contains() since InternalDataListModel implements equals()
        if(!dropDown.getItems().contains(item)) {
            // if settings contains a value not yet in the list (may occur with
            // user specified values e.g. for screen resolution), add it and mark it
            // as one
            logger.warn(String.format(
                "Adding yet unknown user defined value %s on property %s",
                value, key));
            dropDown.addItem(item);
        }
        dropDown.selectItem(item);
    }
    
    private void saveChangedSettingsAndFlush() {
        for(Map.Entry<String,Object> entry : changedSettings.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // on continue the current property is not saved because it does not
            // differ from saved value
            if(value instanceof String) {
                String s = (String) value;
                if(s.equals(getApp().getSettingsManager().getString(key))) {
                    logger.debug(String.format(
                        "Skipping unchanged value %s on property %s", s, key));
                    continue;
                }
                getApp().getSettingsManager().setString(key, s);
            } else if(value instanceof Boolean) {
                Boolean b = (Boolean) value;
                if(b.equals(getApp().getSettingsManager().getBoolean(key))) {
                    logger.debug(String.format(
                        "Skipping unchanged value %s on property %s", b, key));
                    continue;
                }
                getApp().getSettingsManager().setBoolean(key, b);
            }
        }
        getApp().getSettingsManager().flush();
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
        public int hashCode() {
            return internalData.hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            
            if(!(obj instanceof InternalDataListModel)) return false;
            
            InternalDataListModel model = (InternalDataListModel) obj;
            
            return internalData.equals(model.internalData);
        }
        
        @Override
        public String toString() {
            return String.format(" %s", label);
        }
    }
}
