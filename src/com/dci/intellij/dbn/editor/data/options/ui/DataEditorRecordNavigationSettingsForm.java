package com.dci.intellij.dbn.editor.data.options.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.data.record.navigation.RecordNavigationTarget;
import com.dci.intellij.dbn.editor.data.options.DataEditorRecordNavigationSettings;
import com.intellij.openapi.options.ConfigurationException;

public class DataEditorRecordNavigationSettingsForm extends ConfigurationEditorForm<DataEditorRecordNavigationSettings> {
    private JPanel mainPanel;
    private DBNComboBox<RecordNavigationTarget> navigationTargetComboBox;


    public DataEditorRecordNavigationSettingsForm(DataEditorRecordNavigationSettings configuration) {
        super(configuration);
        updateBorderTitleForeground(mainPanel);
        navigationTargetComboBox.setValues(
                RecordNavigationTarget.EDITOR,
                RecordNavigationTarget.VIEWER,
                RecordNavigationTarget.PROMPT);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        DataEditorRecordNavigationSettings configuration = getConfiguration();

        RecordNavigationTarget navigationTarget = navigationTargetComboBox.getSelectedValue();
        configuration.setNavigationTarget(navigationTarget);
    }

    public void resetFormChanges() {
        DataEditorRecordNavigationSettings configuration = getConfiguration();
        RecordNavigationTarget navigationTarget = configuration.getNavigationTarget();
        navigationTargetComboBox.setSelectedValue(navigationTarget);
    }
}
