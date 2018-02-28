package com.dci.intellij.dbn.connection.transaction.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;

public class ToggleAutoCommitAction extends ToggleAction {
    private ConnectionHandler connectionHandler;

    public ToggleAutoCommitAction(ConnectionHandler connectionHandler) {
        super("Auto-Commit");
        this.connectionHandler = connectionHandler;

    }
    @Override
    public boolean isSelected(AnActionEvent e) {
        return connectionHandler.isAutoCommit();
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(connectionHandler.getProject());
        transactionManager.toggleAutoCommit(connectionHandler);
    }
}
