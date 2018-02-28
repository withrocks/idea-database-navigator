package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;

public abstract class ConditionalReadActionRunner<T> {

    public final T start() {
        Application application = ApplicationManager.getApplication();
        if (application.isReadAccessAllowed()) {
            return run();
        } else {
            Computable<T> readAction = new Computable<T>() {
                @Override
                public T compute() {
                    return ConditionalReadActionRunner.this.run();
                }
            };
            return ApplicationManager.getApplication().runReadAction(readAction);
        }
    }

    protected abstract T run();

}
