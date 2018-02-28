package com.dci.intellij.dbn.execution.logging.action;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.execution.logging.DatabaseLogOutput;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public abstract class AbstractDatabaseLogOutputAction extends DumbAwareAction {
    protected AbstractDatabaseLogOutputAction(String text, Icon icon) {
        super(text, null, icon);
    }

    public static DatabaseLogOutput getDatabaseLogOutput(AnActionEvent e) {
        return e.getData(DBNDataKeys.DATABASE_LOG_OUTPUT);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        DatabaseLogOutput databaseLogOutput = getDatabaseLogOutput(e);
        e.getPresentation().setEnabled(databaseLogOutput != null);
    }

}
