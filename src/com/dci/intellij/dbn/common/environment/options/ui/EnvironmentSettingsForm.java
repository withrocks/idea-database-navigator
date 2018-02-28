package com.dci.intellij.dbn.common.environment.options.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.environment.EnvironmentTypeBundle;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.environment.options.EnvironmentVisibilitySettings;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentChangeListener;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;
import java.awt.BorderLayout;
import java.awt.Dimension;

public class EnvironmentSettingsForm extends ConfigurationEditorForm<EnvironmentSettings> {
    private JPanel mainPanel;
    private JCheckBox connectionTabsCheckBox;
    private JCheckBox objectEditorTabsCheckBox;
    private JCheckBox scriptEditorTabsCheckBox;
    private JCheckBox dialogHeadersCheckBox;
    private JCheckBox executionResultTabsCheckBox;
    private JPanel environmentTypesPanel;
    private JPanel environmentApplicabilityPanel;
    private JPanel environmentTypesTablePanel;
    private EnvironmentTypesEditorTable environmentTypesTable;

    public EnvironmentSettingsForm(EnvironmentSettings settings) {
        super(settings);
        environmentTypesTable = new EnvironmentTypesEditorTable(settings.getProject(), settings.getEnvironmentTypes());

        updateBorderTitleForeground(environmentTypesPanel);
        updateBorderTitleForeground(environmentApplicabilityPanel);

        EnvironmentVisibilitySettings visibilitySettings = settings.getVisibilitySettings();
        visibilitySettings.getConnectionTabs().resetChanges(connectionTabsCheckBox);
        visibilitySettings.getObjectEditorTabs().resetChanges(objectEditorTabsCheckBox);
        visibilitySettings.getScriptEditorTabs().resetChanges(scriptEditorTabsCheckBox);
        visibilitySettings.getDialogHeaders().resetChanges(dialogHeadersCheckBox);
        visibilitySettings.getExecutionResultTabs().resetChanges(executionResultTabsCheckBox);

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(environmentTypesTable);
        decorator.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                environmentTypesTable.insertRow();
            }
        });
        decorator.setRemoveAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                environmentTypesTable.removeRow();
            }
        });
        decorator.setMoveUpAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                environmentTypesTable.moveRowUp();
            }
        });
        decorator.setMoveDownAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                environmentTypesTable.moveRowDown();
            }
        });
        decorator.addExtraAction(new AnActionButton("Revert Changes", Icons.ACTION_REVERT_CHANGES) {
            @Override
            public void actionPerformed(AnActionEvent anActionEvent) {
                TableCellEditor cellEditor = environmentTypesTable.getCellEditor();
                if (cellEditor != null) {
                    cellEditor.cancelCellEditing();
                }
                environmentTypesTable.setEnvironmentTypes(EnvironmentTypeBundle.DEFAULT);
            }

        });
        decorator.setPreferredSize(new Dimension(-1, 300));
        JPanel panel = decorator.createPanel();
        environmentTypesTablePanel.add(panel, BorderLayout.CENTER);
        registerComponents(mainPanel);
    }
    
    public JPanel getComponent() {
        return mainPanel;
    }
    
    public void applyFormChanges() throws ConfigurationException {
        EnvironmentSettings settings = getConfiguration();
        EnvironmentTypesTableModel model = environmentTypesTable.getModel();
        model.validate();
        final EnvironmentTypeBundle environmentTypeBundle = model.getEnvironmentTypes();
        final boolean settingsChanged = settings.setEnvironmentTypes(environmentTypeBundle);

        EnvironmentVisibilitySettings visibilitySettings = settings.getVisibilitySettings();
        final boolean visibilityChanged =
            visibilitySettings.getConnectionTabs().applyChanges(connectionTabsCheckBox) ||
            visibilitySettings.getObjectEditorTabs().applyChanges(objectEditorTabsCheckBox) ||
            visibilitySettings.getScriptEditorTabs().applyChanges(scriptEditorTabsCheckBox)||
            visibilitySettings.getDialogHeaders().applyChanges(dialogHeadersCheckBox)||
            visibilitySettings.getExecutionResultTabs().applyChanges(executionResultTabsCheckBox);

        new SettingsChangeNotifier() {
            @Override
            public void notifyChanges() {
                if (settingsChanged || visibilityChanged) {
                    EnvironmentChangeListener listener = EventManager.notify(getConfiguration().getProject(), EnvironmentChangeListener.TOPIC);
                    listener.configurationChanged();
                }
            }
        };
    }

    public void resetFormChanges() {
        EnvironmentSettings settings = getConfiguration();
        environmentTypesTable.getModel().setEnvironmentTypes(settings.getEnvironmentTypes());

        EnvironmentVisibilitySettings visibilitySettings = settings.getVisibilitySettings();
        visibilitySettings.getConnectionTabs().resetChanges(connectionTabsCheckBox);
        visibilitySettings.getObjectEditorTabs().resetChanges(objectEditorTabsCheckBox);
        visibilitySettings.getScriptEditorTabs().resetChanges(scriptEditorTabsCheckBox);
        visibilitySettings.getDialogHeaders().resetChanges(dialogHeadersCheckBox);
        visibilitySettings.getExecutionResultTabs().resetChanges(executionResultTabsCheckBox);
    }
}
