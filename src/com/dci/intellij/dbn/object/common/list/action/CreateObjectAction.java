package com.dci.intellij.dbn.object.common.list.action;

import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.factory.DatabaseObjectFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class CreateObjectAction extends AnAction {

    private DBObjectList objectList;

    public CreateObjectAction(DBObjectList objectList) {
        super("New " + objectList.getObjectType().getName() + "...");
        this.objectList = objectList;
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        DBSchema schema = (DBSchema) objectList.getParent();
        DatabaseObjectFactory factory =
                DatabaseObjectFactory.getInstance(schema.getProject());
        factory.openFactoryInputDialog(schema, objectList.getObjectType());
    }
}