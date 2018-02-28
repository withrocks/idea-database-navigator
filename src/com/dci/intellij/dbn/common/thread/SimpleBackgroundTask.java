package com.dci.intellij.dbn.common.thread;

public abstract class SimpleBackgroundTask extends SynchronizedTask{
    String name;

    public SimpleBackgroundTask(String name) {
        super(null);
        this.name = name;
    }

    public SimpleBackgroundTask(Object syncObject) {
        super(syncObject);
    }

    public final void start() {
        Thread thread = new Thread(this, "DBN Background Thread: " + name);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
}
