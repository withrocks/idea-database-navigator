package com.dci.intellij.dbn.database.common.debug;

import com.dci.intellij.dbn.database.common.statement.CallableStatementOutput;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ExecutionBacktraceInfo implements CallableStatementOutput {
    private List<DebuggerRuntimeInfo> frames = new ArrayList<DebuggerRuntimeInfo>();

    public List<DebuggerRuntimeInfo> getFrames() {
        return frames;
    }

    public void registerParameters(CallableStatement statement) throws SQLException {
        statement.registerOutParameter(1, Types.VARCHAR);
    }

    public void read(CallableStatement statement) throws SQLException {
        String backtraceListing = statement.getString(1);
        StringTokenizer tokenizer = new StringTokenizer(backtraceListing, "\r");
        while (tokenizer.hasMoreTokens()) {
            String backtraceEntry = tokenizer.nextToken();
            int dotIndex = backtraceEntry.indexOf('.');
            if (dotIndex > 0) {
                int nameEndIndex = backtraceEntry.indexOf(' ', dotIndex);
                String ownerName = backtraceEntry.substring(0, dotIndex);
                String programName = backtraceEntry.substring(dotIndex + 1, nameEndIndex);
                int lineNumberEndIndex = backtraceEntry.indexOf(' ', nameEndIndex + 1);
                Integer lineNumber = new Integer(backtraceEntry.substring(nameEndIndex + 1, lineNumberEndIndex));
                Integer namespace = new Integer(backtraceEntry.substring(lineNumberEndIndex + 1));

                DebuggerRuntimeInfo runtimeInfo = new DebuggerRuntimeInfo(ownerName, programName, namespace, lineNumber);
                frames.add(0, runtimeInfo);
            }
        }
    }
}
