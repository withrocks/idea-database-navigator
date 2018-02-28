package com.dci.intellij.dbn.connection.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAwareAction;

public class OpenSQLConsoleAction extends DumbAwareAction {
    private ConnectionHandler connectionHandler;

    public OpenSQLConsoleAction(ConnectionHandler connectionHandler) {
        super("Open SQL Console", null, Icons.FILE_SQL_CONSOLE);
        this.connectionHandler = connectionHandler;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(connectionHandler.getProject());
        fileEditorManager.openFile(connectionHandler.getConsoleBundle().getDefaultConsole(), true);
    }
}
