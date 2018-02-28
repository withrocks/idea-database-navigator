package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.method.result.ui.MethodExecutionResultForm;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class EditMethodAction extends MethodExecutionResultAction {
    public EditMethodAction(MethodExecutionResultForm executionResultForm) {
        super(executionResultForm, "Edit method", Icons.OBEJCT_EDIT_SOURCE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        MethodExecutionResult executionResult = getExecutionResult();
        if (executionResult != null) {
            DatabaseFileSystem.getInstance().openEditor(executionResult.getMethod(), null, true, true);
        }
    }
}