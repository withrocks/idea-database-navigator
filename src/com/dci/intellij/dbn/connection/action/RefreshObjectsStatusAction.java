package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class RefreshObjectsStatusAction extends DumbAwareAction {

    private ConnectionHandler connectionHandler;

    public RefreshObjectsStatusAction(ConnectionHandler connectionHandler) {
        super("Refresh objects status", "", Icons.ACTION_REFRESH);
        this.connectionHandler = connectionHandler;
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        connectionHandler.getObjectBundle().refreshObjectsStatus(null);
    }
}
