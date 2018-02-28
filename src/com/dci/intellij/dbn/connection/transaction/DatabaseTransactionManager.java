package com.dci.intellij.dbn.connection.transaction;

import java.sql.SQLException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionStatusListener;
import com.dci.intellij.dbn.connection.transaction.options.TransactionManagerSettings;
import com.dci.intellij.dbn.connection.transaction.ui.UncommittedChangesDialog;
import com.dci.intellij.dbn.connection.transaction.ui.UncommittedChangesOverviewDialog;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;

public class DatabaseTransactionManager extends AbstractProjectComponent implements ProjectManagerListener{

    private DatabaseTransactionManager(Project project) {
        super(project);
    }

    public static DatabaseTransactionManager getInstance(Project project) {
        return project.getComponent(DatabaseTransactionManager.class);
    }

    public void execute(final ConnectionHandler connectionHandler, boolean background, final TransactionAction... actions) {
        Project project = connectionHandler.getProject();
        String connectionName = connectionHandler.getName();
        if (ApplicationManager.getApplication().isDisposeInProgress()) {
            executeActions(connectionHandler, actions);
        } else {
            new BackgroundTask(project, "Performing " + actions[0].getName() + " on connection " + connectionName, background) {
                @Override
                public void execute(@NotNull ProgressIndicator indicator) {
                    executeActions(connectionHandler, actions);
                }
            }.start();
        }
    }

    public TransactionManagerSettings getTransactionManagerSettings() {
        return ProjectSettingsManager.getInstance(getProject()).getOperationSettings().getTransactionManagerSettings();
    }

    public void executeActions(ConnectionHandler connectionHandler, TransactionAction... actions) {
        Project project = getProject();
        String connectionName = connectionHandler.getName();
        TransactionListener transactionListener = EventManager.notify(project, TransactionListener.TOPIC);
        for (TransactionAction action : actions) {
            if (action != null) {
                boolean success = true;
                try {
                    // notify pre-action
                    transactionListener.beforeAction(connectionHandler, action);
                    ProgressMonitor.setTaskDescription("Performing " + action.getName() + " on connection " + connectionName);

                    action.execute(connectionHandler);
                    if (action.getNotificationType() != null) {
                        NotificationUtil.sendNotification(
                                project,
                                action.getNotificationType(),
                                action.getName(),
                                action.getSuccessNotificationMessage(),
                                connectionName);
                    }
                } catch (SQLException ex) {
                    NotificationUtil.sendErrorNotification(
                            project,
                            action.getName(),
                            action.getErrorNotificationMessage(),
                            connectionName,
                            ex.getMessage());
                    success = false;
                } finally {
                    if (!project.isDisposed()) {
                        // notify post-action
                        transactionListener.afterAction(connectionHandler, action, success);

                        if (action.isStatusChange()) {
                            ConnectionStatusListener statusListener = EventManager.notify(project, ConnectionStatusListener.TOPIC);
                            statusListener.statusChanged(connectionHandler.getId());
                        }
                    }
                }
            }
        }
    }

    public void commit(final ConnectionHandler connectionHandler, boolean fromEditor, boolean background) {
        if (fromEditor && connectionHandler.getUncommittedChanges().size() > 1) {
            Project project = connectionHandler.getProject();
            VirtualFile selectedFile = EditorUtil.getSelectedFile(project);
            if (selectedFile != null) {
                InteractiveOptionHandler<TransactionOption> commitMultipleChangesOptionHandler = getTransactionManagerSettings().getCommitMultipleChangesOptionHandler();
                TransactionOption result = commitMultipleChangesOptionHandler.resolve(connectionHandler.getName(), selectedFile.getPresentableUrl());
                switch (result) {
                    case COMMIT: execute(connectionHandler, background, TransactionAction.COMMIT); break;
                    case REVIEW_CHANGES: showUncommittedChangesDialog(connectionHandler, null); break;
                }
            }
        } else {
            execute(connectionHandler, background, TransactionAction.COMMIT);
        }
    }

    public void rollback(final ConnectionHandler connectionHandler, boolean fromEditor, boolean background) {
        if (fromEditor && connectionHandler.getUncommittedChanges().size() > 1) {
            Project project = connectionHandler.getProject();
            VirtualFile selectedFile = EditorUtil.getSelectedFile(project);
            if (selectedFile != null) {
                InteractiveOptionHandler<TransactionOption> rollbackMultipleChangesOptionHandler = getTransactionManagerSettings().getRollbackMultipleChangesOptionHandler();
                TransactionOption result = rollbackMultipleChangesOptionHandler.resolve(connectionHandler.getName(), selectedFile.getPresentableUrl());
                switch (result) {
                    case ROLLBACK: execute(connectionHandler, background, TransactionAction.ROLLBACK); break;
                    case REVIEW_CHANGES: showUncommittedChangesDialog(connectionHandler, null); break;
                }
            }
        } else {
            execute(connectionHandler, background, TransactionAction.ROLLBACK);
        }
    }

    public void disconnect(final ConnectionHandler connectionHandler, boolean background) {
        execute(connectionHandler, background, TransactionAction.DISCONNECT);
    }


    public boolean showUncommittedChangesOverviewDialog(@Nullable TransactionAction additionalOperation) {
        UncommittedChangesOverviewDialog executionDialog = new UncommittedChangesOverviewDialog(getProject(), additionalOperation);
        executionDialog.show();
        return executionDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE;
    }

    public boolean showUncommittedChangesDialog(ConnectionHandler connectionHandler, @Nullable TransactionAction additionalOperation) {
        UncommittedChangesDialog executionDialog = new UncommittedChangesDialog(connectionHandler, additionalOperation, false);
        executionDialog.show();
        return executionDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE;
    }

    public void toggleAutoCommit(ConnectionHandler connectionHandler) {
        boolean isAutoCommit = connectionHandler.isAutoCommit();
        TransactionAction autoCommitAction = isAutoCommit ?
                TransactionAction.TURN_AUTO_COMMIT_OFF :
                TransactionAction.TURN_AUTO_COMMIT_ON;

        if (!isAutoCommit && connectionHandler.hasUncommittedChanges()) {
            InteractiveOptionHandler<TransactionOption> toggleAutoCommitOptionHandler = getTransactionManagerSettings().getToggleAutoCommitOptionHandler();
            TransactionOption result = toggleAutoCommitOptionHandler.resolve(connectionHandler.getName());
            switch (result) {
                case COMMIT: execute(connectionHandler, true, TransactionAction.COMMIT, autoCommitAction); break;
                case ROLLBACK: execute(connectionHandler, true, TransactionAction.ROLLBACK, autoCommitAction); break;
                case REVIEW_CHANGES: showUncommittedChangesDialog(connectionHandler, autoCommitAction);
            }
        } else {
            execute(connectionHandler, false, autoCommitAction);
        }
    }

    public void disconnect(ConnectionHandler connectionHandler) {
        if (connectionHandler.hasUncommittedChanges()) {
            InteractiveOptionHandler<TransactionOption> disconnectOptionHandler = getTransactionManagerSettings().getDisconnectOptionHandler();
            TransactionOption result = disconnectOptionHandler.resolve(connectionHandler.getName());

            switch (result) {
                case COMMIT: execute(connectionHandler, false, TransactionAction.COMMIT, TransactionAction.DISCONNECT); break;
                case ROLLBACK: execute(connectionHandler, false, TransactionAction.DISCONNECT); break;
                case REVIEW_CHANGES: showUncommittedChangesDialog(connectionHandler, TransactionAction.DISCONNECT);
            }
        } else {
            execute(connectionHandler, false, TransactionAction.DISCONNECT);
        }
    }

    /**********************************************
    *            ProjectManagerListener           *
    ***********************************************/

    /**********************************************
    *                ProjectComponent             *
    ***********************************************/
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.TransactionManager";
    }
}