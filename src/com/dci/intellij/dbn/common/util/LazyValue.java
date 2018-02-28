package com.dci.intellij.dbn.common.util;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;

public abstract class LazyValue<T> implements Disposable{

    private T value;
    private boolean loaded = false;
    private boolean disposed = false;
    public final synchronized T get(){
        if (!loaded && !disposed) {
            value = load();
            loaded = true;
        }
        return value;
    }

    public final void set(T value) {
        this.value = value;
        loaded = value != null;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public final void reset() {
        set(null);
    }

    protected abstract T load();

    @Override
    public void dispose() {
        disposed = true;
        if (value instanceof Disposable) {
            Disposer.dispose((Disposable) value);
        }
    }
}
