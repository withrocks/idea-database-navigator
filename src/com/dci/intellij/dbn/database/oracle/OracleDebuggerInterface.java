package com.dci.intellij.dbn.database.oracle;

import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseDebuggerInterfaceImpl;
import com.dci.intellij.dbn.database.common.debug.BasicOperationInfo;
import com.dci.intellij.dbn.database.common.debug.BreakpointInfo;
import com.dci.intellij.dbn.database.common.debug.BreakpointOperationInfo;
import com.dci.intellij.dbn.database.common.debug.DebuggerRuntimeInfo;
import com.dci.intellij.dbn.database.common.debug.DebuggerSessionInfo;
import com.dci.intellij.dbn.database.common.debug.ExecutionBacktraceInfo;
import com.dci.intellij.dbn.database.common.debug.ExecutionStatusInfo;
import com.dci.intellij.dbn.database.common.debug.VariableInfo;

import java.sql.Connection;
import java.sql.SQLException;

public class OracleDebuggerInterface extends DatabaseDebuggerInterfaceImpl implements DatabaseDebuggerInterface {
    public OracleDebuggerInterface(DatabaseInterfaceProvider provider) {
        super("oracle_debug_interface.xml", provider);
    }

    public DebuggerSessionInfo initializeSession(Connection connection) throws SQLException {
        executeCall(connection, null, "initialize-session-debugging");
        executeCall(connection, null, "initialize-session-compiler-flags");
        return executeCall(connection, new DebuggerSessionInfo(), "initialize-session");
    }

    public void enableDebugging(Connection connection) throws SQLException {
        executeCall(connection, null, "enable-debugging");
    }

    public void disableDebugging(Connection connection) throws SQLException {
        executeCall(connection, null, "disable-debugging");
    }

    public void attachSession(String sessionId, Connection connection) throws SQLException {
        executeCall(connection, null, "attach-session", sessionId);
    }

    public void detachSession(Connection connection) throws SQLException {
        executeCall(connection, null, "detach-session");
    }

    public DebuggerRuntimeInfo synchronizeSession(Connection connection) throws SQLException {
        return executeCall(connection, new DebuggerRuntimeInfo(), "synchronize-session");
    }

    public BreakpointInfo addBreakpoint(String programOwner, String programName, String programType, int line, Connection connection) throws SQLException {
        return executeCall(connection, new BreakpointInfo(), "add-breakpoint", programOwner, programName, programType, line + 1);
    }

    public BreakpointOperationInfo removeBreakpoint(int breakpointId, Connection connection) throws SQLException {
        return executeCall(connection, new BreakpointOperationInfo(), "remove-breakpoint", breakpointId);
    }

    public BreakpointOperationInfo enableBreakpoint(int breakpointId, Connection connection) throws SQLException {
        return executeCall(connection, new BreakpointOperationInfo(), "enable-breakpoint", breakpointId);
    }

    public BreakpointOperationInfo disableBreakpoint(int breakpointId, Connection connection) throws SQLException {
        return executeCall(connection, new BreakpointOperationInfo(), "disable-breakpoint", breakpointId);
    }

    public DebuggerRuntimeInfo stepOver(Connection connection) throws SQLException {
        return executeCall(connection, new DebuggerRuntimeInfo(), "step-over");
    }

    public DebuggerRuntimeInfo stepInto(Connection connection) throws SQLException {
        return executeCall(connection, new DebuggerRuntimeInfo(), "step-into");
    }

    public DebuggerRuntimeInfo stepOut(Connection connection) throws SQLException {
        return executeCall(connection, new DebuggerRuntimeInfo(), "step-out");
    }

    public DebuggerRuntimeInfo runToPosition(String programOwner, String programName, String programType, int line, Connection connection) throws SQLException {
        BreakpointInfo breakpointInfo = addBreakpoint(programOwner, programName, programType, line, connection);
        DebuggerRuntimeInfo runtimeInfo = stepOut(connection);
        removeBreakpoint(breakpointInfo.getBreakpointId(), connection);
        return runtimeInfo;
    }

    public DebuggerRuntimeInfo resumeExecution(Connection connection) throws SQLException {
        return executeCall(connection, new DebuggerRuntimeInfo(), "resume-execution");
    }

    public DebuggerRuntimeInfo stopExecution(Connection connection) throws SQLException {
        return executeCall(connection, new DebuggerRuntimeInfo(), "stop-execution");
    }

    public DebuggerRuntimeInfo getRuntimeInfo(Connection connection) throws SQLException {
        return executeCall(connection, new DebuggerRuntimeInfo(), "get-runtime-info");
    }

    public ExecutionStatusInfo getExecutionStatusInfo(Connection connection) throws SQLException {
        return executeCall(connection, new ExecutionStatusInfo(), "get-execution-status-info");
    }

    public VariableInfo getVariableInfo(String variableName, Integer frameNumber, Connection connection) throws SQLException {
        return executeCall(connection, new VariableInfo(), "get-variable", variableName, frameNumber);
    }

    public BasicOperationInfo setVariableValue(String variableName, Integer frameNumber, String value, Connection connection) throws SQLException {
        return executeCall(connection, new BasicOperationInfo(), "set-variable-value", frameNumber, variableName, value);
    }

    public ExecutionBacktraceInfo getExecutionBacktraceInfo(Connection connection) throws SQLException {
        return executeCall(connection, new ExecutionBacktraceInfo(), "get-execution-backtrace-table");
    }

    public String[] getRequiredPrivilegeNames() {
        return new String[]{"DEBUG CONNECT SESSION", "DEBUG ANY PROCEDURE"};
    }
}
