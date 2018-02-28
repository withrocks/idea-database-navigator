package com.dci.intellij.dbn.object.common.status;

public enum DBObjectStatus {
    PRESENT(false, true),
    ENABLED(true, true),
    VALID(true, true),
    DEBUG(true, true),
    COMPILING(false, false),
    SAVING(false, false);

    private boolean propagable;
    private boolean defaultValue;

    DBObjectStatus(boolean propagable, boolean defaultValue) {
        this.propagable = propagable;
        this.defaultValue = defaultValue;
    }

    public boolean isPropagable() {
        return propagable;
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }
}
