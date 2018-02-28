package com.dci.intellij.dbn.object.dependency.action;

import java.util.List;

import com.dci.intellij.dbn.object.action.NavigateToObjectAction;
import com.dci.intellij.dbn.object.action.ObjectListShowAction;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.actionSystem.AnAction;

public class ReferencedObjectsListShowAction extends ObjectListShowAction {
    public ReferencedObjectsListShowAction(DBSchemaObject object) {
        super("Referenced objects", object);
    }

    public List<DBObject> getObjectList() {
        return ((DBSchemaObject) sourceObject).getReferencedObjects();
    }

    public String getTitle() {
        return "Objects referenced by " + sourceObject.getQualifiedNameWithType();
    }

    public String getEmptyListMessage() {
        return "No referenced objects found for " + sourceObject.getQualifiedNameWithType();
    }


    public String getListName() {
       return "referenced objects";
   }

    @Override
    protected AnAction createObjectAction(DBObject object) {
        return new NavigateToObjectAction(this.sourceObject, object);
    }

}