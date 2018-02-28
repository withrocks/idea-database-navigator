package com.dci.intellij.dbn.object.filter.type;

import javax.swing.*;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.list.Selectable;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.type.ui.ObjectTypeFilterSettingsForm;

public class ObjectTypeFilterSetting implements Selectable<ObjectTypeFilterSetting> {
    private DBObjectType objectType;
    private boolean selected = true;
    private ObjectTypeFilterSettings parent;

    ObjectTypeFilterSetting(ObjectTypeFilterSettings parent, DBObjectType objectType) {
        this.parent = parent;
        this.objectType = objectType;
    }

    ObjectTypeFilterSetting(ObjectTypeFilterSettings parent, DBObjectType objectType, boolean selected) {
        this.parent = parent;
        this.objectType = objectType;
        this.selected = selected;
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    public Icon getIcon() {
        return objectType.getIcon();
    }

    public String getName() {
        return objectType.getName().toUpperCase();
    }

    public String getError() {
        ObjectTypeFilterSettingsForm settingsEditor = parent.getSettingsEditor();

        boolean masterSettingSelected = isMasterSelected();

        boolean settingSelected =
                (settingsEditor == null && parent.isSelected(this)) ||
                (settingsEditor != null && settingsEditor.isSelected(this));
        if (settingSelected && !masterSettingSelected) {
            return "Disabled on project level";
        }
        return null;
    }

    public boolean isMasterSelected() {
        ObjectTypeFilterSettings masterSettings = parent.getMasterSettings();
        if (masterSettings != null) {
            ObjectTypeFilterSettingsForm masterSettingsEditor = masterSettings.getSettingsEditor();
            return masterSettingsEditor == null ?
                    masterSettings.isSelected(this) :
                    masterSettingsEditor.isSelected(this);

        }
        return true;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectTypeFilterSetting that = (ObjectTypeFilterSetting) o;
        return objectType == that.objectType;
    }

    @Override
    public int hashCode() {
        return objectType != null ? objectType.hashCode() : 0;
    }

    @Override
    public int compareTo(@NotNull ObjectTypeFilterSetting o) {
        return 0;
    }
}
