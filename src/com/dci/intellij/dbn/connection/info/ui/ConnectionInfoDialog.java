package com.dci.intellij.dbn.connection.info.ui;

import javax.swing.Action;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;

public class ConnectionInfoDialog extends DBNDialog<ConnectionInfoForm> {
    public ConnectionInfoDialog(ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject(), "Connection Information", true);
        component = new ConnectionInfoForm(this, connectionHandler, true);
        getCancelAction().putValue(Action.NAME, "Close");
        init();
    }

/*
    protected String getDimensionServiceKey() {
        return "DBNavigator.ConnectionIngfo";
    }      
*/

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
            getCancelAction()
        };
    }
}
