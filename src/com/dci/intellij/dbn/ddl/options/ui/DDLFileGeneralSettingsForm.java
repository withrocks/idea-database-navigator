package com.dci.intellij.dbn.ddl.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.ddl.options.DDLFileGeneralSettings;
import com.dci.intellij.dbn.ddl.options.listener.DDLFileSettingsChangeListener;
import com.intellij.openapi.options.ConfigurationException;

public class DDLFileGeneralSettingsForm extends ConfigurationEditorForm<DDLFileGeneralSettings> {
    private JPanel mainPanel;
    private JCheckBox lookupDDLFilesCheckBox;
    private JCheckBox createDDLFileCheckBox;
    private JCheckBox synchronizeDDLFilesCheckBox;
    private JCheckBox useQualifiedObjectNamesCheckBox;
    private JCheckBox makeScriptsRerunnableCheckBox;
    private JPanel hintPanel;

    public DDLFileGeneralSettingsForm(DDLFileGeneralSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);

        String hintText = "NOTE: When \"Synchronize\" option is enabled, the DDL file content gets overwritten with the source from the underlying database object whenever this gets saved to database.";
        DBNHintForm hintForm = new DBNHintForm(hintText);
        hintPanel.add(hintForm.getComponent(), BorderLayout.CENTER);

        resetFormChanges();

        boolean synchronizeSelected = synchronizeDDLFilesCheckBox.isSelected();
        useQualifiedObjectNamesCheckBox.setEnabled(synchronizeSelected);
        makeScriptsRerunnableCheckBox.setEnabled(synchronizeSelected);
        hintPanel.setVisible(synchronizeSelected);

        registerComponent(mainPanel);
    }

    @Override
    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getConfiguration().setModified(true);
                if (e.getSource() == synchronizeDDLFilesCheckBox) {
                    boolean synchronizeSelected = synchronizeDDLFilesCheckBox.isSelected();
                    useQualifiedObjectNamesCheckBox.setEnabled(synchronizeSelected);
                    makeScriptsRerunnableCheckBox.setEnabled(synchronizeSelected);
                    hintPanel.setVisible(synchronizeSelected);

                }
            }
        };
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        final DDLFileGeneralSettings settings = getConfiguration();
        settings.getLookupDDLFilesEnabled().applyChanges(lookupDDLFilesCheckBox);
        settings.getCreateDDLFilesEnabled().applyChanges(createDDLFileCheckBox);
        final boolean settingChanged = settings.getSynchronizeDDLFilesEnabled().applyChanges(synchronizeDDLFilesCheckBox);
        settings.getUseQualifiedObjectNames().applyChanges(useQualifiedObjectNamesCheckBox);
        settings.getMakeScriptsRerunnable().applyChanges(makeScriptsRerunnableCheckBox);

        new SettingsChangeNotifier(){
            @Override
            public void notifyChanges() {
                if (settingChanged) {
                    DDLFileSettingsChangeListener listener = EventManager.notify(settings.getProject(), DDLFileSettingsChangeListener.TOPIC);
                    listener.settingsChanged();

                }
            }
        };
    }

    public void resetFormChanges() {
        DDLFileGeneralSettings settings = getConfiguration();
        settings.getLookupDDLFilesEnabled().resetChanges(lookupDDLFilesCheckBox);
        settings.getCreateDDLFilesEnabled().resetChanges(createDDLFileCheckBox);
        settings.getSynchronizeDDLFilesEnabled().resetChanges(synchronizeDDLFilesCheckBox);
        settings.getUseQualifiedObjectNames().resetChanges(useQualifiedObjectNamesCheckBox);
        settings.getMakeScriptsRerunnable().resetChanges(makeScriptsRerunnableCheckBox);
    }
}
