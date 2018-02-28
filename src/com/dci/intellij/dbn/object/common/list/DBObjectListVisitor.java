package com.dci.intellij.dbn.object.common.list;

import com.dci.intellij.dbn.object.common.DBObject;

public interface DBObjectListVisitor {
    public void visitObjectList(DBObjectList<DBObject> objectList);
}
