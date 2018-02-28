package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.ApplicationManager;

public abstract class SimpleLaterInvocator extends SynchronizedTask{

    public SimpleLaterInvocator() {
        super(null);
    }

    public SimpleLaterInvocator(Object syncObject) {
        super(syncObject);
    }

    public void start() {
        ApplicationManager.getApplication().invokeLater(this/*, ModalityState.NON_MODAL*/);
    }
}
