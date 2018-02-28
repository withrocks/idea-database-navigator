package com.dci.intellij.dbn.editor.session;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModel;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
import com.dci.intellij.dbn.editor.session.options.SessionBrowserSettings;
import com.dci.intellij.dbn.editor.session.ui.SessionBrowserDetailsForm;
import com.dci.intellij.dbn.editor.session.ui.SessionBrowserForm;
import com.dci.intellij.dbn.editor.session.ui.table.SessionBrowserTable;
import com.dci.intellij.dbn.vfs.DBSessionBrowserVirtualFile;
import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;

public class SessionBrowser extends UserDataHolderBase implements FileEditor, Disposable, ConnectionProvider {
    private DBSessionBrowserVirtualFile sessionBrowserFile;
    private SessionBrowserForm editorForm;
    private boolean preventLoading = false;
    private boolean loading;
    private Timer refreshTimer;
    private String modelError;

    public SessionBrowser(DBSessionBrowserVirtualFile sessionBrowserFile) {
        this.sessionBrowserFile = sessionBrowserFile;
        editorForm = new SessionBrowserForm(this);
        Disposer.register(this, editorForm);
        loadSessions(true);
    }

    @Nullable
    public SessionBrowserTable getEditorTable() {
        return editorForm == null ? null : editorForm.getEditorTable();
    }

    @Nullable
    public SessionBrowserForm getEditorForm() {
        return editorForm;
    }

    public void showSearchHeader() {
        editorForm.showSearchHeader();
    }

    @Nullable
    public SessionBrowserModel getTableModel() {
        SessionBrowserTable browserTable = getEditorTable();
        return browserTable == null ? null : browserTable.getModel();
    }

    public SessionBrowserSettings getSettings() {
        return OperationSettings.getInstance(getProject()).getSessionBrowserSettings();
    }

    public boolean isPreventLoading(boolean force) {
        if (force) return false;
        SessionBrowserTable editorTable = getEditorTable();
        if (editorTable != null && editorTable.getSelectedRowCount() > 1) {
            return true;
        }
        return preventLoading;
    }

    public void loadSessions(boolean force) {
        if (!loading && !isPreventLoading(force)) {
            final ConnectionHandler connectionHandler = FailsafeUtil.get(getConnectionHandler());
            setLoading(true);
            new BackgroundTask(getProject(), "Loading sessions", true) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                    try {
                        if (isDisposed()) throw new InterruptedException("Process cancelled");
                        DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
                        Connection connection = connectionHandler.getStandaloneConnection();
                        ResultSet resultSet = metadataInterface.loadSessions(connection);
                        SessionBrowserModel model = new SessionBrowserModel(connectionHandler, resultSet);
                        replaceModel(model);
                        modelError = null;
                    } catch (SQLException e) {
                        modelError = e.getMessage();
                        SessionBrowserModel model = getTableModel();
                        if (model == null || model.isDisposed()) {
                            model = new SessionBrowserModel(connectionHandler);
                            replaceModel(model);
                        }
                    } finally {
                        EventManager.notify(getProject(), SessionBrowserLoadListener.TOPIC).sessionsLoaded(sessionBrowserFile);
                        setLoading(false);
                    }
                }
            }.start();
        }
    }

    private void replaceModel(final SessionBrowserModel newModel) {
        if (newModel != null) {
            new SimpleLaterInvocator() {
                @Override
                protected void execute() {
                    SessionBrowserTable editorTable = getEditorTable();
                    if (editorTable != null) {
                        SessionBrowserModel oldModel = editorTable.getModel();
                        SessionBrowserState state = oldModel.getState();
                        newModel.setState(state);
                        editorTable.setModel(newModel);
                        refreshTable();
                        DisposerUtil.dispose(oldModel);
                    }
                }
            }.start();
        }
    }

    public void clearFilter() {
        SessionBrowserTable editorTable = getEditorTable();
        if (editorTable != null) {
            SessionBrowserFilterState filter = editorTable.getModel().getFilter();
            if (filter != null) {
                filter.clear();
                refreshTable();
            }
        }
    }

    public void refreshTable() {
        SessionBrowserTable editorTable = getEditorTable();
        if (editorTable != null) {
            editorTable.revalidate();
            editorTable.repaint();
            editorTable.accommodateColumnsSize();
            editorTable.restoreSelection();
        }
    }

    public void refreshLoadTimestamp() {
        if (editorForm != null) {
            editorForm.refreshLoadTimestamp();
        }
    }

    public void disconnectSelectedSessions() {
        interruptSessions(SessionInterruptionType.DISCONNECT);
    }

    public void killSelectedSessions() {
        interruptSessions(SessionInterruptionType.KILL);
    }

    public void interruptSession(Object sessionId, Object serialNumber, SessionInterruptionType type) {
        SessionBrowserManager sessionBrowserManager = SessionBrowserManager.getInstance(getProject());
        Map<Object, Object> sessionIds = new HashMap<Object, Object>();
        sessionIds.put(sessionId, serialNumber);
        sessionBrowserManager.interruptSessions(this, sessionIds, type);
        loadSessions(true);
    }

    private void interruptSessions(SessionInterruptionType type) {
        SessionBrowserManager sessionBrowserManager = SessionBrowserManager.getInstance(getProject());
        SessionBrowserTable editorTable = getEditorTable();
        if (editorTable != null) {
            int[] selectedRows = editorTable.getSelectedRows();
            Map<Object, Object> sessionIds = new HashMap<Object, Object>();
            for (int selectedRow : selectedRows) {
                SessionBrowserModelRow row = editorTable.getModel().getRowAtIndex(selectedRow);
                Object sessionId = row.getSessionId();
                Object serialNumber = row.getSerialNumber();
                sessionIds.put(sessionId, serialNumber);
            }

            sessionBrowserManager.interruptSessions(this, sessionIds, type);
            loadSessions(true);
        }
    }


    public DBSessionBrowserVirtualFile getDatabaseFile() {
        return sessionBrowserFile;
    }

    public Project getProject() {
        return sessionBrowserFile.getProject();
    }

    @NotNull
    public JComponent getComponent() {
        return disposed ? new JPanel() : editorForm.getComponent();
    }

    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return getEditorTable();
    }

    @NonNls
    @NotNull
    public String getName() {
        return "Data";
    }

    @NotNull
    public FileEditorState getState(@NotNull FileEditorStateLevel level) {
        SessionBrowserTable editorTable = getEditorTable();
        if (editorTable != null) {
            SessionBrowserModel model = editorTable.getModel();
            return model.getState().clone();
        }
        return SessionBrowserState.VOID;
    }

    public void setState(@NotNull FileEditorState fileEditorState) {
        if (fileEditorState instanceof SessionBrowserState) {
            SessionBrowserTable editorTable = getEditorTable();
            if (editorTable != null) {
                SessionBrowserModel model = editorTable.getModel();
                SessionBrowserState sessionBrowserState = (SessionBrowserState) fileEditorState;
                model.setState(sessionBrowserState);
                refreshTable();
                startRefreshTimer((sessionBrowserState).getRefreshInterval());
            }
        }
    }

    public boolean isModified() {
        return false;
    }

    public boolean isValid() {
        return true;
    }

    public void selectNotify() {

    }

    public void deselectNotify() {

    }

    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
    }

    @Nullable
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Nullable
    public StructureViewBuilder getStructureViewBuilder() {
        return null;
    }

    public void setPreventLoading(boolean preventLoading) {
        this.preventLoading = preventLoading;
    }

    public boolean isLoading() {
        return loading;
    }

    protected void setLoading(boolean loading) {
        if (this.loading != loading) {
            this.loading = loading;

            if (editorForm != null) {
                if (loading)
                    editorForm.showLoadingHint(); else
                    editorForm.hideLoadingHint();
            }

            SessionBrowserTable editorTable = getEditorTable();
            if (editorTable != null) {
                editorTable.setLoading(loading);
                editorTable.revalidate();
                editorTable.repaint();
            }
        }

    }

    public int getRowCount() {
        SessionBrowserTable browserTable = getEditorTable();
        return browserTable == null ? 0 : browserTable.getRowCount();
    }

    public void setRefreshInterval(int refreshInterval) {
        SessionBrowserModel tableModel = getTableModel();
        if (tableModel != null) {
            SessionBrowserState state = tableModel.getState();
            if (state.getRefreshInterval() != refreshInterval) {
                state.setRefreshInterval(refreshInterval);
                stopRefreshTimer();
                startRefreshTimer(refreshInterval);
            }
        }
    }

    private void startRefreshTimer(int refreshInterval) {
        if (refreshTimer == null && refreshInterval > 0) {
            refreshTimer = new Timer("DBN Session Browser refresher");
            int period = refreshInterval * 1000;
            refreshTimer.schedule(new RefreshTask(), period, period);
        }
    }

    private void stopRefreshTimer() {
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer.purge();
            refreshTimer = null;
        }
    }

    public String getModelError() {
        return modelError;
    }

    public int getRefreshInterval() {
        SessionBrowserModel tableModel = getTableModel();
        return tableModel == null ? 0 : tableModel.getState().getRefreshInterval();
    }

    @Nullable
    public Object getSelectedSessionId() {
        SessionBrowserTable editorTable = getEditorTable();
        if (editorTable != null) {
            if (editorTable.getSelectedRowCount() == 1) {
                int rowIndex = editorTable.getSelectedRow();
                SessionBrowserModelRow rowAtIndex = editorTable.getModel().getRowAtIndex(rowIndex);
                return rowAtIndex.getSessionId();
            }
        }
        return null;
    }

    private class RefreshTask extends TimerTask {
        public void run() {
            loadSessions(false);
        }
    }

    public void updateDetails() {
        if (editorForm != null) {
            SessionBrowserTable editorTable = editorForm.getEditorTable();
            SessionBrowserDetailsForm detailsForm = editorForm.getDetailsForm();
            if (editorTable.getSelectedRowCount() == 1) {
                SessionBrowserModelRow selectedRow = editorTable.getModel().getRowAtIndex(editorTable.getSelectedRow());
                detailsForm.update(selectedRow);
            } else {
                detailsForm.update(null);
            }
        }
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        return sessionBrowserFile.getConnectionHandler();
    }

    /*******************************************************
     *                      Listeners                      *
     *******************************************************/


    /*******************************************************
     *                   Data Provider                     *
     *******************************************************/
    public DataProvider dataProvider = new DataProvider() {
        @Override
        public Object getData(@NonNls String dataId) {
            if (DBNDataKeys.SESSION_BROWSER.is(dataId)) {
                return SessionBrowser.this;
            }
            if (PlatformDataKeys.PROJECT.is(dataId)) {
                return getProject();
            }
            return null;
        }
    };

    public DataProvider getDataProvider() {
        return dataProvider;
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        if (!disposed) {
            disposed = true;
            stopRefreshTimer();
            editorForm = null;
        }
    }
}

