package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.options.action.OpenSettingsDialogAction;
import com.intellij.openapi.actionSystem.Anchor;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;

public class SelectConnectionActionGroup extends DefaultActionGroup {
    private static SelectConnectionAction NO_CONNECTION = new SelectConnectionAction(null);

    public SelectConnectionActionGroup(Project project) {
        add(new OpenSettingsDialogAction(), new Constraints(Anchor.FIRST, null));
        addSeparator();
        add(NO_CONNECTION);

        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();

        for (ConnectionHandler virtualConnectionHandler : connectionBundle.getVirtualConnections()) {
            SelectConnectionAction connectionAction = new SelectConnectionAction(virtualConnectionHandler);
            add(connectionAction);
        }

        if (connectionBundle.getConnectionHandlers().size() > 0) {
            addSeparator();
            for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
                SelectConnectionAction connectionAction = new SelectConnectionAction(connectionHandler);
                add(connectionAction);
            }
        }
    }
}
