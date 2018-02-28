package com.dci.intellij.dbn.debugger.breakpoint;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.breakpoints.XBreakpointProperties;

public class DBProgramBreakpointProperties extends XBreakpointProperties<DBProgramBreakpointState> {
    private VirtualFile file;
    private int line;
    private DBProgramBreakpointState state = new DBProgramBreakpointState(true);

    public DBProgramBreakpointProperties(VirtualFile file, int line) {
        this.file = file;
        this.line = line;
    }

    public VirtualFile getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }


    public DBProgramBreakpointState getState() {
        return state;
    }

    public void loadState(DBProgramBreakpointState state) {
        this.state = state;
    }
}
