package com.dci.intellij.dbn.object.filter.name.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.name.CompoundFilterCondition;
import com.dci.intellij.dbn.object.filter.name.ConditionJoinType;
import com.dci.intellij.dbn.object.filter.name.SimpleFilterCondition;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class EditFilterConditionDialog extends DBNDialog<EditFilterConditionForm> {
    private SimpleFilterCondition condition;
    private ConditionJoinType joinType;

    public EditFilterConditionDialog(Project project, CompoundFilterCondition parentCondition, SimpleFilterCondition condition, DBObjectType objectType, EditFilterConditionForm.Operation operation) {
        super(project, getTitle(operation), true);
        component = new EditFilterConditionForm(this, parentCondition, condition,  objectType, operation);
        setModal(true);
        setResizable(false);
        init();
    }

    @Nullable
    private static String getTitle(EditFilterConditionForm.Operation operation) {
        return operation == EditFilterConditionForm.Operation.CREATE ? "Create filter" :
        operation == EditFilterConditionForm.Operation.EDIT ? "Edit filter condition" :
        operation == EditFilterConditionForm.Operation.JOIN ? "Join filter condition" : null;
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component.getFocusComponent();
    }

    public void doOKAction() {
        condition = component.getCondition();
        joinType = component.getJoinType();
        super.doOKAction();
    }

    public void doCancelAction() {
        super.doCancelAction();
    }

    public SimpleFilterCondition getCondition() {
        return condition;
    }

    public ConditionJoinType getJoinType() {
        return joinType;
    }
}
