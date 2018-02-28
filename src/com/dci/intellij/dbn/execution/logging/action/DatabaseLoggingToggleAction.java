package com.dci.intellij.dbn.execution.logging.action;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class DatabaseLoggingToggleAction extends ToggleAction implements DumbAware {

    public DatabaseLoggingToggleAction() {
        super("Enable / Disable Database Logging", null, Icons.EXEC_LOG_OUTPUT_CONSOLE);
    }

    public boolean isSelected(AnActionEvent e) {
        ConnectionHandler activeConnection = getActiveConnection(e);
        return activeConnection != null && activeConnection.isLoggingEnabled();
    }

    @Nullable
    private static ConnectionHandler getActiveConnection(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (project != null && virtualFile != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            ConnectionHandler activeConnection = connectionMappingManager.getActiveConnection(virtualFile);
            if (activeConnection != null && !activeConnection.isVirtual() && !activeConnection.isDisposed()) {
                return activeConnection ;
            }

        }
        return null;
    }

    public void setSelected(AnActionEvent e, boolean selected) {
        ConnectionHandler activeConnection = getActiveConnection(e);
        if (activeConnection != null) activeConnection.setLoggingEnabled(selected);
    }

    public void update(AnActionEvent e) {
        super.update(e);
        ConnectionHandler activeConnection = getActiveConnection(e);
        Presentation presentation = e.getPresentation();

        boolean visible = false;
        String name = "Database Logging";
        if (activeConnection != null) {

            boolean supportsLogging = DatabaseFeature.DATABASE_LOGGING.isSupported(activeConnection);
            if (supportsLogging) {
                visible = true;
                DatabaseCompatibilityInterface compatibilityInterface = activeConnection.getInterfaceProvider().getCompatibilityInterface();
                String databaseLogName = compatibilityInterface.getDatabaseLogName();
                if (StringUtil.isNotEmpty(databaseLogName)) {
                    name = name + " (" + databaseLogName + ")";
                }
            }
        }
        presentation.setText(name);
        presentation.setVisible(visible);
    }
}