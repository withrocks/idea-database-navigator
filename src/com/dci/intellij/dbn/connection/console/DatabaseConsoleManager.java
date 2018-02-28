package com.dci.intellij.dbn.connection.console;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.console.ui.CreateRenameConsoleDialog;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import com.intellij.util.EventDispatcher;

public class DatabaseConsoleManager extends AbstractProjectComponent {
    private final EventDispatcher<VirtualFileListener> eventDispatcher = EventDispatcher.create(VirtualFileListener.class);

    private DatabaseConsoleManager(final Project project) {
        super(project);
    }

    public static DatabaseConsoleManager getInstance(Project project) {
        return project.getComponent(DatabaseConsoleManager.class);
    }

    public void showCreateConsoleDialog(ConnectionHandler connectionHandler) {
        showCreateRenameConsoleDialog(connectionHandler, null);
    }

    public void showRenameConsoleDialog(@NotNull DBConsoleVirtualFile consoleVirtualFile) {
        showCreateRenameConsoleDialog(consoleVirtualFile.getConnectionHandler(), consoleVirtualFile);
    }


    private void showCreateRenameConsoleDialog(final ConnectionHandler connectionHandler, final DBConsoleVirtualFile consoleVirtualFile) {
        new ConditionalLaterInvocator() {
            @Override
            public void execute() {
                CreateRenameConsoleDialog createConsoleDialog = new CreateRenameConsoleDialog(connectionHandler, consoleVirtualFile);
                createConsoleDialog.setModal(true);
                createConsoleDialog.show();
            }
        }.start();
    }

    public void createConsole(ConnectionHandler connectionHandler, String name) {
        DBConsoleVirtualFile consoleFile = connectionHandler.getConsoleBundle().createConsole(name);
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(connectionHandler.getProject());
        fileEditorManager.openFile(consoleFile, true);
        eventDispatcher.getMulticaster().fileCreated(new VirtualFileEvent(this, consoleFile, name, null));
    }

    public void renameConsole(DBConsoleVirtualFile consoleFile, String newName) {
        ConnectionHandler connectionHandler = consoleFile.getConnectionHandler();
        String oldName = consoleFile.getName();
        connectionHandler.getConsoleBundle().renameConsole(oldName, newName);
        VirtualFilePropertyEvent event = new VirtualFilePropertyEvent(this, consoleFile, VirtualFile.PROP_NAME, oldName, newName);
        eventDispatcher.getMulticaster().propertyChanged(event);
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DatabaseConsoleManager";
    }

    public void disposeComponent() {
        super.disposeComponent();
    }

    public void deleteConsole(final DBConsoleVirtualFile consoleFile) {
        final Project project = getProject();
        SimpleTask deleteTask = new SimpleTask() {
            @Override
            public void execute() {
                if (getResult() == 0) {
                    FileEditorManager.getInstance(project).closeFile(consoleFile);
                    ConnectionHandler connectionHandler = consoleFile.getConnectionHandler();
                    String fileName = consoleFile.getName();
                    connectionHandler.getConsoleBundle().removeConsole(fileName);
                    eventDispatcher.getMulticaster().fileDeleted(new VirtualFileEvent(this, consoleFile, fileName, null));
                }
            }
        };
        MessageUtil.showQuestionDialog(
                project,
                "Delete Console",
                "You will loose the information contained in this console.\n" +
                        "Are you sure you want to delete the console?",
                MessageUtil.OPTIONS_YES_NO, 0, deleteTask);
    }
}
