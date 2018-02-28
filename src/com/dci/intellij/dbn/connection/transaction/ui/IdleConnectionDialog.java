package com.dci.intellij.dbn.connection.transaction.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DialogWithTimeout;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.intellij.openapi.project.Project;

public class IdleConnectionDialog extends DialogWithTimeout {
    private IdleConnectionDialogForm idleConnectionDialogForm;
    private ConnectionHandler connectionHandler;

    public IdleConnectionDialog(ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject(), "Idle connection", true, TimeUtil.getSeconds(5));
        this.connectionHandler = connectionHandler;
        idleConnectionDialogForm = new IdleConnectionDialogForm(connectionHandler, 5);
        setModal(false);
        init();
    }


    @Override
    protected JComponent createContentComponent() {
        return idleConnectionDialogForm.getComponent();
    }

    @Override
    public void doDefaultAction() {
        rollback();
    }

    @Override
    protected void doOKAction() {
        ConnectionHandler handler = connectionHandler;
        super.doOKAction();
        handler.getConnectionStatus().setResolvingIdleStatus(false);
    }

    @Override
    public void doCancelAction() {
        ping();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                new CommitAction(),
                new RollbackAction(),
                new KeepAliveAction(),
                getHelpAction()
        };
    }

    private class CommitAction extends AbstractAction {
        public CommitAction() {
            super("Commit", Icons.CONNECTION_COMMIT);
        }

        public void actionPerformed(ActionEvent e) {
            commit();
        }
    }

    private class RollbackAction extends AbstractAction {
        public RollbackAction() {
            super("Rollback", Icons.CONNECTION_ROLLBACK);
        }
        public void actionPerformed(ActionEvent e) {
            rollback();
        }

    }
    private class KeepAliveAction extends AbstractAction {
        public KeepAliveAction() {
            super("Keep Alive");
        }

        public void actionPerformed(ActionEvent e) {
            ping();
        }
    }

    private void commit() {
        ConnectionHandler handler = connectionHandler;
        DatabaseTransactionManager transactionManager = getTransactionManager();
        doOKAction();
        transactionManager.execute(handler, true, TransactionAction.COMMIT, TransactionAction.DISCONNECT_IDLE);
    }

    private void rollback() {
        ConnectionHandler handler = connectionHandler;
        DatabaseTransactionManager transactionManager = getTransactionManager();
        doOKAction();
        transactionManager.execute(handler, true, TransactionAction.ROLLBACK_IDLE, TransactionAction.DISCONNECT_IDLE);
    }

    private void ping() {
        ConnectionHandler handler = connectionHandler;
        DatabaseTransactionManager transactionManager = getTransactionManager();
        doOKAction();
        transactionManager.execute(handler, true, TransactionAction.PING);
    }

    private DatabaseTransactionManager getTransactionManager() {
        Project project = getProject();
        return DatabaseTransactionManager.getInstance(project);
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            connectionHandler = null;
        }
    }
}
