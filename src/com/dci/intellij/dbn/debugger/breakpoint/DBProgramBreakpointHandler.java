package com.dci.intellij.dbn.debugger.breakpoint;

import java.sql.Connection;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.common.debug.BreakpointInfo;
import com.dci.intellij.dbn.database.common.debug.BreakpointOperationInfo;
import com.dci.intellij.dbn.debugger.DBProgramDebugProcess;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;

public class DBProgramBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<DBProgramBreakpointProperties>> {
    public static final Key<Integer> BREAKPOINT_ID_KEY = new Key<Integer>("BREAKPOINT_ID");
    public static final Key<DBEditableObjectVirtualFile> DATABASE_FILE_KEY = Key.create("DBNavigator.DatabaseEditableObjectFile");

    private XDebugSession session;
    private DBProgramDebugProcess debugProcess;

    public DBProgramBreakpointHandler(XDebugSession session, DBProgramDebugProcess debugProcess) {
        super(DBProgramBreakpointType.class);
        this.session = session;
        this.debugProcess  =debugProcess;
        //resetBreakpoints();
    }

    @Override
    public void registerBreakpoint(@NotNull XLineBreakpoint<DBProgramBreakpointProperties> breakpoint) {
        if (!debugProcess.getStatus().CAN_SET_BREAKPOINTS) return;

        ConnectionHandler connectionHandler = debugProcess.getConnectionHandler();
        DBEditableObjectVirtualFile databaseFile = getDatabaseFile(breakpoint);
        if (databaseFile == null) {
            XDebuggerManager.getInstance(session.getProject()).getBreakpointManager().removeBreakpoint(breakpoint);
        } else {
            DBSchemaObject object = databaseFile.getObject();
            if (object != null && object.getConnectionHandler() == connectionHandler) {
                DatabaseDebuggerInterface debuggerInterface = connectionHandler.getInterfaceProvider().getDebuggerInterface();

                Connection debugConnection = debugProcess.getDebugConnection();
                try {
                    Integer breakpointId = breakpoint.getUserData(BREAKPOINT_ID_KEY);

                    if (breakpointId != null) {
                        BreakpointOperationInfo breakpointOperationInfo = debuggerInterface.enableBreakpoint(breakpointId, debugConnection);
                        String error = breakpointOperationInfo.getError();
                        if (error != null) {
                            session.updateBreakpointPresentation( breakpoint,
                                    Icons.DEBUG_INVALID_BREAKPOINT,
                                    "INVALID: " + error);
                        }

                    } else {
                        BreakpointInfo breakpointInfo = debuggerInterface.addBreakpoint(
                                object.getSchema().getName(),
                                object.getName(),
                                object.getObjectType().getName().toUpperCase(),
                                breakpoint.getLine(),
                                debugConnection);

                        String error = breakpointInfo.getError();
                        if (error != null) {
                            session.updateBreakpointPresentation( breakpoint,
                                    Icons.DEBUG_INVALID_BREAKPOINT,
                                    "INVALID: " + error);
                        } else {
                            breakpoint.putUserData(BREAKPOINT_ID_KEY, breakpointInfo.getBreakpointId());

                            if (!breakpoint.isEnabled()) {
                                BreakpointOperationInfo breakpointOperationInfo = debuggerInterface.disableBreakpoint(breakpointInfo.getBreakpointId(), debugConnection);
                                error = breakpointOperationInfo.getError();
                                if (error != null) {
                                    session.updateBreakpointPresentation( breakpoint,
                                            Icons.DEBUG_INVALID_BREAKPOINT,
                                            "INVALID: " + error);
                                }

                            }
                        }
                    }

                } catch (SQLException e) {
                    session.updateBreakpointPresentation( breakpoint,
                            Icons.DEBUG_INVALID_BREAKPOINT,
                            "INVALID: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void unregisterBreakpoint(@NotNull XLineBreakpoint<DBProgramBreakpointProperties> breakpoint, boolean temporary) {
        if (!debugProcess.getStatus().CAN_SET_BREAKPOINTS) return;
        
        DBEditableObjectVirtualFile databaseFile = getDatabaseFile(breakpoint);
        DBSchemaObject object = databaseFile.getObject();
        if (object != null && object.getConnectionHandler() == debugProcess.getConnectionHandler()) {
            ConnectionHandler connectionHandler = object.getConnectionHandler();
            if (connectionHandler != null) {
                DatabaseDebuggerInterface debuggerInterface = connectionHandler.getInterfaceProvider().getDebuggerInterface();
                Integer breakpointId = breakpoint.getUserData(BREAKPOINT_ID_KEY);

                if (breakpointId != null) {
                    try {
                        Connection debugConnection = debugProcess.getDebugConnection();
                        if (temporary) {
                            debuggerInterface.disableBreakpoint(breakpointId, debugConnection);
                        } else {
                            debuggerInterface.removeBreakpoint(breakpointId, debugConnection);
                            breakpoint.putUserData(BREAKPOINT_ID_KEY, null);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    private void resetBreakpoints() {
        Project project = session.getProject();

        XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
        XBreakpoint<?>[] breakpoints = breakpointManager.getAllBreakpoints();

        for (XBreakpoint breakpoint : breakpoints) {
            if (breakpoint.getType() instanceof DBProgramBreakpointType) {
                XLineBreakpoint lineBreakpoint = (XLineBreakpoint) breakpoint;
                DBEditableObjectVirtualFile databaseFile = getDatabaseFile(lineBreakpoint);
                if (databaseFile != null && databaseFile.getConnectionHandler() == debugProcess.getConnectionHandler()) {
                    lineBreakpoint.putUserData(BREAKPOINT_ID_KEY, null);
                }
            }
        }
    }

    public static DBEditableObjectVirtualFile getDatabaseFile(XLineBreakpoint<DBProgramBreakpointProperties> breakpoint) {
        DBEditableObjectVirtualFile databaseFile = breakpoint.getUserData(DATABASE_FILE_KEY);
        if (databaseFile == null) {
            DatabaseFileSystem databaseFileSystem = DatabaseFileSystem.getInstance();
            databaseFile = (DBEditableObjectVirtualFile) databaseFileSystem.findFileByPath(breakpoint.getFileUrl());
            breakpoint.putUserData(DATABASE_FILE_KEY, databaseFile);
        }
        return databaseFile; 
    }


}
