package com.dci.intellij.dbn.connection.console.ui;

import javax.swing.Action;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;

public class CreateRenameConsoleDialog extends DBNDialog<CreateRenameConsoleForm> {
    public CreateRenameConsoleDialog(ConnectionHandler connectionHandler, DBConsoleVirtualFile console) {
        super(connectionHandler.getProject(), console == null ? "Create SQL Console" : "Rename SQL Console", true);
        component = new CreateRenameConsoleForm(this, connectionHandler, console);
        getOKAction().putValue(Action.NAME, console == null ? "Create" : "Rename");
        init();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
            getOKAction(),
            getCancelAction()
        };
    }

    @Override
    protected void doOKAction() {
        DatabaseConsoleManager databaseConsoleManager = DatabaseConsoleManager.getInstance(getProject());
        DBConsoleVirtualFile console = component.getConsole();
        if (console == null) {
            databaseConsoleManager.createConsole(
                    component.getConnectionHandler(),
                    component.getConsoleName());
        } else {
            databaseConsoleManager.renameConsole(console, component.getConsoleName());
        }
        super.doOKAction();
    }

    @NotNull
    public Action getOKAction() {
        return super.getOKAction();
    }


    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return component.getConsoleNameTextField();
    }
}
