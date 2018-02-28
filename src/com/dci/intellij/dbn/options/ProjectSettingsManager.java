package com.dci.intellij.dbn.options;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.code.common.style.options.ProjectCodeStyleSettings;
import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettingsListener;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.navigation.options.NavigationSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.dci.intellij.dbn.options.ui.ProjectSettingsDialog;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

@State(
        name = "DBNavigator.Project.Settings",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class ProjectSettingsManager implements ProjectComponent, PersistentStateComponent<Element> {
    private ProjectSettings projectSettings;

    private ProjectSettingsManager(Project project) {
        projectSettings = new ProjectSettings(project);
    }

    public static ProjectSettingsManager getInstance(Project project) {
        return project.getComponent(ProjectSettingsManager.class);
    }

    public static ProjectSettings getSettings(Project project) {
        if (project.isDefault()) {
            return DefaultProjectSettingsManager.getInstance().getDefaultProjectSettings();
        } else {
            return getInstance(project).projectSettings;
        }
    }

    public ProjectSettings getProjectSettings() {
        return projectSettings;
    }

    public GeneralProjectSettings getGeneralSettings() {
        return projectSettings.getGeneralSettings();
    }

    public DatabaseBrowserSettings getBrowserSettings() {
        return projectSettings.getBrowserSettings();
    }

    public NavigationSettings getNavigationSettings() {
        return projectSettings.getNavigationSettings();
    }

    public ConnectionBundleSettings getConnectionSettings() {
        return projectSettings.getConnectionSettings();
    }

    public DataGridSettings getDataGridSettings() {
        return projectSettings.getDataGridSettings();
    }

    public DataEditorSettings getDataEditorSettings() {
        return projectSettings.getDataEditorSettings();
    }

    public CodeCompletionSettings getCodeCompletionSettings() {
        return projectSettings.getCodeCompletionSettings();
    }

    public ProjectCodeStyleSettings getCodeStyleSettings() {
        return projectSettings.getCodeStyleSettings();
    }

    public OperationSettings getOperationSettings() {
        return projectSettings.getOperationSettings();
    }

    public ExecutionEngineSettings getExecutionEngineSettings() {
        return projectSettings.getExecutionEngineSettings();
    }

    public DDLFileSettings getDdlFileSettings() {
        return projectSettings.getDdlFileSettings();
    }

    public void openDefaultProjectSettings() {
        Project project = ProjectManager.getInstance().getDefaultProject();
        ProjectSettingsDialog globalSettingsDialog = new ProjectSettingsDialog(project);
        globalSettingsDialog.show();
    }

    public void openProjectSettings(ConfigId configId) {
        Project project = getProject();
        ProjectSettingsDialog globalSettingsDialog = new ProjectSettingsDialog(project);
        globalSettingsDialog.focusSettings(configId);
        globalSettingsDialog.show();
    }

    public void openConnectionSettings(@Nullable ConnectionHandler connectionHandler) {
        Project project = getProject();
        ProjectSettingsDialog globalSettingsDialog = new ProjectSettingsDialog(project);
        globalSettingsDialog.focusConnectionSettings(connectionHandler);
        globalSettingsDialog.show();
    }

    private Project getProject() {
        return projectSettings.getProject();
    }

    @Override
    public void projectOpened() {
    }

    @Override
    public void projectClosed() {}

    @Override
    public void initComponent() {
        importDefaultSettings(true);
    }

    public void exportToDefaultSettings() {
        final Project project = getProject();
        MessageUtil.showQuestionDialog(
                project, "Default Project Settings",
                "This will overwrite your default settings with the ones from the current project (including database connections configuration). \nAre you sure you want to continue?",
                new String[]{"Yes", "No"}, 0,
                new SimpleTask() {
                    @Override
                    public void execute() {
                        if (getResult() == 0) {
                            try {
                                Element element = new Element("state");
                                projectSettings.writeConfiguration(element);

                                ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.set(true);
                                ProjectSettings defaultProjectSettings = DefaultProjectSettingsManager.getInstance().getDefaultProjectSettings();
                                defaultProjectSettings.readConfiguration(element);
                                MessageUtil.showInfoDialog(project, "Project Settings", "Project settings exported as default");
                            } finally {
                                ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.set(false);
                            }
                        }
                    }
                });
    }

    public void importDefaultSettings(final boolean isNewProject) {
        final Project project = getProject();
        Boolean settingsLoaded = project.getUserData(DBNDataKeys.PROJECT_SETTINGS_LOADED_KEY);
        if (settingsLoaded == null || !settingsLoaded || !isNewProject) {
            String message = isNewProject ?
                    "Do you want to import the default project settings into project \"" + project.getName() + "\"?":
                    "Your current settings will be overwritten with the default project settings, including database connections configuration. \nAre you sure you want to import the default project settings into project \"" + project.getName() + "\"?";
            MessageUtil.showQuestionDialog(
                    project, "Default Project Settings",
                    message,
                    new String[]{"Yes", "No"}, 0,
                    new SimpleTask() {
                        @Override
                        public void execute() {
                            if (getResult() == 0) {
                                try {
                                    Element element = new Element("state");
                                    ProjectSettings defaultProjectSettings = DefaultProjectSettingsManager.getInstance().getDefaultProjectSettings();
                                    defaultProjectSettings.writeConfiguration(element);

                                    ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.set(true);
                                    projectSettings.readConfiguration(element);
                                    ConnectionBundleSettingsListener listener = EventManager.notify(project, ConnectionBundleSettingsListener.TOPIC);
                                    if (listener != null) listener.settingsChanged();
                                    if (!isNewProject) {
                                        MessageUtil.showInfoDialog(project, "Project Settings", "Default project settings loaded to project \"" + project.getName() + "\".");
                                    }
                                } finally {
                                    ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.set(false);
                                }
                            }

                        }
                    });
        }
    }

    @Override
    public void disposeComponent() {}

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.Project.Settings";
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        projectSettings.writeConfiguration(element);
        return element;
    }

    @Override
    public void loadState(Element element) {
        projectSettings.readConfiguration(element);
        getProject().putUserData(DBNDataKeys.PROJECT_SETTINGS_LOADED_KEY, true);
    }
}
