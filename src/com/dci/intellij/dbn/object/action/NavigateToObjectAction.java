package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class NavigateToObjectAction extends AnAction {
    private DBObject object;

    public NavigateToObjectAction(DBObject object) {
        super(NamingUtil.enhanceUnderscoresForDisplay(object.getName()), null, object.getIcon());
        this.object = object;
    }

    public NavigateToObjectAction(DBObject sourceObject, DBObject object) {
        super(NamingUtil.enhanceUnderscoresForDisplay(
                    sourceObject != object.getParentObject() ?
                            object.getQualifiedName() :
                            object.getName()),
                object.getTypeName(),
                object.getIcon(0));
        this.object = object;
    }

    public void actionPerformed(AnActionEvent event) {
        object.navigate(true);
    }
}
