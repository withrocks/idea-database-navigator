package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;

public abstract class ReadActionRunner<T> {

    public final T start() {
        Computable<T> readAction = new Computable<T>() {
            @Override
            public T compute() {
                return ReadActionRunner.this.run();
            }
        };
        return ApplicationManager.getApplication().runReadAction(readAction);
    }

    protected abstract T run();

}
