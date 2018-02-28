package com.dci.intellij.dbn.object.dependency;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.dependency.ui.ObjectDependencyTreeDialog;
import com.intellij.openapi.project.Project;

public class ObjectDependencyManager extends AbstractProjectComponent {
    private ObjectDependencyManager(final Project project) {
        super(project);
    }

    public static ObjectDependencyManager getInstance(Project project) {
        return project.getComponent(ObjectDependencyManager.class);
    }

    public void openDependencyTree(DBSchemaObject schemaObject) {
        ObjectDependencyTreeDialog dependencyTreeDialog = new ObjectDependencyTreeDialog(getProject(), schemaObject);
        dependencyTreeDialog.show();
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.ObjectDependencyManager";
    }

    public void disposeComponent() {
        super.disposeComponent();
    }
}
