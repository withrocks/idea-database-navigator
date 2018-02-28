package com.dci.intellij.dbn.object.common.list.action;

import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class ObjectListActionGroup extends DefaultActionGroup {

    public ObjectListActionGroup(DBObjectList objectList) {
        add(new ReloadObjectsAction(objectList));
        if(objectList.getParent() instanceof DBSchema) {
            add (new CreateObjectAction(objectList));
        }
    }
}