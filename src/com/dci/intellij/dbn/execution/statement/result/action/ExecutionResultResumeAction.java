package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class ExecutionResultResumeAction extends AbstractExecutionResultAction {
    public ExecutionResultResumeAction() {
        super("Fetch next records", Icons.EXEC_RESULT_RESUME);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            executionResult.fetchNextRecords();
        }

    }

    @Override
    public void update(AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);

        boolean enabled = executionResult != null &&
                executionResult.hasResult() &&
                executionResult.getResultTable() != null &&
                !executionResult.getResultTable().isLoading() &&
                !executionResult.getTableModel().isResultSetExhausted();

        Presentation presentation = e.getPresentation();
        presentation.setEnabled(enabled);
        presentation.setText("Fetch Next Records");
    }
}
