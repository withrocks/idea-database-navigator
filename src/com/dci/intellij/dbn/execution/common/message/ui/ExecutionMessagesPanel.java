package com.dci.intellij.dbn.execution.common.message.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.common.message.action.CloseMessagesWindowAction;
import com.dci.intellij.dbn.execution.common.message.action.CollapseMessagesTreeAction;
import com.dci.intellij.dbn.execution.common.message.action.ExpandMessagesTreeAction;
import com.dci.intellij.dbn.execution.common.message.action.OpenSettingsAction;
import com.dci.intellij.dbn.execution.common.message.action.ViewExecutedStatementAction;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTree;
import com.dci.intellij.dbn.execution.common.ui.ExecutionConsoleForm;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.IdeBorderFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;

public class ExecutionMessagesPanel extends DBNFormImpl<ExecutionConsoleForm>{
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel statusPanel;
    private JScrollPane messagesScrollPane;
    private JPanel messagesPanel;

    private MessagesTree messagesTree;

    public ExecutionMessagesPanel(ExecutionConsoleForm parentForm) {
        super(parentForm);
        messagesTree = new MessagesTree(getProject());
        messagesScrollPane.setViewportView(messagesTree);
        messagesPanel.setBorder(IdeBorderFactory.createBorder());
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.ExecutionMessages.Controls", false,
                new CloseMessagesWindowAction(messagesTree),
                new ViewExecutedStatementAction(messagesTree),
                new ExpandMessagesTreeAction(messagesTree),
                new CollapseMessagesTreeAction(messagesTree),
                ActionUtil.SEPARATOR,
                new OpenSettingsAction(messagesTree));
        actionsPanel.add(actionToolbar.getComponent());

        Disposer.register(this, messagesTree);
    }

    public TreePath addExecutionMessage(StatementExecutionMessage executionMessage, boolean select, boolean focus) {
        return messagesTree.addExecutionMessage(executionMessage, select, focus);
    }

    public TreePath addCompilerMessage(CompilerMessage compilerMessage, boolean select) {
        return messagesTree.addCompilerMessage(compilerMessage, select);
    }

    public TreePath addExplainPlanMessage(ExplainPlanMessage explainPlanMessage, boolean select) {
        return messagesTree.addExplainPlanMessage(explainPlanMessage, select);
    }

    public void reset() {
        messagesTree.reset();
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void selectMessage(@NotNull CompilerMessage compilerMessage) {
        messagesTree.selectCompilerMessage(compilerMessage);
    }

    public void selectMessage(@NotNull StatementExecutionMessage statementExecutionMessage, boolean focus) {
        messagesTree.selectExecutionMessage(statementExecutionMessage, focus);
    }

    public void expand(TreePath treePath) {
        messagesTree.makeVisible(treePath);
    }

    public TreePath getMessageTreePath(StatementExecutionMessage executionMessage) {
        return null;
    }
}
