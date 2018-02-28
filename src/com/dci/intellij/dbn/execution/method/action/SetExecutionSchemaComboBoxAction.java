package com.dci.intellij.dbn.execution.method.action;

import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;

public class SetExecutionSchemaComboBoxAction extends ComboBoxAction {
    private MethodExecutionInput executionInput;

    public SetExecutionSchemaComboBoxAction(MethodExecutionInput executionInput) {
        this.executionInput = executionInput;
        DBSchema schema = executionInput.getExecutionSchema();
        if (schema != null) {
            Presentation presentation = getTemplatePresentation();
            presentation.setText(NamingUtil.enhanceUnderscoresForDisplay(schema.getName()));
            presentation.setIcon(schema.getIcon());
        }
    }

    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
        ConnectionHandler connectionHandler = FailsafeUtil.get(executionInput.getConnectionHandler());
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        for (DBSchema schema : connectionHandler.getObjectBundle().getSchemas()){
            actionGroup.add(new SetExecutionSchemaAction(executionInput, schema));
        }
        return actionGroup;
    }

    public void update(AnActionEvent e) {
        DBSchema schema = executionInput.getExecutionSchema();
        Presentation presentation = e.getPresentation();
        presentation.setText(NamingUtil.enhanceUnderscoresForDisplay(schema.getName()));
        presentation.setIcon(schema.getIcon());

    }
 }