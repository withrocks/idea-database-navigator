package com.dci.intellij.dbn.execution.method.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.method.result.ui.MethodExecutionResultForm;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class StartMethodExecutionAction extends MethodExecutionResultAction {
    public StartMethodExecutionAction(MethodExecutionResultForm executionResultFOrm) {
        super(executionResultFOrm, "Execute again", Icons.METHOD_EXECUTION_RERUN);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        MethodExecutionResult executionResult = getExecutionResult();
        if (executionResult != null) {
            MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
            executionManager.execute(executionResult.getExecutionInput());
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