package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class RefreshActionGroup  extends DefaultActionGroup {
    public RefreshActionGroup(DBObject object) {
        super("Refresh", true);
        getTemplatePresentation().setIcon(Icons.ACTION_REFRESH);
        DBObjectList objectList = (DBObjectList) object.getTreeParent();
        add(new ReloadObjectsAction(objectList));
        if (object instanceof DBSchemaObject && DatabaseFeature.OBJECT_INVALIDATION.isSupported(object)) {
            add(new RefreshObjectsStatusAction(object.getConnectionHandler()));
        }
    }
}
