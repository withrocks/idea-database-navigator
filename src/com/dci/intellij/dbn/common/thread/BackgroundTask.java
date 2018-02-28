package com.dci.intellij.dbn.common.thread;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;

public abstract class BackgroundTask<T> extends Task.Backgroundable implements RunnableTask<T> {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private T result;

    private static PerformInBackgroundOption START_IN_BACKGROUND = new PerformInBackgroundOption() {
        public boolean shouldStartInBackground() { return true;}
        public void processSentToBackground() {}
    };

    private static PerformInBackgroundOption DO_NOT_START_IN_BACKGROUND = new PerformInBackgroundOption() {
        public boolean shouldStartInBackground() { return false;}
        public void processSentToBackground() {}
    };

    public BackgroundTask(@Nullable Project project, @NotNull String title, boolean startInBackground, boolean canBeCancelled) {
        super(project, Constants.DBN_TITLE_PREFIX + title, canBeCancelled, startInBackground ? START_IN_BACKGROUND : DO_NOT_START_IN_BACKGROUND);
    }

    public BackgroundTask(@Nullable Project project, @NotNull String title, boolean startInBackground) {
        this(project, title, startInBackground, false);
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

    public final void run(@NotNull ProgressIndicator progressIndicator) {
        int priority = Thread.currentThread().getPriority();
        try {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            initProgressIndicator(progressIndicator, true);
            progressIndicator.pushState();

            execute(progressIndicator);
        } catch (ProcessCanceledException e) {
            // no action required here
            LOGGER.info(getTitle() + " interrupted");
        } catch (InterruptedException e) {
            // no action required here
            LOGGER.info(getTitle() + " interrupted");
        } catch (Exception e) {
            LOGGER.error("Error executing background operation.", e);
        } finally {
            progressIndicator.popState();
            Thread.currentThread().setPriority(priority);
            /*if (progressIndicator.isRunning()) {
                progressIndicator.stop();
            }*/
        }
    }

    protected abstract void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException;

    public final void start() {
        final ProgressManager progressManager = ProgressManager.getInstance();
        final BackgroundTask task = BackgroundTask.this;
        Application application = ApplicationManager.getApplication();

        if (application.isDispatchThread()) {
            progressManager.run(task);
        } else {
            Runnable runnable = new Runnable() {
                public void run() {
                    progressManager.run(task);
                }
            };
            application.invokeLater(runnable, ModalityState.NON_MODAL);
        }
    }

    public static void initProgressIndicator(final ProgressIndicator progressIndicator, final boolean indeterminate) {
        initProgressIndicator(progressIndicator, indeterminate, null);
    }

    public static void initProgressIndicator(final ProgressIndicator progressIndicator, final boolean indeterminate, @Nullable final String text) {
        new ConditionalLaterInvocator() {
            @Override
            public void execute() {
                if (progressIndicator.isRunning()) {
                    progressIndicator.setIndeterminate(indeterminate);
                    if (text != null) progressIndicator.setText(text);
                }
            }
        }.start();
    }

    public static boolean isProcessCancelled() {
        ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
        return progressIndicator != null && progressIndicator.isCanceled();
    }

}
