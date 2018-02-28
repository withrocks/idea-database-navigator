package com.dci.intellij.dbn.connection.transaction.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.intellij.openapi.project.Project;

public class UncommittedChangesDialog extends DBNDialog<UncommittedChangesForm> {
    private ConnectionHandler connectionHandler;
    private TransactionAction additionalOperation;

    public UncommittedChangesDialog(ConnectionHandler connectionHandler, TransactionAction additionalOperation, boolean showActions) {
        super(connectionHandler.getProject(), "Uncommitted Changes", true);
        this.connectionHandler = connectionHandler;
        this.additionalOperation = additionalOperation;
        component = new UncommittedChangesForm(connectionHandler, additionalOperation, showActions);
        setModal(false);
        setResizable(true);
        init();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                new CommitAction(),
                new RollbackAction(),
                getCancelAction(),
                getHelpAction()
        };
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    private class CommitAction extends AbstractAction {
        public CommitAction() {
            super("Commit", Icons.CONNECTION_COMMIT);
        }

        public void actionPerformed(ActionEvent e) {
            ConnectionHandler commitConnectionHandler = connectionHandler;
            DatabaseTransactionManager transactionManager = getTransactionManager();
            doOKAction();
            transactionManager.execute(commitConnectionHandler, true, TransactionAction.COMMIT, additionalOperation);
        }
    }

    private class RollbackAction extends AbstractAction {
        public RollbackAction() {
            super("Rollback", Icons.CONNECTION_ROLLBACK);
        }

        public void actionPerformed(ActionEvent e) {
            ConnectionHandler commitConnectionHandler = connectionHandler;
            DatabaseTransactionManager transactionManager = getTransactionManager();
            doOKAction();
            transactionManager.execute(commitConnectionHandler, true, TransactionAction.ROLLBACK, additionalOperation);
        }
    }

    private DatabaseTransactionManager getTransactionManager() {
        Project project = connectionHandler.getProject();
        return DatabaseTransactionManager.getInstance(project);
    }


    @Override
    public void dispose() {
        super.dispose();
        connectionHandler = null;
    }
}
