package com.dci.intellij.dbn.database.common.debug;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

public class BreakpointInfo extends BasicOperationInfo {
    private int breakpointId;

    public int getBreakpointId() {
        return breakpointId;
    }

    public void registerParameters(CallableStatement statement) throws SQLException {
        statement.registerOutParameter(1, Types.NUMERIC);
        statement.registerOutParameter(2, Types.VARCHAR);
    }

    public void read(CallableStatement statement) throws SQLException {
        breakpointId = statement.getInt(1);
        error = statement.getString(2);
    }
}
