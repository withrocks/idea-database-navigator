package com.dci.intellij.dbn.code.common.intention;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.intellij.codeInsight.intention.LowPriorityAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

public class DatabaseConnectIntentionAction extends GenericIntentionAction implements LowPriorityAction{
    @NotNull
    public String getText() {
        return "Connect to database";
    }

    @NotNull
    public String getFamilyName() {
        return "Connection intentions";
    }

    @Override
    public Icon getIcon(int flags) {
        return Icons.CONNECTION_ACTIVE;
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        if (psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguagePsiFile = (DBLanguagePsiFile) psiFile;
            ConnectionHandler activeConnection = dbLanguagePsiFile.getActiveConnection();
            if (activeConnection != null && !activeConnection.isDisposed() && !activeConnection.isVirtual() && !activeConnection.canConnect() && !activeConnection.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public void invoke(@NotNull final Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        if (psiFile instanceof DBLanguagePsiFile) {
            DBLanguagePsiFile dbLanguagePsiFile = (DBLanguagePsiFile) psiFile;
            final ConnectionHandler activeConnection = dbLanguagePsiFile.getActiveConnection();
            if (activeConnection != null && !activeConnection.isDisposed() && !activeConnection.isVirtual()) {
                activeConnection.setAllowConnection(true);
                new BackgroundTask(project, "Trying to connect to " + activeConnection.getName(), false) {
                    @Override
                    public void execute(@NotNull ProgressIndicator progressIndicator) {
                        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                        connectionManager.testConnection(activeConnection, false, true);
                    }
                }.start();
            }
        }
    }

    public boolean startInWriteAction() {
        return false;
    }
}
