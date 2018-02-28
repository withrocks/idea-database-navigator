package com.dci.intellij.dbn.common.util;

public class ThreadLocalFlag {
    private boolean defaultValue;
    private ThreadLocal<Boolean> flag = new ThreadLocal<Boolean>();

    public ThreadLocalFlag(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean get() {
        Boolean value = flag.get();
        return value == null ? defaultValue : value;
    }

    public void set(boolean value) {
        flag.set(value);
    }

}
