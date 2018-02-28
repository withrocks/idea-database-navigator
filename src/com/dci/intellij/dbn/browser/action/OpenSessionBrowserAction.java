package com.dci.intellij.dbn.browser.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.session.SessionBrowserManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class OpenSessionBrowserAction extends AnAction {
    public OpenSessionBrowserAction() {
        super("Open Session Browser", "", Icons.FILE_SESSION_BROWSER);
    }

    private static ConnectionHandler getConnectionHandler(@NotNull AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
            return browserManager.getActiveConnection();
        }
        return null;
    }

    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        ConnectionHandler connectionHandler = getConnectionHandler(e);
        if (connectionHandler != null) {
            presentation.setEnabled(true);
            DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
            presentation.setVisible(compatibilityInterface.supportsFeature(DatabaseFeature.SESSION_BROWSING));
        } else {
            presentation.setVisible(false);
            presentation.setEnabled(false);
        }
        presentation.setText("Open Session Browser");
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        ConnectionHandler connectionHandler = getConnectionHandler(e);
        if (project != null && connectionHandler != null) {
            SessionBrowserManager sessionBrowserManager = SessionBrowserManager.getInstance(project);
            sessionBrowserManager.openSessionBrowser(connectionHandler);
        }

    }

}
