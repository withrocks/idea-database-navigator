package com.dci.intellij.dbn.code.psql.style.options.ui;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCustomSettings;
import com.dci.intellij.dbn.code.psql.style.options.PSQLCodeStyleSettings;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;

import javax.swing.JPanel;
import java.awt.BorderLayout;

public class PSQLCodeStyleSettingsEditorForm extends CompositeConfigurationEditorForm<CodeStyleCustomSettings> {
    private JPanel mainPanel;
    private JPanel casePanel;
    private JPanel previewPanel;
    private JPanel attributesPanel;

    public PSQLCodeStyleSettingsEditorForm(PSQLCodeStyleSettings settings) {
        super(settings);
        casePanel.add(settings.getCaseSettings().createComponent(), BorderLayout.CENTER);
        attributesPanel.add(settings.getFormattingSettings().createComponent(), BorderLayout.CENTER);
        updateBorderTitleForeground(previewPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }
}