package com.dci.intellij.dbn.navigation.action;

import com.intellij.openapi.actionSystem.AnActionEvent;

public class LookupDatabaseObjectAction extends GoToDatabaseObjectAction{
    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        event.getPresentation().setText("Lookup Database Object...");
    }
}
