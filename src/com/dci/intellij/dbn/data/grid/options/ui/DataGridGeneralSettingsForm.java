package com.dci.intellij.dbn.data.grid.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.data.grid.options.DataGridGeneralSettings;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class DataGridGeneralSettingsForm extends ConfigurationEditorForm<DataGridGeneralSettings> {
    private JPanel mainPanel;
    private JCheckBox enableZoomingCheckBox;

    public DataGridGeneralSettingsForm(DataGridGeneralSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        DataGridGeneralSettings settings = getConfiguration();
        settings.setZoomingEnabled(enableZoomingCheckBox.isSelected());
    }

    public void resetFormChanges() {
        DataGridGeneralSettings settings = getConfiguration();
        enableZoomingCheckBox.setSelected(settings.isZoomingEnabled());
    }
}
