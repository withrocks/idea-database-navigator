package com.dci.intellij.dbn.browser.options.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;


public class DatabaseBrowserSettingsForm extends CompositeConfigurationEditorForm<DatabaseBrowserSettings> {
    private JPanel mainPanel;
    private JPanel generalSettingsPanel;
    private JPanel filterSettingsPanel;
    private JPanel sortingSettingsPanel;

    public DatabaseBrowserSettingsForm(DatabaseBrowserSettings settings) {
        super(settings);
        generalSettingsPanel.add(settings.getGeneralSettings().createComponent(), BorderLayout.CENTER);
        filterSettingsPanel.add(settings.getFilterSettings().createComponent(), BorderLayout.CENTER);
        sortingSettingsPanel.add(settings.getSortingSettings().createComponent(), BorderLayout.CENTER);
    }

    public JComponent getComponent() {
        return mainPanel;
    }
}
