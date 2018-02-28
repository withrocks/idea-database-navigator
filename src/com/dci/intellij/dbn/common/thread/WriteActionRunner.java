package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.ApplicationManager;

public abstract class WriteActionRunner {

    public final void start() {
        new ConditionalLaterInvocator() {
            @Override
            public void execute() {
                Runnable writeAction = new Runnable() {
                    public void run() {
                        WriteActionRunner.this.run();
                    }
                };
                ApplicationManager.getApplication().runWriteAction(writeAction);
            }
        }.start();
    }

    public abstract void run();

}
