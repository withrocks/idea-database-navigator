package com.dci.intellij.dbn.data.grid.options;

import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.data.grid.options.ui.DataGridSettingsForm;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class DataGridSettings extends CompositeProjectConfiguration<DataGridSettingsForm> implements TopLevelConfig {
    private DataGridGeneralSettings generalSettings;
    private DataGridSortingSettings sortingSettings;
    private DataGridTrackingColumnSettings trackingColumnSettings;

    public DataGridSettings(Project project) {
        super(project);
        generalSettings = new DataGridGeneralSettings(project);
        sortingSettings = new DataGridSortingSettings(project);
        trackingColumnSettings = new DataGridTrackingColumnSettings(project);
    }

    public static DataGridSettings getInstance(Project project) {
        return ProjectSettingsManager.getSettings(project).getDataGridSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.DataGridSettings";
    }

    public String getDisplayName() {
        return "Data Grid";
    }

    public String getHelpTopic() {
        return "dataGrid";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.DATA_GRID;
    }

    @Override
    protected Configuration<DataGridSettingsForm> getOriginalSettings() {
        return getInstance(getProject());
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/

    public DataGridGeneralSettings getGeneralSettings() {
        return generalSettings;
    }

    public DataGridSortingSettings getSortingSettings() {
        return sortingSettings;
    }

    public DataGridTrackingColumnSettings getTrackingColumnSettings() {
       return trackingColumnSettings;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/
    public DataGridSettingsForm createConfigurationEditor() {
        return new DataGridSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "dataset-grid-settings";
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                generalSettings,
                sortingSettings,
                trackingColumnSettings
        };
    }
}
