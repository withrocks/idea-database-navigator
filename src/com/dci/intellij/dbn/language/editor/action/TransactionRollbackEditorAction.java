package com.dci.intellij.dbn.language.editor.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class TransactionRollbackEditorAction extends TransactionEditorAction {
    public TransactionRollbackEditorAction() {
        super("Rollback", "Rollback changes", Icons.CONNECTION_ROLLBACK);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (project != null) {
            DatabaseTransactionManager transactionManager = DatabaseTransactionManager.getInstance(project);
            ConnectionHandler activeConnection = getConnectionHandler(project, virtualFile);
            transactionManager.rollback(activeConnection, true, false);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setText("Rollback");

        Project project = ActionUtil.getProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);

        ConnectionHandler connectionHandler = virtualFile == null ? null : getConnectionHandler(project, virtualFile);
        e.getPresentation().setVisible(connectionHandler != null && !connectionHandler.isAutoCommit());
    }

}
