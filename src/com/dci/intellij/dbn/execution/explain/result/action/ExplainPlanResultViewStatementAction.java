package com.dci.intellij.dbn.execution.explain.result.action;

import java.awt.Component;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.common.ui.StatementViewerPopup;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ExplainPlanResultViewStatementAction extends AbstractExplainPlanResultAction {
    public ExplainPlanResultViewStatementAction() {
        super("View SQL statement", Icons.EXEC_RESULT_VIEW_STATEMENT);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ExplainPlanResult explainPlanResult = getExplainPlanResult(e);
        if (explainPlanResult != null) {
            StatementViewerPopup statementViewer = new StatementViewerPopup(explainPlanResult);
            statementViewer.show((Component) e.getInputEvent().getSource());
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setText("View SQL Statement");
    }
}
