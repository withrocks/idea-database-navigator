package com.dci.intellij.dbn.connection.transaction.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;

public class ToggleAutoConnectAction extends ToggleAction {
    private ConnectionHandler connectionHandler;

    public ToggleAutoConnectAction(ConnectionHandler connectionHandler) {
        super("Connect Automatically");
        this.connectionHandler = connectionHandler;

    }
    @Override
    public boolean isSelected(AnActionEvent e) {
        return connectionHandler.getSettings().getDetailSettings().isConnectAutomatically();
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        connectionHandler.getSettings().getDetailSettings().setConnectAutomatically(state);
    }
}
