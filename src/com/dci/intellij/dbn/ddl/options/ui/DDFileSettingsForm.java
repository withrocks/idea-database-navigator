package com.dci.intellij.dbn.ddl.options.ui;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;

import javax.swing.*;
import java.awt.*;


public class DDFileSettingsForm extends CompositeConfigurationEditorForm<DDLFileSettings> {
    private JPanel mainPanel;
    private JPanel extensionSettingsPanel;
    private JPanel generalSettingsPanel;

    public DDFileSettingsForm(DDLFileSettings settings) {
        super(settings);
        extensionSettingsPanel.add(settings.getExtensionSettings().createComponent(), BorderLayout.CENTER);
        generalSettingsPanel.add(settings.getGeneralSettings().createComponent(), BorderLayout.CENTER);
    }

    public JPanel getComponent() {
        return mainPanel;
    }
}
