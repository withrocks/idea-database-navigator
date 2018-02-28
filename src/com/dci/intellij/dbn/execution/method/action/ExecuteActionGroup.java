package com.dci.intellij.dbn.execution.method.action;

import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBProgram;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class ExecuteActionGroup extends DefaultActionGroup {

    public ExecuteActionGroup(DBSchemaObject object) {
        super("Execute", true);
        if (object.getContentType() == DBContentType.CODE_SPEC_AND_BODY) {
            add(new RunProgramMethodAction((DBProgram) object));
            add(new DebugProgramMethodAction((DBProgram) object));
        } else {
            add(new RunMethodAction((DBMethod) object));
            add(new DebugMethodAction((DBMethod) object));
        }
    }
}