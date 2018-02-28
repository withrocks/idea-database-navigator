package com.dci.intellij.dbn.debugger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.thread.RunnableTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.common.debug.BreakpointInfo;
import com.dci.intellij.dbn.database.common.debug.DebuggerRuntimeInfo;
import com.dci.intellij.dbn.database.common.debug.DebuggerSessionInfo;
import com.dci.intellij.dbn.database.common.debug.ExecutionBacktraceInfo;
import com.dci.intellij.dbn.debugger.breakpoint.DBProgramBreakpointHandler;
import com.dci.intellij.dbn.debugger.evaluation.DBProgramDebuggerEditorsProvider;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfiguration;
import com.dci.intellij.dbn.debugger.frame.DBProgramDebugSuspendContext;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.psql.PSQLFile;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerUtil;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XBreakpointType;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.ui.XDebugTabLayouter;

public class DBProgramDebugProcess extends XDebugProcess {
    private Connection targetConnection;
    private Connection debugConnection;
    private ConnectionHandler connectionHandler;
    private DBProgramBreakpointHandler breakpointHandler;
    private DBProgramBreakpointHandler[] breakpointHandlers;
    private MethodExecutionInput executionInput;
    private BreakpointInfo defaultBreakpointInfo;
    private DBProgramDebugProcessStatus status = new DBProgramDebugProcessStatus();

    private transient DebuggerRuntimeInfo runtimeInfo;
    private transient ExecutionBacktraceInfo backtraceInfo;


    public DBProgramDebugProcess(@NotNull XDebugSession session, ConnectionHandler connectionHandler) {
        super(session);
        this.connectionHandler = connectionHandler;
        Project project = session.getProject();
        DatabaseDebuggerManager.getInstance(project).registerDebugSession(connectionHandler);

        DBProgramRunConfiguration runProfile = (DBProgramRunConfiguration) session.getRunProfile();
        executionInput = runProfile.getExecutionInput();

        breakpointHandler = new DBProgramBreakpointHandler(session, this);
        breakpointHandlers = new DBProgramBreakpointHandler[]{breakpointHandler};
    }

    public DBProgramDebugProcessStatus getStatus() {
        return status;
    }

    public Connection getTargetConnection() {
        return targetConnection;
    }

    public Connection getDebugConnection() {
        return debugConnection;
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    @Nullable
    public Project getProject() {
        return connectionHandler == null ? null : connectionHandler.getProject();
    }

    @NotNull
    @Override
    public XBreakpointHandler<?>[] getBreakpointHandlers() {
        return breakpointHandlers;
    }

    @NotNull
    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return DBProgramDebuggerEditorsProvider.INSTANCE;
    }

    @Override
    public void sessionInitialized() {
        final Project project = getSession().getProject();
        new BackgroundTask(project, "Initialize debug environment", true) {
            @Override
            public void execute(@NotNull ProgressIndicator progressIndicator) {
                try {
                    targetConnection = connectionHandler.getPoolConnection(executionInput.getExecutionSchema());
                    targetConnection.setAutoCommit(false);
                    debugConnection = connectionHandler.getPoolConnection();

                    DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                    progressIndicator.setText("Initializing debugger target session");
                    DebuggerSessionInfo sessionInfo = debuggerInterface.initializeSession(targetConnection);
                    debuggerInterface.enableDebugging(targetConnection);
                    debuggerInterface.attachSession(sessionInfo.getSessionId(), debugConnection);

                    synchronizeSession();
                } catch (SQLException e) {
                    getSession().stop();
                    showErrorDialog(e);
                }
            }
        }.start();
    }

    private void synchronizeSession() {
        final XDebugSession session = getSession();
        final Project project = session.getProject();
        new BackgroundTask(project, "Initialize debug environment", true) {

            public void execute(@NotNull ProgressIndicator progressIndicator) {
                final DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();

                if (status.PROCESS_IS_TERMINATING || status.TARGET_EXECUTION_TERMINATED) {
                    session.stop();
                } else {
                    BackgroundTask sessionSynchronizeTask = new BackgroundTask(getProject(), "Synchronizing debug session", false) {
                        @Override
                        protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                            try {
                                executeMethod();
                                runtimeInfo = debuggerInterface.synchronizeSession(debugConnection);

                                if (status.TARGET_EXECUTION_TERMINATED) {
                                    session.stop();
                                } else {
                                    runtimeInfo = debuggerInterface.stepOver(debugConnection);
                                    progressIndicator.setText("Suspending session");
                                    suspendSession();
                                }
                            } catch (SQLException e) {
                                status.SESSION_SYNCHRONIZING_THREW_EXCEPTION = true;
                                MessageUtil.showErrorDialog(project, "Could not initialize debug environment on connection \"" + connectionHandler.getName() + "\". ", e);
                                session.stop();
                            }

                        }
                    };

                    status.CAN_SET_BREAKPOINTS = true;
                    progressIndicator.setText("Registering breakpoints");
                    registerBreakpoints(sessionSynchronizeTask);
                }
            }
        }.start();
    }

    private void executeMethod() {
        new DebugOperationThread("execute method", getProject()) {
            public void executeOperation() throws SQLException {

                XDebugSession session = getSession();
                MethodExecutionManager executionManager = MethodExecutionManager.getInstance(session.getProject());
                if (status.PROCESS_IS_TERMINATING) return;
                if (status.SESSION_SYNCHRONIZING_THREW_EXCEPTION) return;

                try {
                    status.TARGET_EXECUTION_STARTED = true;
                    executionManager.debugExecute(executionInput, targetConnection);
                } catch (SQLException e){
                    // if the method execution threw exception, the debugger-off statement is not reached,
                    // hence the session will hag as debuggable. To avoid this, disable debugging has
                    // to explicitly be called here
                    status.TARGET_EXECUTION_THREW_EXCEPTION = true;
                    DBObjectRef<DBMethod> methodRef = executionInput.getMethodRef();
                    MessageUtil.showErrorDialog(getProject(), "Error executing " + methodRef.getQualifiedNameWithType(), e);
                    getDebuggerInterface().disableDebugging(targetConnection);

                } finally {
                    status.TARGET_EXECUTION_TERMINATED = true;
                    connectionHandler.freePoolConnection(targetConnection);
                    targetConnection = null;
                }
            }
        }.start();
    }
    /**
     * breakpoints need to be registered after the database session is started,
     * otherwise they do not get valid ids
     */
    private void registerBreakpoints(final RunnableTask callback) {
        final Collection<XLineBreakpoint> breakpoints = new ReadActionRunner<Collection<XLineBreakpoint>>() {
            @Override
            protected Collection<XLineBreakpoint> run() {
                XBreakpointType localXBreakpointType = XDebuggerUtil.getInstance().findBreakpointType(breakpointHandler.getBreakpointTypeClass());
                Project project = getSession().getProject();
                XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
                return breakpointManager.getBreakpoints(localXBreakpointType);
            }
        }.start();

        new WriteActionRunner() {
            @Override
            public void run() {
                for (XLineBreakpoint breakpoint : breakpoints) {
                    breakpointHandler.registerBreakpoint(breakpoint);
                }

                registerDefaultBreakpoint();
                callback.start();
            }
        }.start();
    }

    private void registerDefaultBreakpoint() {
        DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) getMainDatabaseFile().getMainContentFile();
        PSQLFile psqlFile = (PSQLFile) sourceCodeFile.getPsiFile();
        if (psqlFile != null) {
            DBMethod method = executionInput.getMethod();
            if (method != null) {
                BasePsiElement basePsiElement = psqlFile.lookupObjectDeclaration(method.getObjectType().getGenericType(), method.getName());
                if (basePsiElement != null) {
                    BasePsiElement subject = basePsiElement.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
                    int offset = subject.getTextOffset();
                    Document document = DocumentUtil.getDocument(psqlFile);
                    int line = document.getLineNumber(offset);

                    DBSchemaObject schemaObject = getMainDatabaseObject();
                    try {
                        defaultBreakpointInfo = getDebuggerInterface().addBreakpoint(
                                method.getSchema().getName(),
                                schemaObject.getName(),
                                schemaObject.getObjectType().getName().toUpperCase(),
                                line,
                                debugConnection);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * breakpoints need to be unregistered before closing the database session, otherwise they remain resident.
     */
    private void unregisterBreakpoints() {
        final Collection<XLineBreakpoint> breakpoints = new ReadActionRunner<Collection<XLineBreakpoint>>() {
            @Override
            protected Collection<XLineBreakpoint> run() {
                XBreakpointType localXBreakpointType = XDebuggerUtil.getInstance().findBreakpointType(breakpointHandler.getBreakpointTypeClass());
                Project project = getSession().getProject();
                XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
                return breakpointManager.getBreakpoints(localXBreakpointType);
            }
        }.start();

        new WriteActionRunner() {
            @Override
            public void run() {
                for (XLineBreakpoint breakpoint : breakpoints) {
                    breakpointHandler.unregisterBreakpoint(breakpoint, false);
                }

                try {
                    if (defaultBreakpointInfo != null && debugConnection != null) {
                        getDebuggerInterface().removeBreakpoint(defaultBreakpointInfo.getBreakpointId(), debugConnection);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void stop() {
        executionInput.setExecutionCancelled(!status.PROCESS_STOPPED_NORMALLY);
        final Project project = getSession().getProject();

        if (status.PROCESS_IS_TERMINATING) return;
        status.PROCESS_IS_TERMINATING = true;

        new BackgroundTask(project, "Stopping debugger", true) {
            public void execute(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setText("Cancelling / resuming method execution.");
                try {
                    unregisterBreakpoints();
                    rollOutDebugger();
                    status.CAN_SET_BREAKPOINTS = false;

                    if (debugConnection != null) {
                        DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                        if (!getStatus().TARGET_EXECUTION_THREW_EXCEPTION) {
                            runtimeInfo = debuggerInterface.stopExecution(debugConnection);
                        }
                        debuggerInterface.detachSession(debugConnection);
                    }
                    status.PROCESS_IS_TERMINATED = true;
                } catch (final SQLException e) {
                    showErrorDialog(e);
                } finally {
                    connectionHandler.freePoolConnection(debugConnection);
                    debugConnection = null;

                    DatabaseDebuggerManager.getInstance(project).unregisterDebugSession(connectionHandler);
                }
            }
        }.start();
    }

    @Override
    public void startStepOver() {
        new DebugOperationThread("step over", getProject()) {
            public void executeOperation() throws SQLException {
                DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                runtimeInfo = debuggerInterface.stepOver(debugConnection);
                suspendSession();
            }
        }.start();
    }

    @Override
    public void startStepInto() {
        new DebugOperationThread("step into", getProject()) {
            public void executeOperation() throws SQLException {
                DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                runtimeInfo = debuggerInterface.stepInto(debugConnection);
                suspendSession();
            }
        }.start();
    }

    @Override
    public void startStepOut() {
        new DebugOperationThread("step out", getProject()) {
            public void executeOperation() throws SQLException {
                DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                runtimeInfo = debuggerInterface.stepOut(debugConnection);
                suspendSession();
            }
        }.start();
    }

    @Override
    public void resume() {
        new DebugOperationThread("resume execution", getProject()) {
            public void executeOperation() throws SQLException {
                DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                runtimeInfo = debuggerInterface.resumeExecution(debugConnection);
                suspendSession();
            }
        }.start();
    }

    @Override
    public void runToPosition(@NotNull final XSourcePosition position) {
        new DebugOperationThread("run to position", getProject()) {
            public void executeOperation() throws SQLException {
                DBSchemaObject object = DBProgramDebugUtil.getObject(position);
                if (object != null) {
                    DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                    runtimeInfo = debuggerInterface.runToPosition(
                            object.getSchema().getName(),
                            object.getName(),
                            object.getObjectType().getName().toUpperCase(),
                            position.getLine(),
                            debugConnection);
                }

                suspendSession();
            }
        }.start();
    }

    @NotNull
    @Override
    public XDebugTabLayouter createTabLayouter() {
        return new DBProgramDebugTabLayouter();
    }

    @Override
    public void startPausing() {
        // NOT SUPPORTED!!!
        new DebugOperationThread("run to position", getProject()) {
            public void executeOperation() throws SQLException {
                DatabaseDebuggerInterface debuggerInterface = getDebuggerInterface();
                runtimeInfo = debuggerInterface.synchronizeSession(debugConnection);
                suspendSession();
            }
        }.start();
    }

    private void showErrorDialog(SQLException e) {
        MessageUtil.showErrorDialog(getProject(), "Could not perform operation.", e);
    }

    private void suspendSession() {
        if (status.PROCESS_IS_TERMINATING) return;

        if (runtimeInfo.getOwnerName() == null) {
            status.PROCESS_STOPPED_NORMALLY = true;
            getSession().stop();
        } else {
            try {
                backtraceInfo = getDebuggerInterface().getExecutionBacktraceInfo(debugConnection);
            } catch (SQLException e) {
                showErrorDialog(e);
            }
            DBEditableObjectVirtualFile databaseFile = getDatabaseFile(runtimeInfo);
            DBProgramDebugSuspendContext suspendContext = new DBProgramDebugSuspendContext(this);
            getSession().positionReached(suspendContext);
            navigateInEditor(databaseFile, runtimeInfo.getLineNumber());
        }
    }

    public DBEditableObjectVirtualFile getDatabaseFile(DebuggerRuntimeInfo runtimeInfo) {
        DBSchemaObject schemaObject = getDatabaseObject(runtimeInfo);
        return schemaObject.getVirtualFile();
    }

    public DBSchemaObject getDatabaseObject(DebuggerRuntimeInfo runtimeInfo) {
        DBSchema schema = connectionHandler.getObjectBundle().getSchema(runtimeInfo.getOwnerName());
        DBSchemaObject schemaObject = schema.getProgram(runtimeInfo.getProgramName());
        if (schemaObject == null) schemaObject = schema.getMethod(runtimeInfo.getProgramName(), 0); // overload 0 is assuming debug is only supported in oracle (no schema method overloading)
        return schemaObject;
    }

    private DBEditableObjectVirtualFile getMainDatabaseFile() {
        DBSchemaObject schemaObject = getMainDatabaseObject();
        return schemaObject.getVirtualFile();
    }

    public DBSchemaObject getMainDatabaseObject() {
        DBMethod method = getMethod();
        return method.isProgramMethod() ? method.getProgram() : method;
    }

    public DBMethod getMethod() {
        return executionInput.getMethod();
    }

    private void rollOutDebugger() {
        try {
            long millis = System.currentTimeMillis();
            while (!status.TARGET_EXECUTION_THREW_EXCEPTION && runtimeInfo!= null && !runtimeInfo.isTerminated()) {
                runtimeInfo = getDebuggerInterface().stepOut(debugConnection);
                // force closing the target connection
                if (System.currentTimeMillis() - millis > 20000) {
                    break;
                }
            }
        } catch (SQLException e) {
            showErrorDialog(e);
        }
    }

    private void navigateInEditor(final DBEditableObjectVirtualFile databaseFile, final int line) {
        new SimpleLaterInvocator() {
            public void execute() {
                // todo review this
                SourceCodeEditor sourceCodeEditor = null;
                DBSourceCodeVirtualFile mainContentFile = (DBSourceCodeVirtualFile) databaseFile.getMainContentFile();
                if (databaseFile.getContentFiles().size() > 1) {
                    FileEditorManager editorManager = FileEditorManager.getInstance(databaseFile.getProject());
                    FileEditor[] fileEditors = editorManager.getEditors(databaseFile);
                    if (fileEditors.length >= runtimeInfo.getNamespace()) {
                        FileEditor fileEditor = fileEditors[runtimeInfo.getNamespace() -1];
                        sourceCodeEditor = (SourceCodeEditor) fileEditor;
                        databaseFile.FAKE_DOCUMENT.set(sourceCodeEditor.getEditor().getDocument());
                    } else {
                        FileEditor fileEditor = EditorUtil.getTextEditor(databaseFile, mainContentFile);
                        if (fileEditor != null && fileEditor instanceof SourceCodeEditor) {
                            sourceCodeEditor = (SourceCodeEditor) fileEditor;
                        }
                    }
                } else {
                    FileEditor fileEditor = EditorUtil.getTextEditor(databaseFile, mainContentFile);
                    if (fileEditor != null && fileEditor instanceof SourceCodeEditor) {
                        sourceCodeEditor = (SourceCodeEditor) fileEditor;
                    }
                }
                LogicalPosition position = new LogicalPosition(line, 0);
                Project project = connectionHandler.getProject();
                if (sourceCodeEditor != null) {
                    EditorUtil.selectEditor(project, sourceCodeEditor, databaseFile, sourceCodeEditor.getEditorProviderId(), true);
                    sourceCodeEditor.getEditor().getScrollingModel().scrollTo(position, ScrollType.CENTER);
                }
            }
        }.start();
    }

    public DatabaseDebuggerInterface getDebuggerInterface() {
        return connectionHandler.getInterfaceProvider().getDebuggerInterface();
    }

    public DebuggerRuntimeInfo getRuntimeInfo() {
        return runtimeInfo;
    }

    public ExecutionBacktraceInfo getBacktraceInfo() {
        return backtraceInfo;
    }

    abstract class DebugOperationThread extends Thread {
        private Project project;
        private String operationName;
        protected DebugOperationThread(String operationName, Project project) {
            super("DBN Debug operation (" + operationName + ')');
            this.operationName = operationName;
        }

        @Override
        public final void run() {
            try {
                executeOperation();
            } catch (final SQLException e) {
                MessageUtil.showErrorDialog(project, "Could not perform debug operation (" + operationName + ").", e);
            }
        }
        public abstract void executeOperation() throws SQLException;
    }
}
