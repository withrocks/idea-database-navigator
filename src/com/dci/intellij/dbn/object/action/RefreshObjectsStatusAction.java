package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class RefreshObjectsStatusAction extends AnAction {

    private ConnectionHandler connectionHandler;

    public RefreshObjectsStatusAction(ConnectionHandler connectionHandler) {
        super("Refresh objects status");
        this.connectionHandler = connectionHandler;
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        connectionHandler.getObjectBundle().refreshObjectsStatus(null);
    }
}
