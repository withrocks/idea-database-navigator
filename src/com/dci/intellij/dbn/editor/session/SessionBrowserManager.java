package com.dci.intellij.dbn.editor.session;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.option.InteractiveOptionHandler;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.session.options.SessionBrowserSettings;
import com.dci.intellij.dbn.editor.session.options.SessionInterruptionOption;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.dci.intellij.dbn.vfs.DBSessionBrowserVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

@State(
    name = "DBNavigator.Project.SessionEditorManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class SessionBrowserManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {

    private Timer timestampUpdater;
    private List<DBSessionBrowserVirtualFile> openFiles = new ArrayList<DBSessionBrowserVirtualFile>();

    private SessionBrowserManager(Project project) {
        super(project);
    }

    public static SessionBrowserManager getInstance(Project project) {
        return project.getComponent(SessionBrowserManager.class);
    }

    public SessionBrowserSettings getSessionBrowserSettings() {
        return ProjectSettingsManager.getInstance(getProject()).getOperationSettings().getSessionBrowserSettings();
    }

    public void openSessionBrowser(ConnectionHandler connectionHandler) {
        Project project = connectionHandler.getProject();
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        DBSessionBrowserVirtualFile sessionBrowserFile = connectionHandler.getSessionBrowserFile();
        fileEditorManager.openFile(sessionBrowserFile, true);
    }

    public String loadSessionCurrentSql(ConnectionHandler connectionHandler, Object sessionId) {
        try {
            DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
            DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
            if (compatibilityInterface.supportsFeature(DatabaseFeature.SESSION_CURRENT_SQL)) {
                DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();

                Connection connection = connectionHandler.getStandaloneConnection();
                ResultSet resultSet = metadataInterface.loadSessionCurrentSql(sessionId, connection);
                if (resultSet.next()) {
                    return resultSet.getString(1);
                }
            } else {
                return "";
            }
        } catch (SQLException e) {
            NotificationUtil.sendWarningNotification(connectionHandler.getProject(), "Session Browser", "Could not load current session SQL. Cause: {0}", e.getMessage());
        }
        return "";
    }

    public void interruptSessions(final SessionBrowser sessionBrowser, final Map<Object, Object> sessionIds, SessionInterruptionType type) {
        final ConnectionHandler connectionHandler = FailsafeUtil.get(sessionBrowser.getConnectionHandler());
        final DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
        DatabaseCompatibilityInterface compatibilityInterface = interfaceProvider.getCompatibilityInterface();
        if (compatibilityInterface.supportsFeature(DatabaseFeature.SESSION_INTERRUPTION_TIMING)) {

            SessionBrowserSettings sessionBrowserSettings = getSessionBrowserSettings();
            InteractiveOptionHandler<SessionInterruptionOption> disconnectOptionHandler =
                    type == SessionInterruptionType.KILL ? sessionBrowserSettings.getKillSessionOptionHandler() :
                    type == SessionInterruptionType.DISCONNECT  ? sessionBrowserSettings.getDisconnectSessionOptionHandler() : null;

            if (disconnectOptionHandler != null) {
                String subject = sessionIds.size() > 1 ? "selected sessions" : "session with id \"" + sessionIds.keySet().iterator().next().toString() + "\"";
                SessionInterruptionOption result = disconnectOptionHandler.resolve(subject, connectionHandler.getName());
                if (result != SessionInterruptionOption.CANCEL && result != SessionInterruptionOption.ASK) {
                    doInterruptSessions(sessionBrowser, sessionIds, type, result);
                }
            }
        } else {
            doInterruptSessions(sessionBrowser, sessionIds, SessionInterruptionType.KILL, SessionInterruptionOption.NORMAL);
        }
    }

    private void doInterruptSessions(final SessionBrowser sessionBrowser, final Map<Object, Object> sessionIds, final SessionInterruptionType type, final SessionInterruptionOption option) {
        final String killedAction = type == SessionInterruptionType.KILL ? "killed" : "disconnected";
        final String killingAction = type == SessionInterruptionType.KILL? "killing" : "disconnecting";
        final String taskAction = (type == SessionInterruptionType.KILL? "Killing" : "Disconnecting") + (sessionIds.size() == 1 ? " Session" : " Sessions");

        final ConnectionHandler connectionHandler = FailsafeUtil.get(sessionBrowser.getConnectionHandler());
        final DatabaseInterfaceProvider interfaceProvider = connectionHandler.getInterfaceProvider();
        new BackgroundTask(getProject(), taskAction, false, true) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                Project project = connectionHandler.getProject();
                Connection connection = null;
                try {
                    connection = connectionHandler.getPoolConnection();
                    Map<Object, SQLException> errors = new HashMap<Object, SQLException>();
                    final DatabaseMetadataInterface metadataInterface = interfaceProvider.getMetadataInterface();

                    for (Object sessionId : sessionIds.keySet()) {
                        Object serialNumber = sessionIds.get(sessionId);
                        if (progressIndicator.isCanceled()) return;
                        try {
                            boolean immediate = option == SessionInterruptionOption.IMMEDIATE;
                            boolean postTransaction = option == SessionInterruptionOption.POST_TRANSACTION;
                            switch (type) {
                                case DISCONNECT: metadataInterface.disconnectSession(sessionId, serialNumber, postTransaction, immediate, connection); break;
                                case KILL: metadataInterface.killSession(sessionId, serialNumber, immediate, connection); break;
                            }
                        } catch (SQLException e) {
                            errors.put(sessionId, e);
                        }
                    }

                    if (sessionIds.size() == 1) {
                        Object sessionId = sessionIds.keySet().iterator().next();
                        if (errors.size() == 0) {
                            MessageUtil.showInfoDialog(project, "Info", "Session " + sessionId + " " + killedAction +".");
                        } else {
                            SQLException exception = errors.get(sessionId);
                            MessageUtil.showErrorDialog(project, "Error " + killingAction + " session " + sessionId + "." , exception);
                        }
                    } else {
                        if (errors.size() == 0) {
                            MessageUtil.showInfoDialog(project, "Info", sessionIds.size() + " sessions " + killedAction + ".");
                        } else {
                            StringBuilder message = new StringBuilder("Error " + killingAction + " one or more of the selected sessions:");
                            for (Object sessionId : sessionIds.keySet()) {
                                SQLException exception = errors.get(sessionId);
                                message.append("\n - session id ").append(sessionId).append(": ");
                                if (exception == null) message.append(killedAction); else message.append(exception.getMessage().trim());

                            }
                            MessageUtil.showErrorDialog(project, message.toString());
                        }

                    }
                } catch (SQLException e) {
                    MessageUtil.showErrorDialog(project, "Error performing operation", e);
                } finally {
                    connectionHandler.freePoolConnection(connection);
                    sessionBrowser.loadSessions(false);
                }
            }
        }.start();
    }

    private class UpdateTimestampTask extends TimerTask {
        public void run() {
            if (openFiles.size() > 0) {
                new ReadActionRunner() {
                    @Override
                    protected Object run() {
                        Project project = getProject();
                        if (project != null && !project.isDisposed()) {
                            final List<SessionBrowser> sessionBrowsers = new ArrayList<SessionBrowser>();
                            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                            FileEditor[] editors = fileEditorManager.getAllEditors();
                            for (FileEditor editor : editors) {
                                if (editor instanceof SessionBrowser) {
                                    SessionBrowser sessionBrowser = (SessionBrowser) editor;
                                    sessionBrowsers.add(sessionBrowser);
                                }
                            }

                            new SimpleLaterInvocator() {
                                @Override
                                protected void execute() {
                                    for (SessionBrowser sessionBrowser : sessionBrowsers) {
                                        sessionBrowser.refreshLoadTimestamp();
                                    }
                                }
                            }.start();
                        }
                        return null;
                    }
                }.start();

            }
        }
    }

    /****************************************
    *             ProjectComponent          *
    *****************************************/
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.SessionEditorManager";
    }

    @Override
    public void projectOpened() {
        EventManager.subscribe(getProject(), FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
    }

    public void projectClosed() {
        EventManager.unsubscribe(fileEditorManagerListener);
    }

    private FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerAdapter() {
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (file instanceof DBSessionBrowserVirtualFile) {
                boolean schedule = openFiles.size() == 0;
                DBSessionBrowserVirtualFile sessionBrowserFile = (DBSessionBrowserVirtualFile) file;
                openFiles.add(sessionBrowserFile);

                if (schedule) {
                    timestampUpdater = new Timer("DBN Session Browser timestamp updater");
                    timestampUpdater.schedule(new UpdateTimestampTask(), TimeUtil.ONE_SECOND, TimeUtil.ONE_SECOND);
                }
            }
        }

        public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (file instanceof DBSessionBrowserVirtualFile) {
                DBSessionBrowserVirtualFile sessionBrowserFile = (DBSessionBrowserVirtualFile) file;
                openFiles.remove(sessionBrowserFile);

                if (openFiles.size() == 0 && timestampUpdater != null) {
                    timestampUpdater.cancel();
                    timestampUpdater.purge();
                }
            }
        }
    };

    @Override
    public void disposeComponent() {
        if (timestampUpdater != null) {
            timestampUpdater.cancel();
            timestampUpdater.purge();
        }
        openFiles.clear();
    }


    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        return element;
    }

    @Override
    public void loadState(Element element) {
    }
}
