package com.dci.intellij.dbn.common.options.setting;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import org.jdom.Element;

import javax.swing.JToggleButton;

public class BooleanSetting extends Setting<Boolean, JToggleButton> implements PersistentConfiguration {
    public BooleanSetting(String name, Boolean value) {
        super(name, value);
    }
    
    @Override
    public void readConfiguration(Element parent) {
        setValue(SettingsUtil.getBoolean(parent, getName(), this.value()));
    }

    public void readConfigurationAttribute(Element parent) {
        setValue(SettingsUtil.getBooleanAttribute(parent, getName(), this.value()));
    }

    @Override
    public void writeConfiguration(Element parent) {
        SettingsUtil.setBoolean(parent, getName(), this.value());
    }

    public void writeConfigurationAttribute(Element parent) {
        SettingsUtil.setBooleanAttribute(parent, getName(), this.value());
    }


    public boolean applyChanges(JToggleButton checkBox) {
        return setValue(checkBox.isSelected());
    }
    
    public void resetChanges(JToggleButton checkBox) {
        checkBox.setSelected(value());
    }
}
