package com.dci.intellij.dbn.object.factory.ui.common;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.object.factory.DatabaseObjectFactory;
import com.dci.intellij.dbn.object.factory.ObjectFactoryInput;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

public class ObjectFactoryInputDialog extends DBNDialog<ObjectFactoryInputForm> {
    public ObjectFactoryInputDialog(Project project, ObjectFactoryInputForm inputForm) {
        super(project, "Create " + inputForm.getObjectType().getName(), true);
        this.component = inputForm;
        setModal(true);
        setResizable(true);
        Disposer.register(this, inputForm);
        init();
    }

    public void doOKAction() {
        Project project = component.getConnectionHandler().getProject();
        DatabaseObjectFactory factory = DatabaseObjectFactory.getInstance(project);
        ObjectFactoryInput factoryInput = component.createFactoryInput(null);
        if (factory.createObject(factoryInput)) {
            super.doOKAction();
        }
    }

    public void doCancelAction() {
        super.doCancelAction();
    }
}
