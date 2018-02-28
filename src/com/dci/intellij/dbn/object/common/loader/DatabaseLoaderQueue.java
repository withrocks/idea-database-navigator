package com.dci.intellij.dbn.object.common.loader;

import com.dci.intellij.dbn.common.Constants;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DatabaseLoaderQueue extends Task.Modal implements Disposable {
    boolean isActive = true;
    private List<Runnable> queue = new ArrayList<Runnable>();

    public DatabaseLoaderQueue(@org.jetbrains.annotations.Nullable Project project) {
        super(project, Constants.DBN_TITLE_PREFIX + "Loading data dictionary", false);
    }

    public void queue(Runnable task) {
        queue.add(task);
    }

    public void run(@NotNull ProgressIndicator indicator) {
        while (queue.size() > 0) {
            Runnable task = queue.remove(0);
            task.run();
        }
        isActive = false;
    }

    public boolean isActive() {
        return isActive;
    }

    public void dispose() {
        queue.clear();
    }
}
