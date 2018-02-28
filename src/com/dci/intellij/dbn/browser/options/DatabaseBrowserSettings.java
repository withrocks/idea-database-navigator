package com.dci.intellij.dbn.browser.options;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.options.ui.DatabaseBrowserSettingsForm;
import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;

public class DatabaseBrowserSettings extends CompositeProjectConfiguration<DatabaseBrowserSettingsForm> implements TopLevelConfig {
    private DatabaseBrowserGeneralSettings generalSettings;
    private DatabaseBrowserFilterSettings filterSettings;
    private DatabaseBrowserSortingSettings sortingSettings;

    public DatabaseBrowserSettings(Project project) {
        super(project);
        filterSettings = new DatabaseBrowserFilterSettings(project);
        generalSettings = new DatabaseBrowserGeneralSettings(project);
        sortingSettings = new DatabaseBrowserSortingSettings(project);
    }

    @Override
    public DatabaseBrowserSettingsForm createConfigurationEditor() {
        return new DatabaseBrowserSettingsForm(this);
    }

    public static DatabaseBrowserSettings getInstance(Project project) {
        return ProjectSettingsManager.getSettings(project).getBrowserSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.DatabaseBrowserSettings";
    }

    public String getDisplayName() {
        return "Database Browser";
    }

    public String getHelpTopic() {
        return "browserSettings";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.BROWSER;
    }

    @Override
    protected Configuration<DatabaseBrowserSettingsForm> getOriginalSettings() {
        return getInstance(getProject());
    }

    /*********************************************************
     *                        Custom                         *
     *********************************************************/

    public DatabaseBrowserGeneralSettings getGeneralSettings() {
        return generalSettings;
    }

    public DatabaseBrowserFilterSettings getFilterSettings() {
        return filterSettings;
    }

    public DatabaseBrowserSortingSettings getSortingSettings() {
        return sortingSettings;
    }

    /*********************************************************
     *                     Configuration                     *
     *********************************************************/

    @Override
    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                generalSettings,
                filterSettings,
                sortingSettings};
    }

    @Override
    public String getConfigElementName() {
        return "browser-settings";
    }
}
