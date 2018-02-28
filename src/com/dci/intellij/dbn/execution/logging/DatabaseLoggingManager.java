package com.dci.intellij.dbn.execution.logging;

import java.sql.Connection;
import java.sql.SQLException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

public class DatabaseLoggingManager extends AbstractProjectComponent {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private DatabaseLoggingManager(Project project) {
        super(project);
    }

    public static DatabaseLoggingManager getInstance(Project project) {
        return project.getComponent(DatabaseLoggingManager.class);
    }

    @Override
    public void disposeComponent() {
        super.disposeComponent();
    }

    /*********************************************************
     *                       Custom                          *
     *********************************************************/
    public boolean enableLogger(ConnectionHandler connectionHandler, Connection connection) {
        DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
        if (DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler)) {
            try {
                DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
                metadataInterface.enableLogger(connection);
                return true;
            } catch (SQLException e) {
                LOGGER.warn("Error enabling database logging", e);
                DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
                String logName = getLogName(compatibilityInterface);
                NotificationUtil.sendWarningNotification(connectionHandler.getProject(), "Database Logging", "Error enabling " + logName + ": " + e.getMessage());
                return false;
            }
        }

        return false;
    }

    public void disableLogger(ConnectionHandler connectionHandler, @Nullable Connection connection) {
        if (connection != null) {
            DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
            if (DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler)) {
                try {
                    DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
                    metadataInterface.disableLogger(connection);
                } catch (SQLException e) {
                    LOGGER.warn("Error disabling database logging", e);
                    DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
                    String logName = getLogName(compatibilityInterface);
                    NotificationUtil.sendWarningNotification(connectionHandler.getProject(), "Database Logging", "Error disabling " + logName + ": " + e.getMessage());
                }
            }
        }
    }

    public String readLoggerOutput(ConnectionHandler connectionHandler, Connection connection) {
        DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
        DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
        try {
            DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();
            return metadataInterface.readLoggerOutput(connection);
        } catch (SQLException e) {
            LOGGER.warn("Error disabling database logging", e);
            String logName = getLogName(compatibilityInterface);
            NotificationUtil.sendWarningNotification(connectionHandler.getProject(), "Database Logging", "Error loading " + logName + " : " + e.getMessage());
        }

        return null;
    }

    @Nullable
    private String getLogName(@Nullable DatabaseCompatibilityInterface compatibilityInterface) {
        String logName = compatibilityInterface == null ? null : compatibilityInterface.getDatabaseLogName();
        if (StringUtil.isEmpty(logName)) {
            logName = "database logging";
        }
        return logName;
    }

    public boolean supportsLogging(ConnectionHandler connectionHandler) {
        return DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler);
    }


    /*********************************************************
     *                    ProjectComponent                   *
     *********************************************************/
    @NotNull
    @NonNls
    public String getComponentName() {
        return "DBNavigator.Project.DatabaseLoggingManager";
    }
}
