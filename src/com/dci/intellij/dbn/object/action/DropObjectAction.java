package com.dci.intellij.dbn.object.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.factory.DatabaseObjectFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class DropObjectAction extends AnAction {
    private DBSchemaObject object;

    public DropObjectAction(DBSchemaObject object) {
        super("Drop...", null, Icons.ACTION_DELETE);
        this.object = object;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DatabaseObjectFactory.getInstance(object.getProject()).dropObject(object);
    }
}