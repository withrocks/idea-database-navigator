package com.dci.intellij.dbn.execution.statement.result.action;

import java.awt.Component;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.common.ui.StatementViewerPopup;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ExecutionResultViewStatementAction extends AbstractExecutionResultAction {
    public ExecutionResultViewStatementAction() {
        super("View SQL statement", Icons.EXEC_RESULT_VIEW_STATEMENT);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            StatementViewerPopup statementViewer = new StatementViewerPopup(executionResult);
            statementViewer.show((Component) e.getInputEvent().getSource());
        }
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setText("View SQL Statement");
    }
}
