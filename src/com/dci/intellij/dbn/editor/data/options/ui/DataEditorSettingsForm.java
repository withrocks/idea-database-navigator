package com.dci.intellij.dbn.editor.data.options.ui;

import javax.swing.JPanel;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;

public class DataEditorSettingsForm extends CompositeConfigurationEditorForm<DataEditorSettings> {
    private JPanel mainPanel;
    private JPanel textEditorAutopopupPanel;
    private JPanel generalSettingsPanel;
    private JPanel filtersPanel;
    private JPanel valuesListPopupPanel;
    private JPanel lobContentTypesPanel;
    private JPanel recordNavigationPanel;

    public DataEditorSettingsForm(DataEditorSettings settings) {
        super(settings);
        textEditorAutopopupPanel.add(settings.getPopupSettings().createComponent(), BorderLayout.CENTER);
        valuesListPopupPanel.add(settings.getValueListPopupSettings().createComponent(), BorderLayout.CENTER);
        generalSettingsPanel.add(settings.getGeneralSettings().createComponent(), BorderLayout.CENTER);
        filtersPanel.add(settings.getFilterSettings().createComponent(), BorderLayout.CENTER);
        lobContentTypesPanel.add(settings.getQualifiedEditorSettings().createComponent(), BorderLayout.CENTER);
        recordNavigationPanel.add(settings.getRecordNavigationSettings().createComponent(), BorderLayout.CENTER);
        resetFormChanges();
    }


    public JPanel getComponent() {
        return mainPanel;
    }
}
