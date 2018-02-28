package com.dci.intellij.dbn.object.action;

import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.operation.DBOperationNotSupportedException;
import com.dci.intellij.dbn.object.common.operation.DBOperationType;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class EnableDisableAction extends AnAction {
    private DBSchemaObject object;

    public EnableDisableAction(DBSchemaObject object) {
        super("Enable/Disable");
        this.object = object;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        boolean enabled = object.getStatus().is(DBObjectStatus.ENABLED);
        try {
            DBOperationType operationType = enabled ? DBOperationType.DISABLE : DBOperationType.ENABLE;
            object.getOperationExecutor().executeOperation(operationType);
        } catch (SQLException e1) {
            String message = "Error " + (!enabled ? "enabling " : "disabling ") + object.getQualifiedNameWithType();
            MessageUtil.showErrorDialog(project, message, e1);
        } catch (DBOperationNotSupportedException e1) {
            MessageUtil.showErrorDialog(project, e1.getMessage());
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enabled = object.getStatus().is(DBObjectStatus.ENABLED);
        e.getPresentation().setText(!enabled? "Enable" : "Disable");
    }
}