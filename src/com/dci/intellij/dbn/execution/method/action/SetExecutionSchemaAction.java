package com.dci.intellij.dbn.execution.method.action;

import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;

public class SetExecutionSchemaAction extends DumbAwareAction {
    private MethodExecutionInput executionInput;
    private DBSchema schema;

    public SetExecutionSchemaAction(MethodExecutionInput executionInput, DBSchema schema) {
        super(schema.getName(), null, schema.getIcon());
        this.executionInput = executionInput;
        this.schema = schema;
    }

    public void actionPerformed(AnActionEvent e) {
        executionInput.setExecutionSchema(schema);
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText(NamingUtil.enhanceUnderscoresForDisplay(schema.getName()));
        presentation.setIcon(schema.getIcon());
        presentation.setDescription(schema.getDescription());
    }
}
