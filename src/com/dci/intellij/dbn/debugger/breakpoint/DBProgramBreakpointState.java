package com.dci.intellij.dbn.debugger.breakpoint;

public class DBProgramBreakpointState {
    private boolean enabled;

    public DBProgramBreakpointState() {
        enabled = true;
    }

    public DBProgramBreakpointState(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
