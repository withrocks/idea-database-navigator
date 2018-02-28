package com.dci.intellij.dbn.execution.common.message.action;

import java.awt.Component;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTree;
import com.dci.intellij.dbn.execution.common.message.ui.tree.StatementExecutionMessageNode;
import com.dci.intellij.dbn.execution.common.ui.StatementViewerPopup;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class ViewExecutedStatementAction extends ExecutionMessagesAction {
    public ViewExecutedStatementAction(MessagesTree messagesTree) {
        super(messagesTree, "View SQL statement", Icons.EXEC_RESULT_VIEW_STATEMENT);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        getMessagesTree().grabFocus();
        StatementExecutionMessageNode execMessageNode = (StatementExecutionMessageNode) getMessagesTree().getSelectionPath().getLastPathComponent();
        StatementExecutionResult executionResult = execMessageNode.getExecutionMessage().getExecutionResult();
        StatementViewerPopup statementViewer = new StatementViewerPopup(executionResult);
        statementViewer.show((Component) e.getInputEvent().getSource());
    }

    public void update(@NotNull AnActionEvent e) {
        boolean enabled =
                getMessagesTree().getSelectionPath() != null &&
                 getMessagesTree().getSelectionPath().getLastPathComponent() instanceof StatementExecutionMessageNode;
        Presentation presentation = e.getPresentation();
        presentation.setEnabled(enabled);
    }
}