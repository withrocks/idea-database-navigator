package com.dci.intellij.dbn.execution.method.browser.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.event.ActionEvent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.execution.method.browser.MethodBrowserSettings;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.ui.ObjectTreeModel;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;

public class MethodExecutionBrowserDialog extends DBNDialog<MethodExecutionBrowserForm> implements Disposable, TreeSelectionListener {
    private SelectAction selectAction;
    private DBObjectRef<DBMethod> methodRef;

    public MethodExecutionBrowserDialog(Project project, MethodBrowserSettings settings, ObjectTreeModel objectTreeModel) {
        super(project, "Method Browser", true);
        setModal(true);
        setResizable(true);
        component = new MethodExecutionBrowserForm(this, settings, objectTreeModel);
        component.addTreeSelectionListener(this);
        init();
    }

    @Override
    public void show() {
        super.show();
    }

    @NotNull
    protected final Action[] createActions() {
        selectAction = new SelectAction();
        selectAction.setEnabled(false);
        return new Action[]{selectAction, getCancelAction()};
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    public void dispose() {
        super.dispose();
    }

    public void valueChanged(TreeSelectionEvent e) {
        selectAction.setEnabled(component.getSelectedMethod() != null);
    }

    public DBMethod getSelectedMethod() {
        return DBObjectRef.get(methodRef);
    }

    /**********************************************************
     *                         Actions                        *
     **********************************************************/
    private class SelectAction extends AbstractAction {

        public SelectAction() {
            super("Select");
        }

        public void actionPerformed(ActionEvent e) {
            methodRef = DBObjectRef.from(component.getSelectedMethod());
            close(OK_EXIT_CODE);
        }

    }
}
