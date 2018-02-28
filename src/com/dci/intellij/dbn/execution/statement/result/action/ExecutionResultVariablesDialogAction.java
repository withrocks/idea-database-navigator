package com.dci.intellij.dbn.execution.statement.result.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionCursorProcessor;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

public class ExecutionResultVariablesDialogAction extends AbstractExecutionResultAction {
    public ExecutionResultVariablesDialogAction() {
        super("Open variables dialog", Icons.EXEC_RESULT_OPEN_EXEC_DIALOG);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            final StatementExecutionCursorProcessor executionProcessor = executionResult.getExecutionProcessor();
            final Project project = executionResult.getProject();
            StatementExecutionManager statementExecutionManager = StatementExecutionManager.getInstance(project);
            statementExecutionManager.promptVariablesDialog(executionProcessor,
                    new BackgroundTask(project, "Executing " + executionResult.getExecutionProcessor().getStatementName(), false, true) {
                        @Override
                        protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                            executionProcessor.execute(progressIndicator);
                        }
                    });
        }
    }

    @Override
    public void update(AnActionEvent e) {
        boolean visible = false;
        StatementExecutionCursorResult executionResult = getExecutionResult(e);
        if (executionResult != null) {
            StatementExecutionCursorProcessor executionProcessor = executionResult.getExecutionProcessor();
            if (executionProcessor != null) {
                StatementExecutionVariablesBundle executionVariables = executionProcessor.getExecutionVariables();
                visible = executionVariables != null && executionVariables.getVariables().size() > 0;
            }
        }
        e.getPresentation().setVisible(visible);
        e.getPresentation().setText("Open Variables Dialog");
    }
}
