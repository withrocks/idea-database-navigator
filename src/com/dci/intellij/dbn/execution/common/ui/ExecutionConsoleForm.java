package com.dci.intellij.dbn.execution.common.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.options.EnvironmentVisibilitySettings;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentChangeListener;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.common.message.ui.ExecutionMessagesPanel;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.dci.intellij.dbn.execution.logging.DatabaseLogOutput;
import com.dci.intellij.dbn.execution.logging.ui.DatabaseLogOutputForm;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentTransactionListener;
import com.intellij.ui.tabs.JBTabsPosition;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.tabs.impl.TabLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

public class ExecutionConsoleForm extends DBNFormImpl{
    private JPanel mainPanel;
    //private Map<Component, ExecutionResult> executionResultsMap = new HashMap<Component, ExecutionResult>();
    private TabbedPane resultTabs;
    private ExecutionMessagesPanel executionMessagesPanel;

    private boolean canScrollToSource;

    public ExecutionConsoleForm(Project project) {
        super(project);
        resultTabs = new TabbedPane(this);
        mainPanel.add(resultTabs, BorderLayout.CENTER);
        resultTabs.setFocusable(false);
        //resultTabs.setAdjustBorders(false);
        resultTabs.addTabMouseListener(mouseListener);
        resultTabs.addListener(tabsListener);
        resultTabs.setPopupGroup(new ExecutionConsolePopupActionGroup(this), "place", false);
        resultTabs.setTabsPosition(JBTabsPosition.bottom);
        resultTabs.setBorder(null);
        EventManager.subscribe(project, EnvironmentChangeListener.TOPIC, environmentChangeListener);
        EventManager.subscribe(project, PsiDocumentTransactionListener.TOPIC, psiDocumentTransactionListener);
    }

    public int getTabCount() {
        return resultTabs.getTabCount();
    }

    private EnvironmentChangeListener environmentChangeListener = new EnvironmentChangeListener() {
        @Override
        public void configurationChanged() {
            EnvironmentVisibilitySettings visibilitySettings = getEnvironmentSettings(getProject()).getVisibilitySettings();
            for (TabInfo tabInfo : resultTabs.getTabs()) {
                ExecutionResult executionResult = getExecutionResult(tabInfo);
                if (executionResult != null) {
                    ConnectionHandler connectionHandler = executionResult.getConnectionHandler();
                    if (connectionHandler != null) {
                        EnvironmentType environmentType = connectionHandler.getEnvironmentType();
                        if (visibilitySettings.getExecutionResultTabs().value()){
                            tabInfo.setTabColor(environmentType.getColor());
                        } else {
                            tabInfo.setTabColor(null);
                        }
                    }
                }
            }
        }
    };

    private PsiDocumentTransactionListener psiDocumentTransactionListener = new PsiDocumentTransactionListener() {

        @Override
        public void transactionStarted(@NotNull Document document, @NotNull PsiFile file) {

        }

        @Override
        public void transactionCompleted(@NotNull Document document, @NotNull PsiFile file) {
            for (TabInfo tabInfo : resultTabs.getTabs()) {
                ExecutionResult executionResult = getExecutionResult(tabInfo);
                if (executionResult instanceof StatementExecutionResult) {
                    StatementExecutionResult statementExecutionResult = (StatementExecutionResult) executionResult;
                    StatementExecutionProcessor executionProcessor = statementExecutionResult.getExecutionProcessor();
                    if (executionProcessor.getPsiFile().equals(file)) {
                        Icon icon = executionProcessor.isDirty() ? Icons.STMT_EXEC_RESULTSET_ORPHAN : Icons.STMT_EXEC_RESULTSET;
                        tabInfo.setIcon(icon);
                    }
                }

                if (executionMessagesPanel != null) {
                    JComponent messagePanelComponent = executionMessagesPanel.getComponent();
                    messagePanelComponent.revalidate();
                    messagePanelComponent.repaint();
                }
            }
        }
    };


    private MouseListener mouseListener = new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
            if (e.isShiftDown() && (16 & e.getModifiers()) > 0 || ((8 & e.getModifiers()) > 0)) {
                if (e.getSource() instanceof TabLabel) {
                    TabLabel tabLabel = (TabLabel) e.getSource();
                    removeTab(tabLabel.getInfo());
                }
            }
        }
    };

    private TabsListener tabsListener = new TabsListener.Adapter() {
        @Override
        public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
            if (canScrollToSource) {
                if (newSelection != null) {
                    ExecutionResult executionResult = getExecutionResult(newSelection);
                    if (executionResult instanceof StatementExecutionResult) {
                        StatementExecutionResult statementExecutionResult = (StatementExecutionResult) executionResult;
                        statementExecutionResult.navigateToEditor(false);
                    }
                }
            }
            if (oldSelection != null && newSelection != null && getExecutionResult(newSelection) instanceof DatabaseLogOutput) {
                newSelection.setIcon(Icons.EXEC_LOG_OUTPUT_CONSOLE);
            }

        }
    };

    public void removeAllExceptTab(TabInfo exceptionTabInfo) {
        for (TabInfo tabInfo : resultTabs.getTabs()) {
            if (tabInfo != exceptionTabInfo) {
                removeTab(tabInfo);
            }
        }
    }

    public synchronized void removeTab(TabInfo tabInfo) {
        ExecutionResult executionResult = getExecutionResult(tabInfo);
        if (executionResult == null) {
            removeMessagesTab();
        } else {
            removeResultTab(executionResult);
        }
    }

    public void removeAllTabs() {
        for (TabInfo tabInfo : resultTabs.getTabs()) {
            removeTab(tabInfo);
        }
    }

    public JComponent getComponent() {
        return resultTabs;
    }

    public void selectResult(StatementExecutionResult executionResult) {
        StatementExecutionMessage executionMessage = executionResult.getExecutionMessage();
        if (executionMessage != null) {
            prepareMessagesTab();
            ExecutionMessagesPanel messagesPane = getMessagesPanel();
            messagesPane.selectMessage(executionMessage, true);
        }
    }

    public void addResult(ExplainPlanResult explainPlanResult) {
        if (explainPlanResult.isError()) {
            prepareMessagesTab();
            ExecutionMessagesPanel messagesPane = getMessagesPanel();
            ExplainPlanMessage explainPlanMessage = new ExplainPlanMessage(explainPlanResult, MessageType.ERROR);
            messagesPane.addExplainPlanMessage(explainPlanMessage, true);
        } else {
            showResultTab(explainPlanResult);
        }
    }

    public void addResult(StatementExecutionResult executionResult) {
        ExecutionMessagesPanel messagesPane = getMessagesPanel();
        TreePath messageTreePath = null;
        CompilerResult compilerResult = executionResult.getCompilerResult();
        boolean hasCompilerResult = compilerResult != null;
        boolean selectMessage = !executionResult.getExecutionProcessor().getExecutionInput().isBulkExecution() && !hasCompilerResult;
        boolean focusMessage = selectMessage && focusOnExecution();
        if (executionResult instanceof StatementExecutionCursorResult) {
            StatementExecutionMessage executionMessage = executionResult.getExecutionMessage();
            if (executionMessage == null) {
                showResultTab(executionResult);
            } else {
                prepareMessagesTab();
                messageTreePath = messagesPane.addExecutionMessage(executionMessage, selectMessage, focusMessage);
            }
        } else {
            prepareMessagesTab();
            messageTreePath = messagesPane.addExecutionMessage(executionResult.getExecutionMessage(), selectMessage, focusMessage);
        }

        if (compilerResult != null) {
            addResult(compilerResult);
        }
        if (messageTreePath != null) {
            messagesPane.expand(messageTreePath);
        }
    }

    private boolean focusOnExecution() {
        ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(getProject());
        StatementExecutionSettings statementExecutionSettings = executionEngineSettings.getStatementExecutionSettings();
        return statementExecutionSettings.isFocusResult();
    }

    public void addResult(CompilerResult compilerResult) {
        prepareMessagesTab();
        CompilerMessage firstMessage = null;
        ExecutionMessagesPanel messagesPanel = getMessagesPanel();
        if (compilerResult.getCompilerMessages().size() > 0) {
            for (CompilerMessage compilerMessage : compilerResult.getCompilerMessages()) {
                if (firstMessage == null) {
                    firstMessage = compilerMessage;
                }
                messagesPanel.addCompilerMessage(compilerMessage, false);
            }
        }

        if (firstMessage != null && firstMessage.isError()) {
            messagesPanel.selectMessage(firstMessage);
        }
    }

    public void addResult(MethodExecutionResult executionResult) {
        showResultTab(executionResult);
    }

    public void addResults(List<CompilerResult> compilerResults) {
        prepareMessagesTab();
        CompilerMessage firstMessage = null;
        ExecutionMessagesPanel messagesPanel = getMessagesPanel();
        for (CompilerResult compilerResult : compilerResults) {
            if (compilerResult.getCompilerMessages().size() > 0) {
                for (CompilerMessage compilerMessage : compilerResult.getCompilerMessages()) {
                    if (firstMessage == null) {
                        firstMessage = compilerMessage;
                    }
                    messagesPanel.addCompilerMessage(compilerMessage, false);
                }
            }
        }
        if (firstMessage != null && firstMessage.isError()) {
            messagesPanel.selectMessage(firstMessage);
        }
    }
    
    public ExecutionResult getSelectedExecutionResult() {
        TabInfo selectedInfo = resultTabs.getSelectedInfo();
        return selectedInfo == null ? null : getExecutionResult(selectedInfo);
    }

    @Nullable
    private static ExecutionResult getExecutionResult(TabInfo tabInfo) {
        ExecutionResultForm executionResultForm = (ExecutionResultForm) tabInfo.getObject();
        return executionResultForm == null ? null : executionResultForm.getExecutionResult();
    }

    /*********************************************************
     *                       Messages                        *
     *********************************************************/
    private ExecutionMessagesPanel getMessagesPanel() {
        if (executionMessagesPanel == null) {
            executionMessagesPanel = new ExecutionMessagesPanel(this);
            Disposer.register(this, executionMessagesPanel);
        }
        return executionMessagesPanel;
    }

    private void prepareMessagesTab() {
        JComponent component = getMessagesPanel().getComponent();
        if (resultTabs.getTabCount() == 0 || resultTabs.getTabAt(0).getComponent() != component) {
            TabInfo tabInfo = new TabInfo(component);

            tabInfo.setText("Messages");
            tabInfo.setIcon(Icons.EXEC_RESULT_MESSAGES);
            resultTabs.addTab(tabInfo, 0);

        }

        TabInfo tabInfo = resultTabs.getTabAt(0);
        resultTabs.select(tabInfo, true);
    }


    public void removeMessagesTab() {
        ExecutionMessagesPanel executionMessagesPanel = getMessagesPanel();
        JComponent component = executionMessagesPanel.getComponent();
        if (resultTabs.getTabCount() > 0 || resultTabs.getTabAt(0).getComponent() == component) {
            TabInfo tabInfo = resultTabs.getTabAt(0);
            resultTabs.removeTab(tabInfo);
        }

        executionMessagesPanel.reset();
        if (getTabCount() == 0) {
            ExecutionManager.getInstance(getProject()).hideExecutionConsole();
        }
    }

    private boolean isMessagesTabVisible() {
        if (resultTabs.getTabCount() > 0) {
            JComponent messagesPanelComponent = getMessagesPanel().getComponent();
            TabInfo tabInfo = resultTabs.getTabAt(0);
            return tabInfo.getComponent() == messagesPanelComponent;
        }
        return false;
    }

    /*********************************************************
     *                       Logging                         *
     *********************************************************/
    public void displayLogOutput(ConnectionHandler connectionHandler, String output) {
        for (TabInfo tabInfo : resultTabs.getTabs()) {
            ExecutionResult executionResult = getExecutionResult(tabInfo);
            if (executionResult instanceof DatabaseLogOutput) {
                DatabaseLogOutput logOutput = (DatabaseLogOutput) executionResult;
                if (logOutput.getConnectionHandler() == connectionHandler) {
                    logOutput.write(output);
                    tabInfo.setIcon(Icons.EXEC_LOG_OUTPUT_CONSOLE_UNREAD);
                    return;
                }
            }
        }
        boolean messagesTabVisible = isMessagesTabVisible();

        DatabaseLogOutput logOutput = new DatabaseLogOutput(connectionHandler);
        DatabaseLogOutputForm form = logOutput.getForm(true);
        if (form != null) {
            JComponent component = form.getComponent();
            TabInfo tabInfo = new TabInfo(component);
            tabInfo.setObject(form);
            tabInfo.setText(logOutput.getName());
            tabInfo.setIcon(Icons.EXEC_LOG_OUTPUT_CONSOLE_UNREAD);

            EnvironmentVisibilitySettings visibilitySettings = getEnvironmentSettings(getProject()).getVisibilitySettings();
            if (visibilitySettings.getExecutionResultTabs().value()){
                tabInfo.setTabColor(connectionHandler.getEnvironmentType().getColor());
            } else {
                tabInfo.setTabColor(null);
            }

            resultTabs.addTab(tabInfo, messagesTabVisible ? 1 : 0);
            logOutput.write(output);
        }
    }

    /*********************************************************
     *                  Statement executions                 *
     *********************************************************/
    public void showResultTab(ExecutionResult executionResult) {
        if (executionResult instanceof ExplainPlanResult) {
            addResultTab(executionResult);
        } else {
            if (containsResultTab(executionResult)) {
                selectResultTab(executionResult);
            } else {
                addResultTab(executionResult);
            }
        }
    }

    public void addResultTab(ExecutionResult executionResult) {
        ExecutionResultForm resultForm = executionResult.getForm(true);
        if (resultForm != null) {
            JComponent component = resultForm.getComponent();
            TabInfo tabInfo = new TabInfo(component);
            tabInfo.setObject(resultForm);
            EnvironmentVisibilitySettings visibilitySettings = getEnvironmentSettings(getProject()).getVisibilitySettings();
            if (visibilitySettings.getExecutionResultTabs().value()){
                tabInfo.setTabColor(executionResult.getConnectionHandler().getEnvironmentType().getColor());
            } else {
                tabInfo.setTabColor(null);
            }
            tabInfo.setText(executionResult.getName());
            tabInfo.setIcon(executionResult.getIcon());
            resultTabs.addTab(tabInfo);
            selectResultTab(tabInfo);
        }
    }

    public boolean containsResultTab(ExecutionResult executionProcessor) {
        ExecutionResultForm resultForm = executionProcessor.getForm(false);
        if (resultForm != null) {
            Component component = resultForm.getComponent();
            return containsResultTab(component);
        }
        return false;
    }

    public void removeResultTab(ExecutionResult executionResult) {
        try {
            canScrollToSource = false;
            ExecutionResultForm resultForm = executionResult.getForm(false);
            if (resultForm != null) {
                TabInfo tabInfo = resultTabs.findInfo(resultForm.getComponent());
                if (resultTabs.getTabs().contains(tabInfo)) {
                    resultTabs.removeTab(tabInfo);
                    if (executionResult instanceof StatementExecutionResult) {
                        StatementExecutionResult statementExecutionResult = (StatementExecutionResult) executionResult;
                        StatementExecutionInput executionInput = statementExecutionResult.getExecutionInput();
                        if (executionInput != null && !executionInput.isDisposed()) {
                            DBLanguagePsiFile file = executionInput.getExecutionProcessor().getPsiFile();
                            DocumentUtil.refreshEditorAnnotations(file);
                        }
                    }
                }
                if (getTabCount() == 0) {
                    ExecutionManager.getInstance(getProject()).hideExecutionConsole();
                }
            }
        } finally {
            canScrollToSource = true;
        }
    }

    public <T extends ExecutionResult> void selectResultTab(T executionResult) {
        ExecutionResultForm resultForm = executionResult.getForm(false);
        if (resultForm != null) {
            resultForm.setExecutionResult(executionResult);
            resultForm = executionResult.getForm(false);
            if (resultForm != null) {
                JComponent component = resultForm.getComponent();
                TabInfo tabInfo = resultTabs.findInfo(component);
                if (tabInfo != null) {
                    tabInfo.setText(executionResult.getName());
                    tabInfo.setIcon(executionResult.getIcon());
                    selectResultTab(tabInfo);
                }

            }
        }


    }

    /*********************************************************
     *                      Miscellaneous                    *
     *********************************************************/

    private boolean containsResultTab(Component component) {
        for (TabInfo tabInfo : resultTabs.getTabs()) {
            if (tabInfo.getComponent() == component) {
                return true;
            }
        }
        return false;
    }

    private void selectResultTab(TabInfo tabInfo) {
        try {
            canScrollToSource = false;
            resultTabs.select(tabInfo, true);
        } finally {
            canScrollToSource = true;
        }
    }

    public void dispose() {
        EventManager.unsubscribe(environmentChangeListener);
        super.dispose();
    }
}
