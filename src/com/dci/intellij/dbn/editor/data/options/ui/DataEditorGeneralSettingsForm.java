package com.dci.intellij.dbn.editor.data.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.editor.data.options.DataEditorGeneralSettings;
import com.intellij.openapi.options.ConfigurationException;

public class DataEditorGeneralSettingsForm extends ConfigurationEditorForm<DataEditorGeneralSettings> {
    private JPanel mainPanel;
    private JTextField fetchBlockSizeTextField;
    private JTextField fetchTimeoutTextField;
    private JCheckBox trimWhitespacesCheckBox;
    private JCheckBox convertEmptyToNullCheckBox;
    private JCheckBox selectContentOnEditCheckBox;
    private JCheckBox largeValuePreviewActiveCheckBox;

    public DataEditorGeneralSettingsForm(DataEditorGeneralSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        resetFormChanges();

        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        ConfigurationEditorUtil.validateIntegerInputValue(fetchBlockSizeTextField, "Fetch block size", 1, 10000, null);
        ConfigurationEditorUtil.validateIntegerInputValue(fetchTimeoutTextField, "Fetch timeout", 0, 300, "\nUse value 0 for no timeout");

        DataEditorGeneralSettings settings = getConfiguration();
        settings.getFetchBlockSize().applyChanges(fetchBlockSizeTextField);
        settings.getFetchTimeout().applyChanges(fetchTimeoutTextField);
        settings.getTrimWhitespaces().applyChanges(trimWhitespacesCheckBox);
        settings.getConvertEmptyStringsToNull().applyChanges(convertEmptyToNullCheckBox);
        settings.getSelectContentOnCellEdit().applyChanges(selectContentOnEditCheckBox);
        settings.getLargeValuePreviewActive().applyChanges(largeValuePreviewActiveCheckBox);
    }

    public void resetFormChanges() {
        DataEditorGeneralSettings settings = getConfiguration();
        settings.getFetchBlockSize().resetChanges(fetchBlockSizeTextField);
        settings.getFetchTimeout().resetChanges(fetchTimeoutTextField);
        settings.getTrimWhitespaces().resetChanges(trimWhitespacesCheckBox);
        settings.getConvertEmptyStringsToNull().resetChanges(convertEmptyToNullCheckBox);
        settings.getSelectContentOnCellEdit().resetChanges(selectContentOnEditCheckBox);
        settings.getLargeValuePreviewActive().resetChanges(largeValuePreviewActiveCheckBox);    }
}
