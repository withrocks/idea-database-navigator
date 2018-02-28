package com.dci.intellij.dbn.editor.code.options.ui;

import javax.swing.JPanel;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.editor.code.options.CodeEditorSettings;

public class CodeEditorSettingsForm extends CompositeConfigurationEditorForm<CodeEditorSettings> {
    private JPanel mainPanel;
    private JPanel generalSettingsPanel;

    public CodeEditorSettingsForm(CodeEditorSettings settings) {
        super(settings);
        generalSettingsPanel.add(settings.getGeneralSettings().createComponent(), BorderLayout.CENTER);
        resetFormChanges();
    }


    public JPanel getComponent() {
        return mainPanel;
    }
}
