package com.dci.intellij.dbn.connection.operation.options;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.operation.options.ui.OperationsSettingsForm;
import com.dci.intellij.dbn.connection.transaction.options.TransactionManagerSettings;
import com.dci.intellij.dbn.editor.session.options.SessionBrowserSettings;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;

public class OperationSettings extends CompositeProjectConfiguration<OperationsSettingsForm> implements TopLevelConfig {
    private TransactionManagerSettings transactionManagerSettings = new TransactionManagerSettings();
    private SessionBrowserSettings sessionBrowserSettings = new SessionBrowserSettings();

    public OperationSettings(Project project) {
        super(project);
    }

    public static OperationSettings getInstance(Project project) {
        return ProjectSettingsManager.getSettings(project).getOperationSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.OperationSettings";
    }

    public String getDisplayName() {
        return "Operations";
    }

    public String getHelpTopic() {
        return "operations";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.OPERATIONS;
    }

    @Override
    protected Configuration<OperationsSettingsForm> getOriginalSettings() {
        return getInstance(getProject());
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/

    public TransactionManagerSettings getTransactionManagerSettings() {
       return transactionManagerSettings;
    }

    public SessionBrowserSettings getSessionBrowserSettings() {
        return sessionBrowserSettings;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    public OperationsSettingsForm createConfigurationEditor() {
        return new OperationsSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "operation-settings";
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                transactionManagerSettings,
                sessionBrowserSettings};
    }
}
