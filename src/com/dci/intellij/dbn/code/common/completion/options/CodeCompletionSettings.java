package com.dci.intellij.dbn.code.common.completion.options;

import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.code.common.completion.options.filter.CodeCompletionFiltersSettings;
import com.dci.intellij.dbn.code.common.completion.options.general.CodeCompletionFormatSettings;
import com.dci.intellij.dbn.code.common.completion.options.sorting.CodeCompletionSortingSettings;
import com.dci.intellij.dbn.code.common.completion.options.ui.CodeCompletionSettingsForm;
import com.dci.intellij.dbn.common.options.CompositeProjectConfiguration;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.options.TopLevelConfig;
import com.intellij.openapi.project.Project;

public class CodeCompletionSettings extends CompositeProjectConfiguration<CodeCompletionSettingsForm> implements TopLevelConfig {
    private CodeCompletionFiltersSettings filterSettings;
    private CodeCompletionSortingSettings sortingSettings;
    private CodeCompletionFormatSettings formatSettings;

    public CodeCompletionSettings(Project project) {
        super(project);
        filterSettings = new CodeCompletionFiltersSettings();
        sortingSettings = new CodeCompletionSortingSettings();
        formatSettings = new CodeCompletionFormatSettings();
        loadDefaults();
    }

    public static CodeCompletionSettings getInstance(Project project) {
        return ProjectSettingsManager.getSettings(project).getCodeCompletionSettings();
    }

    @NotNull
    @Override
    public String getId() {
        return "DBNavigator.Project.CodeCompletionSettings";
    }


    public String getDisplayName() {
        return "Code Completion";
    }

    public String getHelpTopic() {
        return "codeEditor";
    }

    @Override
    public ConfigId getConfigId() {
        return ConfigId.CODE_COMPLETION;
    }

    private void loadDefaults() {
       try {
           Document document = CommonUtil.loadXmlFile(getClass(), "default-settings.xml");
           Element root = document.getRootElement();
           readConfiguration(root);
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

   /*********************************************************
    *                         Custom                        *
    *********************************************************/
    public CodeCompletionFiltersSettings getFilterSettings() {
        return filterSettings;
    }

    public CodeCompletionSortingSettings getSortingSettings() {
        return sortingSettings;
    }

    public CodeCompletionFormatSettings getFormatSettings() {
        return formatSettings;
    }

    /*********************************************************
    *                     Configuration                      *
    *********************************************************/

    protected CodeCompletionSettingsForm createConfigurationEditor() {
        return new CodeCompletionSettingsForm(this);
    }

    @Override
    public String getConfigElementName() {
        return "code-completion-settings";
    }

    protected Configuration[] createConfigurations() {
        return new Configuration[] {
                filterSettings,
                sortingSettings,
                formatSettings};
    }
}
