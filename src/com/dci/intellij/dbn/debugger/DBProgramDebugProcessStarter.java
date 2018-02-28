package com.dci.intellij.dbn.debugger;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;

public class DBProgramDebugProcessStarter extends XDebugProcessStarter {
    private ConnectionHandler connectionHandler;

    public DBProgramDebugProcessStarter(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    @NotNull
    @Override
    public XDebugProcess start(@NotNull XDebugSession session) {
        return new DBProgramDebugProcess(session, connectionHandler);
    }

}
