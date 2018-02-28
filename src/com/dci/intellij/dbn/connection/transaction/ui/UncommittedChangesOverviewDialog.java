package com.dci.intellij.dbn.connection.transaction.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.dci.intellij.dbn.connection.transaction.TransactionAction;
import com.dci.intellij.dbn.connection.transaction.TransactionListener;
import com.intellij.openapi.project.Project;

public class UncommittedChangesOverviewDialog extends DBNDialog<UncommittedChangesOverviewForm> {
    private TransactionAction additionalOperation;

    public UncommittedChangesOverviewDialog(Project project, TransactionAction additionalOperation) {
        super(project, "Uncommitted changes overview", true);
        this.additionalOperation = additionalOperation;
        component = new UncommittedChangesOverviewForm(this);
        setModal(false);
        setResizable(true);
        init();
        EventManager.subscribe(project, TransactionListener.TOPIC, transactionListener);
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                commitAllAction,
                rollbackAllAction,
                getCancelAction(),
                getHelpAction()
        };
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    private AbstractAction commitAllAction = new AbstractAction("Commit all", Icons.CONNECTION_COMMIT) {
        public void actionPerformed(ActionEvent e) {
            DatabaseTransactionManager transactionManager = getTransactionManager();
            List<ConnectionHandler> connectionHandlers = component.getConnectionHandlers();

            doOKAction();
            for (ConnectionHandler connectionHandler : connectionHandlers) {
                transactionManager.execute(connectionHandler, true, TransactionAction.COMMIT, additionalOperation);
            }
        }

        @Override
        public boolean isEnabled() {
            return component.hasUncommittedChanges();
        }
    };

    private AbstractAction rollbackAllAction = new AbstractAction("Rollback all", Icons.CONNECTION_ROLLBACK) {
        public void actionPerformed(ActionEvent e) {
            DatabaseTransactionManager transactionManager = getTransactionManager();
            List<ConnectionHandler> connectionHandlers = new ArrayList<ConnectionHandler>(component.getConnectionHandlers());

            doOKAction();
            for (ConnectionHandler connectionHandler : connectionHandlers) {
                transactionManager.execute(connectionHandler, true, TransactionAction.ROLLBACK, additionalOperation);
            }
        }

        @Override
        public boolean isEnabled() {
            return component.hasUncommittedChanges();
        }
    };

    private DatabaseTransactionManager getTransactionManager() {
        return DatabaseTransactionManager.getInstance(getProject());
    }

    TransactionListener transactionListener = new TransactionListener() {
        @Override
        public void beforeAction(ConnectionHandler connectionHandler, TransactionAction action) {

        }

        @Override
        public void afterAction(ConnectionHandler connectionHandler, TransactionAction action, boolean succeeded) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
            if (!connectionManager.hasUncommittedChanges()) {
                new ConditionalLaterInvocator() {
                    @Override
                    public void execute() {
                        getCancelAction().putValue(Action.NAME, "Close");
                        commitAllAction.setEnabled(false);
                        rollbackAllAction.setEnabled(false);

                    }
                }.start();
            }
        }
    };

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            EventManager.unsubscribe(transactionListener);
            transactionListener = null;
        }
    }
}
