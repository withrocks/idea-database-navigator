package com.dci.intellij.dbn.execution.method.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;

public class MethodExecutionDialog extends DBNDialog<MethodExecutionForm> {
    private boolean debug;

    public MethodExecutionDialog(MethodExecutionInput executionInput, boolean debug) {
        super(executionInput.getProject(), (debug ? "Debug" : "Execute") + " Method", true);
        this.debug = debug;
        setModal(true);
        setResizable(true);
        component = new MethodExecutionForm(this, executionInput, true, debug);
        init();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                new ExecuteAction(),
                getCancelAction(),
                getHelpAction()
        };
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return component.getComponent();
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    private class ExecuteAction extends AbstractAction {
        public ExecuteAction() {
            super(debug ? "Debug" : "Execute",
                    debug ? Icons.METHOD_EXECUTION_DEBUG : Icons.METHOD_EXECUTION_RUN);
            putValue(FOCUSED_ACTION, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent e) {
            component.updateExecutionInput();
            doOKAction();
        }
    }
}
