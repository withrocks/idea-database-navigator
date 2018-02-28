package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.progress.ProcessCanceledException;

public abstract class SynchronizedTask extends SimpleTask {
    private final Object syncObject;

    public SynchronizedTask(Object syncObject) {
        this.syncObject = syncObject;
    }

    public void start() {
        run();
    }

    @Override
    public final void run() {
        if (syncObject == null) {
            try {
                execute();
            } catch (ProcessCanceledException e) {
                // do nothing
            }
        } else {
            synchronized (syncObject) {
                try {
                    execute();
                } catch (ProcessCanceledException e) {
                    // do nothing
                }
            }
        }
    }
    protected abstract void execute();
}
