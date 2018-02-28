package com.dci.intellij.dbn.connection.transaction.action;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;

public class ToggleDatabaseLoggingAction extends ToggleAction {
    private ConnectionHandler connectionHandler;

    public ToggleDatabaseLoggingAction(ConnectionHandler connectionHandler) {
        super("Database Logging");
        this.connectionHandler = connectionHandler;
    }

    @Override
    public boolean isSelected(AnActionEvent e) {
        return connectionHandler.isLoggingEnabled();
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        connectionHandler.setLoggingEnabled(state);
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
        boolean supportsLogging = DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler);
        Presentation presentation = e.getPresentation();
        presentation.setVisible(supportsLogging);
        String databaseLogName = compatibilityInterface.getDatabaseLogName();
        if (StringUtil.isNotEmpty(databaseLogName)) {
            presentation.setText("Database Logging (" + databaseLogName + ")");
        }
    }
}
