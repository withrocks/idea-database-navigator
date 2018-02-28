package com.dci.intellij.dbn.common.options;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.intellij.openapi.project.Project;

public abstract class CompositeProjectConfiguration<T extends CompositeConfigurationEditorForm> extends CompositeConfiguration<T> {
    private Project project;

    public CompositeProjectConfiguration(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }


}
