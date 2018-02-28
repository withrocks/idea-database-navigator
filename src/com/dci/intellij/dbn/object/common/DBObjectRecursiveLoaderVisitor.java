package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectListVisitor;

import java.util.List;

public class DBObjectRecursiveLoaderVisitor implements DBObjectListVisitor{
    public static final DBObjectRecursiveLoaderVisitor INSTANCE = new DBObjectRecursiveLoaderVisitor();

    private DBObjectRecursiveLoaderVisitor() {
    }

    @Override
    public void visitObjectList(DBObjectList<DBObject> objectList) {
        List<DBObject> objects = objectList.getObjects();

        if (!objectList.getDependencyAdapter().isSubContent()) {
            for (DBObject object : objects) {
                DBObjectListContainer childObjects = object.getChildObjects();
                if (childObjects != null) {
                    childObjects.visitLists(this, false);
                }
            }
        }
    }
}
