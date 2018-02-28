package com.dci.intellij.dbn.database.common.debug;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DebuggerRuntimeInfo extends BasicOperationInfo {
    private String ownerName;
    private String programName;
    private Integer namespace;
    private Integer lineNumber;
    private int breakpointId;
    private int reason;
    private boolean terminated;

    public DebuggerRuntimeInfo() {}

    public DebuggerRuntimeInfo(String ownerName, String programName, Integer namespace, Integer lineNumber) {
        this.ownerName = ownerName;
        this.programName = programName;
        this.namespace = namespace;
        this.lineNumber = Math.max(lineNumber -1, 0) ;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getProgramName() {
        return programName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public int getBreakpointId() {
        return breakpointId;
    }

    public Integer getNamespace() {
        return namespace;
    }

    public int getReason() {
        return reason;
    }

    public void registerParameters(CallableStatement statement) throws SQLException {
        statement.registerOutParameter(1, Types.VARCHAR);
        statement.registerOutParameter(2, Types.VARCHAR);
        statement.registerOutParameter(3, Types.NUMERIC);
        statement.registerOutParameter(4, Types.NUMERIC);
        statement.registerOutParameter(5, Types.NUMERIC);
        statement.registerOutParameter(6, Types.NUMERIC);
        statement.registerOutParameter(7, Types.NUMERIC);
        statement.registerOutParameter(8, Types.VARCHAR);
    }

    public void read(CallableStatement statement) throws SQLException {
        ownerName = statement.getString(1);
        programName = statement.getString(2);
        namespace = statement.getInt(3);
        lineNumber = Math.max(statement.getInt(4) - 1, 0);
        terminated = statement.getInt(5) != 0;
        breakpointId = statement.getInt(6);
        reason = statement.getInt(7);
        error = statement.getString(8);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DebuggerRuntimeInfo that = (DebuggerRuntimeInfo) o;

        if (!lineNumber.equals(that.lineNumber)) return false;
        if (!namespace.equals(that.namespace)) return false;
        if (!ownerName.equals(that.ownerName)) return false;
        if (!programName.equals(that.programName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = ownerName.hashCode();
        result = 31 * result + programName.hashCode();
        result = 31 * result + namespace.hashCode();
        result = 31 * result + lineNumber.hashCode();
        return result;
    }
}

