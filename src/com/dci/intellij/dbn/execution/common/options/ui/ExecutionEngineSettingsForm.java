package com.dci.intellij.dbn.execution.common.options.ui;

import javax.swing.JPanel;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;

public class ExecutionEngineSettingsForm extends CompositeConfigurationEditorForm<ExecutionEngineSettings> {
    private JPanel mainPanel;
    private JPanel statementExecutionPanel;
    private JPanel compilerPanel;
    private JPanel methodExecutionPanel;

    public ExecutionEngineSettingsForm(ExecutionEngineSettings settings) {
        super(settings);
        statementExecutionPanel.add(settings.getStatementExecutionSettings().createComponent(), BorderLayout.CENTER);
        methodExecutionPanel.add(settings.getMethodExecutionSettings().createComponent(), BorderLayout.CENTER);
        compilerPanel.add(settings.getCompilerSettings().createComponent(), BorderLayout.CENTER);
    }

    public JPanel getComponent() {
        return mainPanel;
    }
}
