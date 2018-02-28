package com.dci.intellij.dbn.object.common.list.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;

public class ReloadObjectsAction extends AnAction {

    private DBObjectList objectList;

    public ReloadObjectsAction(DBObjectList objectList) {
        super("Reload", null, Icons.ACTION_REFRESH);
        this.objectList = objectList;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        new ConnectionAction(objectList) {
            @Override
            public void execute() {
                new BackgroundTask(objectList.getProject(), "Reloading " + objectList.getContentDescription() + ".", false) {
                    @Override
                    public void execute(@NotNull final ProgressIndicator progressIndicator) throws InterruptedException {
                        objectList.reload();
                    }
                }.start();

            }
        }.start();
    }
}
