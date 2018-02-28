package com.dci.intellij.dbn.execution.method.result.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dci.intellij.dbn.execution.logging.ui.DatabaseLogOutputConsole;
import com.dci.intellij.dbn.execution.method.ArgumentValue;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.method.result.action.CloseExecutionResultAction;
import com.dci.intellij.dbn.execution.method.result.action.EditMethodAction;
import com.dci.intellij.dbn.execution.method.result.action.OpenSettingsAction;
import com.dci.intellij.dbn.execution.method.result.action.PromptMethodExecutionAction;
import com.dci.intellij.dbn.execution.method.result.action.StartMethodExecutionAction;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import java.awt.BorderLayout;
import java.util.List;

public class MethodExecutionResultForm extends DBNFormImpl implements ExecutionResultForm<MethodExecutionResult> {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel statusPanel;
    private JLabel connectionLabel;
    private JLabel durationLabel;
    private JPanel outputCursorsPanel;
    private JTree argumentValuesTree;
    private JPanel argumentValuesPanel;
    private JPanel executionResultPanel;
    private TabbedPane outputTabs;


    private MethodExecutionResult executionResult;

    public MethodExecutionResultForm(Project project, MethodExecutionResult executionResult) {
        super(project);
        this.executionResult = executionResult;
        outputTabs = new TabbedPane(this);
        createActionsPanel();
        updateOutputTabs();

        outputCursorsPanel.add(outputTabs, BorderLayout.CENTER);

        argumentValuesPanel.setBorder(IdeBorderFactory.createBorder());
        updateStatusBarLabels();
        executionResultPanel.setSize(800, -1);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        TreeUtil.expand(argumentValuesTree, 2);

        Disposer.register(this, outputTabs);
        Disposer.register(this, executionResult);
    }

    public void setExecutionResult(MethodExecutionResult executionResult) {
        if (this.executionResult != executionResult) {
            MethodExecutionResult oldExecutionResult = this.executionResult;
            this.executionResult = executionResult;
            rebuild();

            DisposerUtil.dispose(oldExecutionResult);
        }
    }

    public MethodExecutionResult getExecutionResult() {
        return executionResult;
    }

    public DBMethod getMethod() {
        return executionResult.getMethod();
    }

    public void rebuild() {
        new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                updateArgumentValueTree();
                updateOutputTabs();
                updateStatusBarLabels();
            }
        }.start();
    }

    private void updateArgumentValueTree() {
        List<ArgumentValue> inputArgumentValues = executionResult.getExecutionInput().getInputArgumentValues();
        List<ArgumentValue> outputArgumentValues = executionResult.getArgumentValues();

        DBMethod method = executionResult.getMethod();
        ArgumentValuesTreeModel treeModel = new ArgumentValuesTreeModel(method, inputArgumentValues, outputArgumentValues);
        argumentValuesTree.setModel(treeModel);
        TreeUtil.expand(argumentValuesTree, 2);
    }

    private void updateOutputTabs() {
        outputTabs.removeAllTabs();
        String logOutput = executionResult.getLogOutput();
        String logConsoleName = "Output";
        ConnectionHandler connectionHandler = executionResult.getConnectionHandler();
        if (connectionHandler != null) {
            DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
            String databaseLogName = compatibilityInterface.getDatabaseLogName();
            if (databaseLogName != null) {
                logConsoleName = databaseLogName;
            }

            DatabaseLogOutputConsole outputConsole = new DatabaseLogOutputConsole(connectionHandler, logConsoleName, true);
            outputConsole.writeToConsole(logOutput);
            Disposer.register(this, outputConsole);

            TabInfo outputTabInfo = new TabInfo(outputConsole.getComponent());
            outputTabInfo.setText(outputConsole.getTitle());
            outputTabInfo.setIcon(Icons.EXEC_LOG_OUTPUT_CONSOLE);
            outputTabInfo.setObject(outputConsole);
            outputTabs.addTab(outputTabInfo);

            boolean isFirst = true;
            for (ArgumentValue argumentValue : executionResult.getArgumentValues()) {
                if (argumentValue.isCursor()) {
                    DBArgument argument = argumentValue.getArgument();

                    MethodExecutionCursorResultForm cursorResultForm =
                            new MethodExecutionCursorResultForm(this, executionResult, argument);

                    TabInfo tabInfo = new TabInfo(cursorResultForm.getComponent());
                    tabInfo.setText(argument.getName());
                    tabInfo.setIcon(argument.getIcon());
                    tabInfo.setObject(cursorResultForm);
                    outputTabs.addTab(tabInfo);
                    if (isFirst) {
                        outputTabs.select(tabInfo, false);
                        isFirst = false;
                    }
                } else if (argumentValue.isLargeObject()) {
                    DBArgument argument = argumentValue.getArgument();

                    MethodExecutionLargeValueResultForm largeValueResultForm =
                            new MethodExecutionLargeValueResultForm(this, executionResult, argument);

                    TabInfo tabInfo = new TabInfo(largeValueResultForm.getComponent());
                    tabInfo.setText(argument.getName());
                    tabInfo.setIcon(argument.getIcon());
                    tabInfo.setObject(largeValueResultForm);
                    outputTabs.addTab(tabInfo);
                    if (isFirst) {
                        outputTabs.select(tabInfo, false);
                        isFirst = false;
                    }
                }
            }

            outputTabs.revalidate();
            outputTabs.repaint();
        }
    }

    public void selectArgumentOutputTab(DBArgument argument) {
        for (TabInfo tabInfo : outputTabs.getTabs()) {
            Object object = tabInfo.getObject();
            if (object instanceof MethodExecutionCursorResultForm) {
                MethodExecutionCursorResultForm cursorResultForm = (MethodExecutionCursorResultForm) object;
                if (cursorResultForm.getArgument().equals(argument)) {
                    outputTabs.select(tabInfo, true);
                    break;
                }
            } else if (object instanceof MethodExecutionLargeValueResultForm) {
                MethodExecutionLargeValueResultForm largeValueResultForm = (MethodExecutionLargeValueResultForm) object;
                if (largeValueResultForm.getArgument().equals(argument)) {
                    outputTabs.select(tabInfo, true);
                    break;
                }
            }


        }
    }

    private void updateStatusBarLabels() {
        ConnectionHandler connectionHandler = executionResult.getConnectionHandler();
        if (connectionHandler != null) {
            connectionLabel.setIcon(connectionHandler.getIcon());
            connectionLabel.setText(connectionHandler.getName());
        }

        durationLabel.setText(": " + executionResult.getExecutionDuration() + " ms");
    }



    private void createActionsPanel() {
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.MethodExecutionResult.Controls", false,
                new CloseExecutionResultAction(this),
                new EditMethodAction(this),
                new StartMethodExecutionAction(this),
                new PromptMethodExecutionAction(this),
                ActionUtil.SEPARATOR,
                new OpenSettingsAction());
        actionsPanel.add(actionToolbar.getComponent());
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
        executionResult = null;
    }

    private void createUIComponents() {
        List<ArgumentValue> inputArgumentValues = executionResult.getExecutionInput().getInputArgumentValues();
        List<ArgumentValue> outputArgumentValues = executionResult.getArgumentValues();
        argumentValuesTree = new ArgumentValuesTree(this, inputArgumentValues, outputArgumentValues);
    }
}
