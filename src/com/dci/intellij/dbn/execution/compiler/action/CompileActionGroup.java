package com.dci.intellij.dbn.execution.compiler.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class CompileActionGroup extends DefaultActionGroup {

    public CompileActionGroup(DBSchemaObject object) {
        super("Compile", true);
        getTemplatePresentation().setIcon(Icons.OBEJCT_COMPILE);
        if (object.getContentType() == DBContentType.CODE_SPEC_AND_BODY) {
            add(new CompileObjectAction(object, DBContentType.CODE_SPEC));
            add(new CompileObjectAction(object, DBContentType.CODE_BODY));
        } else {
            add(new CompileObjectAction(object, DBContentType.CODE));
        }

        addSeparator();
        add(new CompileInvalidObjectsAction(object.getSchema()));
    }
}