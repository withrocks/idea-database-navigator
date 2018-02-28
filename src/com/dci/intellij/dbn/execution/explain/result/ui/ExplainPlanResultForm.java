package com.dci.intellij.dbn.execution.explain.result.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.UIUtil;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ExplainPlanResultForm extends DBNFormImpl implements ExecutionResultForm{
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JScrollPane resultScrollPane;
    private ExplainPlanTreeTable explainPlanTreeTable;
    private JPanel resultPanel;
    private ExplainPlanResult explainPlanResult;

    public ExplainPlanResultForm(final ExplainPlanResult explainPlanResult) {
        super(explainPlanResult.getProject());
        this.explainPlanResult = explainPlanResult;
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", false, "DBNavigator.ActionGroup.ExplainPlanResult");

        actionsPanel.add(actionToolbar.getComponent());

        resultPanel.setBorder(IdeBorderFactory.createBorder());
        ExplainPlanTreeTableModel treeTableModel = new ExplainPlanTreeTableModel(explainPlanResult);
        explainPlanTreeTable = new ExplainPlanTreeTable(treeTableModel);

        resultScrollPane.setViewportView(explainPlanTreeTable);
        resultScrollPane.getViewport().setBackground(explainPlanTreeTable.getBackground());

        JPanel panel = new JPanel();
        panel.setBorder(UIUtil.getTableHeaderCellBorder());

        Disposer.register(this, explainPlanResult);
        ActionUtil.registerDataProvider(explainPlanTreeTable, explainPlanResult.getDataProvider(), false);
    }

    public ExplainPlanTreeTable getExplainPlanTreeTable() {
        return explainPlanTreeTable;
    }

    public void dispose() {
        super.dispose();
        explainPlanResult = null;
    }

    public void show() {
        Project project = explainPlanResult.getProject();
        ExecutionManager.getInstance(project).selectResultTab(explainPlanResult);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    public void setExecutionResult(ExecutionResult executionResult) {
    }

    @Override
    public ExecutionResult getExecutionResult() {
        return null;
    }

    public void collapseAllNodes() {
        TreeUtil.collapseAll(explainPlanTreeTable.getTree());    
    }

    public void expandAllNodes() {
        TreeUtil.expandAll(explainPlanTreeTable.getTree());
    }
}
