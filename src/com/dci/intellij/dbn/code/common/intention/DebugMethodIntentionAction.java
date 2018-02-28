package com.dci.intellij.dbn.code.common.intention;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

public class DebugMethodIntentionAction extends AbstractMethodExecutionIntentionAction {

    @Override
    protected String getActionName() {
        return "Debug";
    }

    @Override
    public Icon getIcon(int flags) {
        return Icons.METHOD_EXECUTION_DEBUG;
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        if (psiFile != null) {
            DBMethod method = resolveMethod(editor, psiFile);
            return method != null;
        }
        return false;
    }

    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        DBMethod method = resolveMethod(editor, psiFile);
        if (method != null) {
            DatabaseDebuggerManager executionManager = DatabaseDebuggerManager.getInstance(project);
            executionManager.createDebugConfiguration(method);
        }
    }

    public boolean startInWriteAction() {
        return false;
    }
}
