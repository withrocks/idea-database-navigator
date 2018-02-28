package com.dci.intellij.dbn.object.factory.ui;

import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.project.Project;

public class ProcedureFactoryInputForm extends MethodFactoryInputForm {

    public ProcedureFactoryInputForm(Project project, DBSchema schema, DBObjectType objectType, int index) {
        super(project, schema, objectType, index);
    }

    public boolean hasReturnArgument() {
        return false;
    }

    public void dispose() {
        super.dispose();
    }
}