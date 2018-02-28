package com.dci.intellij.dbn.code.common.style.options;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.code.common.style.options.ui.CodeStyleSettingsForm;
import com.dci.intellij.dbn.code.psql.style.options.PSQLCodeStyleSettings;
import com.dci.intellij.dbn.code.psql.style.options.PSQLCustomCodeStyleSettings;
import com.dci.intellij.dbn.code.sql.style.options.SQLCodeStyleSettings;
import com.dci.intellij.dbn.code.sql.style.options.SQLCustomCodeStyleSettings;
import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;

public class ProjectCodeStyleSettings extends CompositeProjectConfiguration<CodeStyleSettingsForm> implements TopLevelConfig {
    public ProjectCodeStyleSettings(Project project){
        super(project);
    }

    public static ProjectCodeStyleSettings getInstance(Project project) {
        return ProjectSettingsManager.getSettings(project).getCodeStyleSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.CodeStyleSettings";
    }

    public String getDisplayName() {
        return "Code Style";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.CODE_STYLE;
    }

    public CodeStyleSettingsForm createConfigurationEditor() {
        return new CodeStyleSettingsForm(this);
    }

    public SQLCodeStyleSettings getSQLCodeStyleSettings() {
        CodeStyleSettings codeStyleSettings = getCodeStyleSettings();

        SQLCustomCodeStyleSettings customCodeStyleSettings =
                codeStyleSettings.getCustomSettings(SQLCustomCodeStyleSettings.class);
        return customCodeStyleSettings.getCodeStyleSettings();
    }

    public PSQLCodeStyleSettings getPSQLCodeStyleSettings() {
        CodeStyleSettings codeStyleSettings = getCodeStyleSettings();

        PSQLCustomCodeStyleSettings customCodeStyleSettings =
                codeStyleSettings.getCustomSettings(PSQLCustomCodeStyleSettings.class);
        return customCodeStyleSettings.getCodeStyleSettings();
    }

    private CodeStyleSettings getCodeStyleSettings() {
        CodeStyleSettings codeStyleSettings;
        if (CodeStyleSettingsManager.getInstance().USE_PER_PROJECT_SETTINGS) {
            codeStyleSettings = CodeStyleSettingsManager.getSettings(getProject());
        } else {
            codeStyleSettings = CodeStyleSettingsManager.getInstance().getCurrentSettings();
        }
        return codeStyleSettings;
    }

    /*********************************************************
    *                     Configuration                      *
    *********************************************************/
    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                getSQLCodeStyleSettings(),
                getPSQLCodeStyleSettings()};
    }

    public void readConfiguration(Element element) {

    }

    public void writeConfiguration(Element element) {

    }

}
