package com.dci.intellij.dbn.editor.data.options;

import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.data.record.navigation.RecordNavigationTarget;
import com.dci.intellij.dbn.editor.data.options.ui.DataEditorRecordNavigationSettingsForm;
import org.jdom.Element;

public class DataEditorRecordNavigationSettings extends Configuration<DataEditorRecordNavigationSettingsForm> {
    private RecordNavigationTarget navigationTarget = RecordNavigationTarget.VIEWER;

    @Override
    public DataEditorRecordNavigationSettingsForm createConfigurationEditor() {
        return new DataEditorRecordNavigationSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "record-navigation";
    }

    public RecordNavigationTarget getNavigationTarget() {
        return navigationTarget;
    }

    public void setNavigationTarget(RecordNavigationTarget navigationTarget) {
        this.navigationTarget = navigationTarget;
    }

    public void readConfiguration(Element element) {
        navigationTarget = SettingsUtil.getEnum(element, "navigation-target", RecordNavigationTarget.VIEWER);
    }

    public void writeConfiguration(Element element) {
        SettingsUtil.setEnum(element, "navigation-target", navigationTarget);
    }
}
