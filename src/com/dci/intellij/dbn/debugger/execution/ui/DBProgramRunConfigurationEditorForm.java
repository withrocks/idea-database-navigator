package com.dci.intellij.dbn.debugger.execution.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfiguration;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.browser.MethodBrowserSettings;
import com.dci.intellij.dbn.execution.method.browser.ui.MethodExecutionBrowserDialog;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionForm;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.ui.ObjectTreeModel;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;

public class DBProgramRunConfigurationEditorForm extends DBNFormImpl {
    private JPanel headerPanel;
    private JPanel mainPanel;
    private JPanel methodArgumentsPanel;
    private JCheckBox compileDependenciesCheckBox;
    private JPanel selectMethodActionPanel;

    private MethodExecutionForm methodExecutionForm;
    private MethodExecutionInput executionInput;

    private DBProgramRunConfiguration configuration;

    public DBProgramRunConfigurationEditorForm(final DBProgramRunConfiguration configuration) {
        this.configuration = configuration;

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, new SelectMethodAction());
        selectMethodActionPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public class SelectMethodAction extends GroupPopupAction {
        public SelectMethodAction()  {
            super("Select method", "Select method", Icons.DBO_METHOD);
        }

        @Override
        protected AnAction[] getActions(AnActionEvent e) {
            return new AnAction[]{
                    new OpenMethodHistoryAction(),
                    new OpenMethodBrowserAction()
            };
        }
    }

    public class OpenMethodBrowserAction extends AnAction {
        public OpenMethodBrowserAction() {
            super("Method Browser");
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            final Project project = ActionUtil.getProject(e);
            if (project != null) {
                BackgroundTask backgroundTask = new BackgroundTask(project, "Loading executable elements", false) {
                    @Override
                    public void execute(@NotNull ProgressIndicator progressIndicator) {
                        final MethodBrowserSettings settings = MethodExecutionManager.getInstance(project).getBrowserSettings();
                        DBMethod currentMethod = configuration.getExecutionInput() == null ? null : configuration.getExecutionInput().getMethod();
                        if (currentMethod != null) {
                            settings.setConnectionHandler(currentMethod.getConnectionHandler());
                            settings.setSchema(currentMethod.getSchema());
                            settings.setMethod(currentMethod);
                        }

                        final ObjectTreeModel objectTreeModel = new ObjectTreeModel(settings.getSchema(), settings.getVisibleObjectTypes(), settings.getMethod());

                        new SimpleLaterInvocator() {
                            public void execute() {
                                final MethodExecutionBrowserDialog browserDialog = new MethodExecutionBrowserDialog(project, settings, objectTreeModel);
                                browserDialog.show();
                                if (browserDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                                    DBMethod method = browserDialog.getSelectedMethod();
                                    MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(project);
                                    MethodExecutionInput methodExecutionInput = methodExecutionManager.getExecutionInput(method);
                                    if (methodExecutionInput != null) {
                                        configuration.setExecutionInput(methodExecutionInput);
                                    }
                                }
                            }
                        }.start();

                    }
                };
                backgroundTask.start();
            }
        }
    }
    public class OpenMethodHistoryAction extends AnAction {
        public OpenMethodHistoryAction() {
            super("Execution History", null, Icons.METHOD_EXECUTION_HISTORY);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = ActionUtil.getProject(e);
            if (project != null) {
                MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(project);
                MethodExecutionInput currentInput = configuration.getExecutionInput();
                MethodExecutionInput methodExecutionInput = methodExecutionManager.selectHistoryMethodExecutionInput(currentInput);
                if (methodExecutionInput != null) {
                    configuration.setExecutionInput(methodExecutionInput);
                }
            }
        }
    }
    public class SelectHistoryMethodAction extends AnAction{
        private MethodExecutionInput executionInput;

        public SelectHistoryMethodAction(MethodExecutionInput executionInput) {
            super("");
            this.executionInput = executionInput;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            configuration.setExecutionInput(executionInput);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            DBMethod method = executionInput.getMethod();
            if (method == null) {
                presentation.setIcon(Icons.DBO_METHOD);
            } else {
                presentation.setIcon(method.getOriginalIcon());
            }
            presentation.setText(NamingUtil.enhanceNameForDisplay(executionInput.getMethodRef().getPath()));
        }
    }


    public MethodExecutionInput getExecutionInput() {
        return executionInput;
    }

    public void writeConfiguration(DBProgramRunConfiguration configuration) {
        if (methodExecutionForm != null) {
            methodExecutionForm.setExecutionInput(configuration.getExecutionInput());
            methodExecutionForm.updateExecutionInput();
        }
        configuration.setCompileDependencies(compileDependenciesCheckBox.isSelected());
        //selectMethodAction.setConfiguration(configuration);
    }

    public void readConfiguration(DBProgramRunConfiguration configuration) {
        setExecutionInput(configuration.getExecutionInput(), false);
        compileDependenciesCheckBox.setSelected(configuration.isCompileDependencies());
    }

    public void setExecutionInput(MethodExecutionInput executionInput, boolean touchForm) {
        this.executionInput = executionInput;
        String headerTitle = "No method selected";
        Icon headerIcon = null;
        Color headerBackground = UIUtil.getPanelBackground();

        methodArgumentsPanel.removeAll();
        if (executionInput != null) {
            DBObjectRef<DBMethod> methodRef = executionInput.getMethodRef();
            headerTitle = methodRef.getPath();
            headerIcon = methodRef.getObjectType().getIcon();
            DBMethod method = executionInput.getMethod();
            if (method != null) {
                methodExecutionForm = new MethodExecutionForm(this, executionInput, false, true);
                methodArgumentsPanel.add(methodExecutionForm.getComponent(), BorderLayout.CENTER);
                if (touchForm) methodExecutionForm.touch();
                headerIcon = method.getOriginalIcon();
                if (getEnvironmentSettings(method.getProject()).getVisibilitySettings().getDialogHeaders().value()) {
                    headerBackground = method.getEnvironmentType().getColor();
                }
            }
        }

        DBNHeaderForm headerForm = new DBNHeaderForm(
                headerTitle,
                headerIcon,
                headerBackground);
        headerPanel.removeAll();
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void dispose() {
        super.dispose();
        DisposerUtil.dispose(methodExecutionForm);
        methodExecutionForm = null;
        executionInput = null;
        configuration = null;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
