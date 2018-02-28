package com.dci.intellij.dbn.editor.session.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.dci.intellij.dbn.editor.session.ui.table.SessionBrowserTable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class DisconnectSessionsAction extends AbstractSessionBrowserAction {

    public DisconnectSessionsAction() {
        super("Disconnect Sessions", Icons.ACTION_DISCONNECT_SESSION);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        SessionBrowser sessionBrowser = getSessionBrowser(e);
        if (sessionBrowser != null) {
            sessionBrowser.disconnectSelectedSessions();
        }
    }

    public void update(AnActionEvent e) {
        SessionBrowser sessionBrowser = getSessionBrowser(e);
        boolean visible = false;
        boolean enabled = false;
        if (sessionBrowser != null) {
            ConnectionHandler connectionHandler = FailsafeUtil.get(sessionBrowser.getConnectionHandler());
            DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
            visible = compatibilityInterface.supportsFeature(DatabaseFeature.SESSION_DISCONNECT);
            SessionBrowserTable editorTable = sessionBrowser.getEditorTable();
            if (editorTable != null) {
                enabled = editorTable.getSelectedRows().length > 0;
            }
        }

        Presentation presentation = e.getPresentation();
        presentation.setText("Disconnect Sessions");
        presentation.setVisible(visible);
        presentation.setEnabled(enabled);
    }
}