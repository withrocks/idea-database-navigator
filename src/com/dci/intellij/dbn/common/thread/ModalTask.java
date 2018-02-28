package com.dci.intellij.dbn.common.thread;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;

public abstract class ModalTask<T> extends Task.Modal implements RunnableTask<T>{
    private T result;

    public ModalTask(Project project, String title, boolean canBeCancelled) {
        super(project, title, canBeCancelled);
    }

    public ModalTask(Project project, String title) {
        super(project, title, false);
    }

    @Override
    public void setResult(T result) {
        this.result = result;
    }

    @Override
    public T getResult() {
        return result;
    }

    @Override
    public final void run() {
        ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
        run(progressIndicator);
    }

    @Override
    public final void run(@NotNull ProgressIndicator progressIndicator) {
        try {
            progressIndicator.pushState();
            progressIndicator.setIndeterminate(true);
            execute(progressIndicator);
        } finally {
            progressIndicator.popState();
        }
    }

    protected abstract void execute(@NotNull ProgressIndicator progressIndicator);

    public void start() {
        final ProgressManager progressManager = ProgressManager.getInstance();
        Application application = ApplicationManager.getApplication();

        if (application.isDispatchThread()) {
            progressManager.run(ModalTask.this);
        } else {
            Runnable runnable = new Runnable() {
                public void run() {
                    progressManager.run(ModalTask.this);
                }
            };
            application.invokeLater(runnable, ModalityState.NON_MODAL);
        }
    }
}
