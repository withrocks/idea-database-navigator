package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.method.result.ui.MethodExecutionResultForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class PromptMethodExecutionAction extends MethodExecutionResultAction {
    public PromptMethodExecutionAction(MethodExecutionResultForm executionResultForm) {
        super(executionResultForm, "Open execution dialog", Icons.METHOD_EXECUTION_DIALOG);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
        MethodExecutionResult executionResult = getExecutionResult();
        if (executionResult != null) {
            MethodExecutionInput executionInput = executionResult.getExecutionInput();
            if (executionManager.promptExecutionDialog(executionInput, false)) {
                executionManager.execute(executionInput);
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        MethodExecutionResult executionResult = getExecutionResult();
        e.getPresentation().setEnabled(
                executionResult != null &&
                !executionResult.isDebug() &&
                !executionResult.getExecutionInput().isExecuting());
    }    
}