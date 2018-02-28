package com.dci.intellij.dbn.editor.data.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterType;
import com.dci.intellij.dbn.editor.data.options.DataEditorFilterSettings;
import com.intellij.openapi.options.ConfigurationException;

public class DataEditorFilterSettingsForm extends ConfigurationEditorForm<DataEditorFilterSettings> {
    private JPanel mainPanel;
    private JCheckBox promptFilterDialogCheckBox;
    private DBNComboBox<DatasetFilterType> defaultFilterTypeComboBox;

    public DataEditorFilterSettingsForm(DataEditorFilterSettings settings) {
        super(settings);
        defaultFilterTypeComboBox.setValues(
                DatasetFilterType.NONE,
                DatasetFilterType.BASIC,
                DatasetFilterType.CUSTOM);

        updateBorderTitleForeground(mainPanel);
        resetFormChanges();
        defaultFilterTypeComboBox.setEnabled(promptFilterDialogCheckBox.isSelected());
        registerComponent(mainPanel);
    }

    @Override
    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getConfiguration().setModified(true);
                defaultFilterTypeComboBox.setEnabled(promptFilterDialogCheckBox.isSelected());
            }
        };
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        DataEditorFilterSettings settings = getConfiguration();
        settings.setPromptFilterDialog(promptFilterDialogCheckBox.isSelected());
        settings.setDefaultFilterType(defaultFilterTypeComboBox.getSelectedValue());
    }


    public void resetFormChanges() {
        DataEditorFilterSettings settings = getConfiguration();
        promptFilterDialogCheckBox.setSelected(settings.isPromptFilterDialog());
        defaultFilterTypeComboBox.setSelectedValue(settings.getDefaultFilterType());
    }
}
