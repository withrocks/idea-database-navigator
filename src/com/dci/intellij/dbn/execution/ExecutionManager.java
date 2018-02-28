package com.dci.intellij.dbn.execution;

import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.ui.ExecutionConsoleForm;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentFactoryImpl;

@State(
    name = "DBNavigator.Project.ExecutionManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class ExecutionManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String TOOL_WINDOW_ID = "DB Execution Console";
    private ExecutionConsoleForm executionConsoleForm;

    private ExecutionManager(Project project) {
        super(project);
    }

    public static ExecutionManager getInstance(Project project) {
        return project.getComponent(ExecutionManager.class);
    }

    private void showExecutionConsole() {
        ToolWindow toolWindow = initExecutionConsole();
        toolWindow.show(null);
    }

    public void hideExecutionConsole() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(getProject());
        ToolWindow toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
        if (toolWindow != null) {
            toolWindow.getContentManager().removeAllContents(false);
            toolWindow.setAvailable(false, null);
        }
    }

    @Override
    public void projectOpened() {
        ToolWindow toolWindow = initExecutionConsole();
        toolWindow.getContentManager().removeAllContents(false);
        toolWindow.setAvailable(false, null);

    }

    private ToolWindow initExecutionConsole() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(getProject());
        ToolWindow toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
        if (toolWindow == null) {
            toolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, true, ToolWindowAnchor.BOTTOM, getProject(), true);
            toolWindow.setIcon(Icons.WINDOW_EXECUTION_CONSOLE);
            toolWindow.setToHideOnEmptyContent(true);
        }

        if (toolWindow.getContentManager().getContents().length == 0) {
            ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
            if (executionConsoleForm != null) {
                ContentFactory contentFactory = new ContentFactoryImpl();
                Content content = contentFactory.createContent(executionConsoleForm.getComponent(), null, true);
                toolWindow.getContentManager().addContent(content);
                toolWindow.setAvailable(true, null);
            }
        }
        return toolWindow;
    }

    public void addExecutionResult(final CompilerResult compilerResult) {
        new ConditionalLaterInvocator() {
            public void execute() {
                showExecutionConsole();
                ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
                if (executionConsoleForm != null) {
                    executionConsoleForm.addResult(compilerResult);
                }
            }
        }.start();
    }

    public void addExecutionResults(final List<CompilerResult> compilerResults) {
        new ConditionalLaterInvocator() {
            public void execute() {
                showExecutionConsole();
                ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
                if (executionConsoleForm != null) {
                    executionConsoleForm.addResults(compilerResults);
                }
            }
        }.start();
    }

    public void addExplainPlanResult(final ExplainPlanResult explainPlanResult) {
        new ConditionalLaterInvocator() {
            public void execute() {
                showExecutionConsole();
                ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
                if (executionConsoleForm != null) {
                    executionConsoleForm.addResult(explainPlanResult);
                }
            }
        }.start();
    }

    public void addExecutionResult(final StatementExecutionResult executionResult) {
        new ConditionalLaterInvocator() {
            public void execute() {
                showExecutionConsole();
                ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
                if (executionConsoleForm != null) {
                    if (executionResult.isLoggingActive()) {
                        executionConsoleForm.displayLogOutput(executionResult.getConnectionHandler(), executionResult.getLoggingOutput());
                    }

                    executionConsoleForm.addResult(executionResult);
                    if (!executionResult.isBulkExecution() && !executionResult.hasCompilerResult() && !focusOnExecution()) {
                        executionResult.navigateToEditor(true);
                    }
                }
            }
        }.start();
    }

    private boolean focusOnExecution() {
        Project project = getProject();
        ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(project);
        StatementExecutionSettings statementExecutionSettings = executionEngineSettings.getStatementExecutionSettings();
        return statementExecutionSettings.isFocusResult();
    }


    public void addExecutionResult(final MethodExecutionResult executionResult) {
        new ConditionalLaterInvocator() {
            public void execute() {
                showExecutionConsole();
                ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
                if (executionConsoleForm != null) {
                    executionConsoleForm.addResult(executionResult);
                }
            }
        }.start();
    }

    public void selectExecutionResult(final StatementExecutionResult executionResult) {
        new ConditionalLaterInvocator() {
            public void execute() {
                ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
                if (executionConsoleForm != null) {
                    executionConsoleForm.selectResult(executionResult);
                    showExecutionConsole();
                }
            }
        }.start();

    }

    public void removeMessagesTab() {
        ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
        if (executionConsoleForm != null) {
            executionConsoleForm.removeMessagesTab();
        }
    }

    public void removeResultTab(ExecutionResult executionResult) {
        ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
        if (executionConsoleForm != null) {
            executionConsoleForm.removeResultTab(executionResult);
        }
    }

    public void selectResultTab(ExecutionResult executionResult) {
        showExecutionConsole();
        ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
        if (executionConsoleForm != null) {
            executionConsoleForm.selectResultTab(executionResult);
        }
    }

    @Nullable
    public ExecutionConsoleForm getExecutionConsoleForm() {
        if (executionConsoleForm == null && !isDisposed()) {
            executionConsoleForm = new ExecutionConsoleForm(getProject());
        }
        return executionConsoleForm;
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.ExecutionManager";
    }

    @Override
    public void disposeComponent() {
        super.disposeComponent();
        DisposerUtil.dispose(executionConsoleForm);
        executionConsoleForm = null;
    }

    @Nullable
    public ExecutionResult getSelectedExecutionResult() {
        return executionConsoleForm == null ? null : executionConsoleForm.getSelectedExecutionResult();
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(Element element) {
    }
}
