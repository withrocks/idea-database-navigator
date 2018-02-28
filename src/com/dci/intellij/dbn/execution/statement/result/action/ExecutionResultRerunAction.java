package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class ExecutionResultRerunAction extends AbstractExecutionResultAction {
    public ExecutionResultRerunAction() {
        super("Rerun statement", Icons.EXEC_RESULT_RERUN);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            executionResult.reload();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(
                executionResult != null &&
                executionResult.getResultTable() != null &&
                !executionResult.getResultTable().isLoading());
        
        presentation.setText("Rerun Statement");
    }
}
