package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;

public abstract class CommandWriteActionRunner {
    private Project project;
    private String commandName;

    public CommandWriteActionRunner(Project project, String commandName) {
        this.project = project;
        this.commandName = commandName;
    }

    protected CommandWriteActionRunner(Project project) {
        this.project = project;
        this.commandName = "";
    }

    public final void start() {
        Runnable command = new Runnable() {
            public void run() {
                Runnable writeAction = new Runnable() {
                    public void run() {
                        CommandWriteActionRunner.this.run();
                    }
                };
                ApplicationManager.getApplication().runWriteAction(writeAction);
            }
        };
        CommandProcessor.getInstance().executeCommand(project, command, commandName, null);
    }

    public abstract void run();

}
