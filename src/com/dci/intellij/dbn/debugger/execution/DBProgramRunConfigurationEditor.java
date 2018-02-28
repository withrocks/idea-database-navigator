package com.dci.intellij.dbn.debugger.execution;

import com.dci.intellij.dbn.debugger.execution.ui.DBProgramRunConfigurationEditorForm;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

public class DBProgramRunConfigurationEditor extends SettingsEditor<DBProgramRunConfiguration> {
    private DBProgramRunConfigurationEditorForm configurationEditorComponent;
    private DBProgramRunConfiguration configuration;

    public DBProgramRunConfigurationEditor(DBProgramRunConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void resetEditorFrom(DBProgramRunConfiguration configuration) {
        configurationEditorComponent.readConfiguration(configuration);
    }

    @Override
    protected void applyEditorTo(DBProgramRunConfiguration configuration) throws ConfigurationException {
        configurationEditorComponent.writeConfiguration(configuration);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        configurationEditorComponent = new DBProgramRunConfigurationEditorForm(configuration);
        return configurationEditorComponent.getComponent();
    }

    @Override
    protected void disposeEditor() {
        //configurationEditorComponent.dispose();
    }

    public void setExecutionInput(MethodExecutionInput executionInput) {
        if (configurationEditorComponent != null) {
            configurationEditorComponent.setExecutionInput(executionInput, true);
        }
    }
}
