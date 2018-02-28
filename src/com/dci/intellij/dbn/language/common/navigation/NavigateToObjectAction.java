package com.dci.intellij.dbn.language.common.navigation;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class NavigateToObjectAction extends AnAction {
    private DBObjectRef objectRef;
    public NavigateToObjectAction(DBObject object, DBObjectType objectType) {
        super("Navigate to " + objectType.getName(), null, objectType.getIcon());
        objectRef = DBObjectRef.from(object);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (objectRef != null) {
            DBObject object = objectRef.get();
            if (object != null) {
                object.navigate(true);
            }
        }

    }
}