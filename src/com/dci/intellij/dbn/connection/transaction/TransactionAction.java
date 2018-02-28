package com.dci.intellij.dbn.connection.transaction;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.notification.NotificationType;

import java.io.Serializable;
import java.sql.SQLException;

public enum TransactionAction implements Serializable {
    COMMIT(
            Constants.DBN_TITLE_PREFIX + "Commit",
            NotificationType.INFORMATION, "Connection \"{0}\" committed",
            "Error committing connection \"{0}\". Details: {1}",
            false,
            new Executor() {
                void execute(ConnectionHandler connectionHandler) throws SQLException {
                    connectionHandler.commit();
                }
            }),

    ROLLBACK(
            Constants.DBN_TITLE_PREFIX + "Rollback",
            NotificationType.INFORMATION, "Connection \"{0}\" rolled back.",
            "Error rolling back connection \"{0}\". Details: {1}",
            false,
            new Executor() {
                void execute(ConnectionHandler connectionHandler) throws SQLException {
                    connectionHandler.rollback();
                }
            }),

    ROLLBACK_IDLE(
            Constants.DBN_TITLE_PREFIX + "Rollback",
            NotificationType.INFORMATION, "Connection \"{0}\" rolled back.",
            "Error rolling back connection \"{0}\". Details: {1}",
            false,
            new Executor() {
                void execute(ConnectionHandler connectionHandler) throws SQLException {
                    connectionHandler.rollback();
                }
            }),

    DISCONNECT(
            Constants.DBN_TITLE_PREFIX + "Disconnect",
            NotificationType.INFORMATION, "Disconnected from \"{0}\"",
            "Error disconnecting from \"{0}\". Details: {1}",
            true,
            new Executor() {
                @Override
                void execute(ConnectionHandler connectionHandler) throws SQLException {
                    connectionHandler.disconnect();
                }
            }),

    DISCONNECT_IDLE(
            Constants.DBN_TITLE_PREFIX + "Idle Disconnect",
            NotificationType.WARNING, "Disconnected from \"{0}\" because it has exceeded the configured idle timeout.",
            "Error disconnecting from \"{0}\". Details: {1}",
            true,
            new Executor() {
                void execute(ConnectionHandler connectionHandler) throws SQLException {
                    connectionHandler.disconnect();
                }
            }),

    PING(
            Constants.DBN_TITLE_PREFIX + "Ping",
            null, "",
            "Error checking connectivity for \"{0}\". Details: {1}",
            false,
            new Executor() {
                @Override
                void execute(ConnectionHandler connectionHandler) throws SQLException {
                    connectionHandler.ping(true);
                }
            }),

    TURN_AUTO_COMMIT_ON(
            Constants.DBN_TITLE_PREFIX + "Auto-Commit ON",
            NotificationType.WARNING,
            "Auto-Commit turned ON for connection \"{0}\".",
            "Error turning Auto-Commit ON for connection \"{0}\". Details: {1}",
            true,
            new Executor() {
                void execute(ConnectionHandler connectionHandler) throws SQLException {
                    assert !connectionHandler.isAutoCommit();
                    connectionHandler.setAutoCommit(true);
                }
            }),

    TURN_AUTO_COMMIT_OFF(
            Constants.DBN_TITLE_PREFIX + "Auto-Commit OFF",
            NotificationType.INFORMATION, "Auto-Commit turned OFF for connection \"{0}\".",
            "Error turning Auto-Commit OFF for connection\"{0}\". Details: {1}",
            true,
            new Executor() {
                void execute(ConnectionHandler connectionHandler) throws SQLException {
                    assert connectionHandler.isAutoCommit();
                    connectionHandler.setAutoCommit(false);
                }
            });


    private String name;
    private String successNotificationMessage;
    private String errorNotificationMessage;
    private NotificationType notificationType;
    private Executor executor;
    private boolean isStatusChange;

    private TransactionAction(String name, NotificationType notificationType, String successNotificationMessage, String errorNotificationMessage, boolean isStatusChange, Executor executor) {
        this.name = name;
        this.errorNotificationMessage = errorNotificationMessage;
        this.successNotificationMessage = successNotificationMessage;
        this.executor = executor;
        this.isStatusChange = isStatusChange;
        this.notificationType = notificationType;
    }

    public String getName() {
        return name;
    }

    public String getSuccessNotificationMessage() {
        return successNotificationMessage;
    }

    public String getErrorNotificationMessage() {
        return errorNotificationMessage;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public boolean isStatusChange() {
        return isStatusChange;
    }

    private abstract static class Executor {
        abstract void execute(ConnectionHandler connectionHandler) throws SQLException;
    }

    public void execute(ConnectionHandler connectionHandler) throws SQLException {
        executor.execute(connectionHandler);
    }

}
