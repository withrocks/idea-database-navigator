package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.transaction.action.ShowUncommittedChangesAction;
import com.dci.intellij.dbn.connection.transaction.action.ToggleAutoCommitAction;
import com.dci.intellij.dbn.connection.transaction.action.ToggleAutoConnectAction;
import com.dci.intellij.dbn.connection.transaction.action.ToggleDatabaseLoggingAction;
import com.dci.intellij.dbn.connection.transaction.action.TransactionCommitAction;
import com.dci.intellij.dbn.connection.transaction.action.TransactionRollbackAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class ConnectionActionGroup extends DefaultActionGroup {

    public ConnectionActionGroup(ConnectionHandler connectionHandler) {
        //add(new ConnectAction(connectionHandler));
        add(new TransactionCommitAction(connectionHandler));
        add(new TransactionRollbackAction(connectionHandler));
        add(new ToggleAutoCommitAction(connectionHandler));
        add(new ToggleDatabaseLoggingAction(connectionHandler));
        addSeparator();
        add(new OpenSQLConsoleAction(connectionHandler));
        add(new ShowUncommittedChangesAction(connectionHandler));
        addSeparator();
        add(new ToggleAutoConnectAction(connectionHandler));
        add(new ConnectAction(connectionHandler));
        add(new DisconnectAction(connectionHandler));
        add(new TestConnectivityAction(connectionHandler));
        add(new LoadAllObjectsAction(connectionHandler));
        addSeparator();
        add(new OpenConnectionSettingsAction(connectionHandler));
        add(new ShowDatabaseInformationAction(connectionHandler));
    }
}
