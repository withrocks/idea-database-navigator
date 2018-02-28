package com.dci.intellij.dbn.object.dependency.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.project.Project;

public class ObjectDependencyTreeDialog extends DBNDialog<ObjectDependencyTreeForm> {
    public ObjectDependencyTreeDialog(Project project, DBSchemaObject schemaObject) {
        super(project, "Object Dependency Tree", true);
        this.component = new ObjectDependencyTreeForm(this, schemaObject);
        setModal(false);
        setResizable(true);
        init();
    }

    public void doOKAction() {
    }

    public void doCancelAction() {
        super.doCancelAction();
    }

}
