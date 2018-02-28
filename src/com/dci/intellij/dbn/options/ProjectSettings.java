package com.dci.intellij.dbn.options;

import javax.swing.Icon;
import javax.swing.JComponent;
import org.jdom.Element;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.code.common.style.options.ProjectCodeStyleSettings;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.code.options.CodeEditorSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.navigation.options.NavigationSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.dci.intellij.dbn.options.ui.ProjectSettingsEditorForm;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;

public class ProjectSettings
        extends CompositeProjectConfiguration<ProjectSettingsEditorForm>
        implements SearchableConfigurable.Parent {


    private GeneralProjectSettings generalSettings;
    private DatabaseBrowserSettings browserSettings;
    private NavigationSettings navigationSettings;
    private DataGridSettings dataGridSettings;
    private DataEditorSettings dataEditorSettings;
    private CodeEditorSettings codeEditorSettings;
    private CodeCompletionSettings codeCompletionSettings;
    private ProjectCodeStyleSettings codeStyleSettings;
    private ExecutionEngineSettings executionEngineSettings;
    private OperationSettings operationSettings;
    private DDLFileSettings ddlFileSettings;
    private ConnectionBundleSettings connectionSettings;


    public ProjectSettings(Project project) {
        super(project);
        generalSettings = new GeneralProjectSettings(project);
        browserSettings = new DatabaseBrowserSettings(project);
        navigationSettings = new NavigationSettings(project);
        codeStyleSettings = new ProjectCodeStyleSettings(project);
        codeEditorSettings = new CodeEditorSettings(project);
        dataGridSettings = new DataGridSettings(project);
        dataEditorSettings = new DataEditorSettings(project);
        codeCompletionSettings = new CodeCompletionSettings(project);
        executionEngineSettings = new ExecutionEngineSettings(project);
        operationSettings = new OperationSettings(project);
        ddlFileSettings = new DDLFileSettings(project);
        connectionSettings = new ConnectionBundleSettings(project);


        if ((project.isDefault() && ApplicationManager.getApplication().isActive()) || project.isInitialized()) {
            ProjectSettings projectSettings = ProjectSettingsManager.getSettings(project);
            Element settings = new Element("settings");
            projectSettings.writeConfiguration(settings);
            readConfiguration(settings);
        }
    }

    public String getHelpTopic() {
        ProjectSettingsEditorForm settingsEditor = getSettingsEditor();
        if (settingsEditor == null) {
            return super.getHelpTopic();
        } else {
            Configuration selectedConfiguration = settingsEditor.getActiveSettings();
            return selectedConfiguration.getHelpTopic();
        }
    }

    @Override
    public JComponent createComponent() {
        return null;//super.createComponent();
    }

    public JComponent createCustomComponent() {
        return super.createComponent();
    }



    /*********************************************************
    *                         Custom                        *
    *********************************************************/
    public GeneralProjectSettings getGeneralSettings() {
        return generalSettings;
    }

    public DatabaseBrowserSettings getBrowserSettings() {
        return browserSettings;
    }

    public NavigationSettings getNavigationSettings() {
        return navigationSettings;
    }

    public ConnectionBundleSettings getConnectionSettings() {
        return connectionSettings;
    }

    public DataGridSettings getDataGridSettings() {
        return dataGridSettings;
    }

    public DataEditorSettings getDataEditorSettings() {
        return dataEditorSettings;
    }

    public CodeEditorSettings getCodeEditorSettings() {
        return codeEditorSettings;
    }

    public CodeCompletionSettings getCodeCompletionSettings() {
        return codeCompletionSettings;
    }

    public ProjectCodeStyleSettings getCodeStyleSettings() {
        return codeStyleSettings;
    }

    public ExecutionEngineSettings getExecutionEngineSettings() {
        return executionEngineSettings;
    }

    public OperationSettings getOperationSettings() {
        return operationSettings;
    }

    public DDLFileSettings getDdlFileSettings() {
        return ddlFileSettings;
    }

    @Nls
    public String getDisplayName() {
        return "Database Navigator";
    }

    @Nullable
    public Icon getIcon() {
        return Icons.DATABASE_NAVIGATOR;
    }

    public Configurable[] getConfigurables() {
        return getConfigurations();
    }

    @Nullable
    public Configuration getConfiguration(ConfigId settingsId) {
        for (Configurable configurable : getConfigurables()) {
            TopLevelConfig topLevelConfig = (TopLevelConfig) configurable;
            if (topLevelConfig.getConfigId() == settingsId) {
                return (Configuration) configurable;
            }
        }
        return null;
    }


    /*********************************************************
     *                    Configuration                      *
     *********************************************************/
    public ProjectSettingsEditorForm createConfigurationEditor() {
        return new ProjectSettingsEditorForm(this);
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                connectionSettings,
                browserSettings,
                navigationSettings,
                //codeStyleSettings,
                dataGridSettings,
                dataEditorSettings,
                codeEditorSettings,
                codeCompletionSettings,
                executionEngineSettings,
                operationSettings,
                ddlFileSettings,
                generalSettings};
    }

    /*********************************************************
    *              SearchableConfigurable.Parent             *
    *********************************************************/
    public boolean hasOwnContent() {
        return false;
    }

    public boolean isVisible() {
        return true;
    }

    @NotNull
    public String getId() {
        return "DBNavigator.Project.Settings";
    }

    public Runnable enableSearch(String option) {
        return null;
    }
}
