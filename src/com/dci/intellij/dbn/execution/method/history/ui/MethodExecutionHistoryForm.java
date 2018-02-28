package com.dci.intellij.dbn.execution.method.history.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionForm;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionHistory;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.JBSplitter;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodExecutionHistoryForm extends DBNFormImpl<MethodExecutionHistoryDialog> {
    private JPanel mainPanel;
    private JTree executionInputsTree;
    private JPanel actionsPanel;
    private JPanel argumentsPanel;
    private JPanel contentPanel;
    private MethodExecutionHistory executionHistory;
    private ChangeListener changeListener;

    private Map<MethodExecutionInput, MethodExecutionForm> methodExecutionForms;

    public MethodExecutionHistoryForm(MethodExecutionHistoryDialog parentComponent, MethodExecutionHistory executionHistory) {
        super(parentComponent);
        this.executionHistory = executionHistory;
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true,
                new ShowGroupedTreeAction(),
                new DeleteHistoryEntryAction(),
                ActionUtil.SEPARATOR,
                new OpenSettingsAction());
        actionsPanel.add(actionToolbar.getComponent());
        methodExecutionForms = new HashMap<MethodExecutionInput, MethodExecutionForm>();
        mainPanel.setBorder(Borders.BOTTOM_LINE_BORDER);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(contentPanel);
        JBSplitter splitter = (JBSplitter) contentPanel.getComponent(0);
        splitter.setProportion((float) 0.32);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public List<MethodExecutionInput> getExecutionInputs() {
        MethodExecutionHistoryTreeModel model = (MethodExecutionHistoryTreeModel) executionInputsTree.getModel();
        return model.getExecutionInputs();
    }

    private void createUIComponents() {
        boolean group = executionHistory.isGroupEntries();
        executionInputsTree = new MethodExecutionHistoryTree(getParentComponent(), executionHistory, group);
        Disposer.register(this, (Disposable) executionInputsTree);
    }

    public MethodExecutionHistoryTree getTree() {
        return (MethodExecutionHistoryTree) executionInputsTree;
    }

    public void dispose() {
        super.dispose();
        executionInputsTree = null;
        executionHistory = null;
    }

    public void showMethodExecutionPanel(MethodExecutionInput executionInput) {
        argumentsPanel.removeAll();
        if (executionInput != null && !executionInput.isObsolete()) {
            MethodExecutionForm methodExecutionForm = methodExecutionForms.get(executionInput);
            if (methodExecutionForm == null) {
                methodExecutionForm = new MethodExecutionForm(this, executionInput, true, false);
                methodExecutionForm.addChangeListener(getChangeListener());
                methodExecutionForms.put(executionInput, methodExecutionForm);
            }
            argumentsPanel.add(methodExecutionForm.getComponent(), BorderLayout.CENTER);
        }
        argumentsPanel.revalidate();
        argumentsPanel.repaint();
    }

    private ChangeListener getChangeListener() {
        if (changeListener == null) {
            changeListener = new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    getParentComponent().setSaveButtonEnabled(true);
                }
            };
        }
        return changeListener;
    }

    public void updateMethodExecutionInputs() {
        for (MethodExecutionForm methodExecutionComponent : methodExecutionForms.values()) {
            methodExecutionComponent.updateExecutionInput();
        }
    }

    public void setSelectedInput(MethodExecutionInput selectedExecutionInput) {
        getTree().setSelectedInput(selectedExecutionInput);
    }

    public class DeleteHistoryEntryAction extends DumbAwareAction {
        public DeleteHistoryEntryAction() {
            super("Delete", null, Icons.ACTION_REMOVE);
        }

        public void actionPerformed(AnActionEvent e) {
            getTree().removeSelectedEntries();
        }

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setEnabled(!getTree().isSelectionEmpty());
            e.getPresentation().setVisible(getParentComponent().isEditable());
        }
    }

    public static class OpenSettingsAction extends DumbAwareAction {
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = ActionUtil.getProject(e);
            if (project != null) {
                ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
                settingsManager.openProjectSettings(ConfigId.EXECUTION_ENGINE);
            }
        }

        public void update(@NotNull AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setIcon(Icons.ACTION_SETTINGS);
            presentation.setText("Settings");
        }
    }

    public class ShowGroupedTreeAction extends ToggleAction {
        public ShowGroupedTreeAction() {
            super("Group by Program", "Show grouped by program", Icons.ACTION_GROUP);
        }

        @Override
        public boolean isSelected(AnActionEvent e) {
            return getTree().isGrouped();
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            getTemplatePresentation().setText(state ? "Ungroup" : "Group by Program");
            getTree().showGrouped(state);
            Project project = ActionUtil.getProject(e);
            MethodExecutionManager.getInstance(project).getExecutionHistory().setGroupEntries(state);

        }
    }
}
