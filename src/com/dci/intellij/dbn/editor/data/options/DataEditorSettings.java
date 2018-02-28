package com.dci.intellij.dbn.editor.data.options;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.editor.data.options.ui.DataEditorSettingsForm;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;

public class DataEditorSettings extends CompositeProjectConfiguration<DataEditorSettingsForm> implements TopLevelConfig {
    private DataEditorPopupSettings popupSettings = new DataEditorPopupSettings();
    private DataEditorValueListPopupSettings valueListPopupSettings = new DataEditorValueListPopupSettings();
    private DataEditorFilterSettings filterSettings = new DataEditorFilterSettings();
    private DataEditorGeneralSettings generalSettings = new DataEditorGeneralSettings();
    private DataEditorQualifiedEditorSettings qualifiedEditorSettings = new DataEditorQualifiedEditorSettings();
    private DataEditorRecordNavigationSettings recordNavigationSettings = new DataEditorRecordNavigationSettings();

    public DataEditorSettings(Project project) {
        super(project);
    }

    public static DataEditorSettings getInstance(Project project) {
        return ProjectSettingsManager.getSettings(project).getDataEditorSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.DataEditorSettings";
    }

    public String getDisplayName() {
        return "Data Editor";
    }

    public String getHelpTopic() {
        return "dataEditor";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.DATA_EDITOR;
    }

    @Override
    protected Configuration<DataEditorSettingsForm> getOriginalSettings() {
        return getInstance(getProject());
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/

    public DataEditorValueListPopupSettings getValueListPopupSettings() {
        return valueListPopupSettings;
    }

    public DataEditorPopupSettings getPopupSettings() {
       return popupSettings;
    }

    public DataEditorGeneralSettings getGeneralSettings() {
        return generalSettings;
    }

    public DataEditorFilterSettings getFilterSettings() {
        return filterSettings;
    }

    public DataEditorQualifiedEditorSettings getQualifiedEditorSettings() {
        return qualifiedEditorSettings;
    }

    public DataEditorRecordNavigationSettings getRecordNavigationSettings() {
        return recordNavigationSettings;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    public DataEditorSettingsForm createConfigurationEditor() {
        return new DataEditorSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "dataset-editor-settings";
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                popupSettings,
                valueListPopupSettings,
                generalSettings,
                filterSettings,
                qualifiedEditorSettings,
                recordNavigationSettings};
    }
}
