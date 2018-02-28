package com.dci.intellij.dbn.code.common.completion.options.general.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.dci.intellij.dbn.code.common.completion.options.general.CodeCompletionFormatSettings;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.intellij.openapi.options.ConfigurationException;

public class CodeCompletionFormatSettingsForm extends ConfigurationEditorForm<CodeCompletionFormatSettings> {
    private JCheckBox enforceCaseCheckBox;
    private JPanel mainPanel;

    public CodeCompletionFormatSettingsForm(CodeCompletionFormatSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        resetFormChanges();

        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        CodeCompletionFormatSettings settings = getConfiguration();
        settings.setEnforceCodeStyleCase(enforceCaseCheckBox.isSelected());
    }

    public void resetFormChanges() {
        CodeCompletionFormatSettings settings = getConfiguration();
        enforceCaseCheckBox.setSelected(settings.isEnforceCodeStyleCase());
    }
}
