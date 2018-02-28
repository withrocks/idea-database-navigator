package com.dci.intellij.dbn.connection.transaction.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class TransactionRollbackAction extends DumbAwareAction {
    private ConnectionHandler connectionHandler;

    public TransactionRollbackAction(ConnectionHandler connectionHandler) {
        super("Rollback", "Rollback connection", Icons.CONNECTION_ROLLBACK);
        this.connectionHandler = connectionHandler;

    }

    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
            transactionManager.rollback(connectionHandler, false, false);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabled(connectionHandler.hasUncommittedChanges());
        e.getPresentation().setVisible(!connectionHandler.isAutoCommit());
    }
}
