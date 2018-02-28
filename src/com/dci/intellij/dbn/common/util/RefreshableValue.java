package com.dci.intellij.dbn.common.util;

public abstract class RefreshableValue<T>{
    private T value;
    private boolean loaded = false;
    private int refreshInterval;
    private long lastRefreshTimestamp;

    public RefreshableValue(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public T get() {
        if (!loaded || lastRefreshTimestamp < System.currentTimeMillis() - refreshInterval) {
            value = CommonUtil.nvln(load(), value);
            loaded = true;
            lastRefreshTimestamp = System.currentTimeMillis();
        }
        return value;
    }

    protected abstract T load();
}
