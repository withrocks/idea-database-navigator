package com.dci.intellij.dbn.editor.data.options;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.editor.data.options.ui.DataEditorPopupSettingsForm;
import org.jdom.Element;

public class DataEditorPopupSettings extends Configuration<DataEditorPopupSettingsForm>{
    private boolean active = false;
    private boolean activeIfEmpty = false;
    private int dataLengthThreshold = 100;
    private int delay = 1000;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActiveIfEmpty() {
        return activeIfEmpty;
    }

    public void setActiveIfEmpty(boolean activeIfEmpty) {
        this.activeIfEmpty = activeIfEmpty;
    }

    public int getDataLengthThreshold() {
        return dataLengthThreshold;
    }

    public void setDataLengthThreshold(int dataLengthThreshold) {
        this.dataLengthThreshold = dataLengthThreshold;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public String getDisplayName() {
        return null;
    }

    /****************************************************
     *                   Configuration                  *
     ****************************************************/
   public DataEditorPopupSettingsForm createConfigurationEditor() {
       return new DataEditorPopupSettingsForm(this);
   }

    @Override
    public String getConfigElementName() {
        return "text-editor-popup";
    }

    public void readConfiguration(Element element) {
        active = SettingsUtil.getBoolean(element, "active", active);
        activeIfEmpty = SettingsUtil.getBoolean(element, "active-if-empty", activeIfEmpty);
        dataLengthThreshold = SettingsUtil.getInteger(element, "data-length-threshold", dataLengthThreshold);
        delay = SettingsUtil.getInteger(element, "popup-delay", delay);
    }

    public void writeConfiguration(Element element) {
        SettingsUtil.setBoolean(element, "active", active);
        SettingsUtil.setBoolean(element, "active-if-empty", activeIfEmpty);
        SettingsUtil.setInteger(element, "data-length-threshold", dataLengthThreshold);
        SettingsUtil.setInteger(element, "popup-delay", delay);
    }

}
