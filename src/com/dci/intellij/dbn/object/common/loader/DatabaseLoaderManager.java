package com.dci.intellij.dbn.object.common.loader;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionLoadListener;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

public class DatabaseLoaderManager extends AbstractProjectComponent {
    private DatabaseLoaderQueue loaderQueue;

    private DatabaseLoaderManager(final Project project) {
        super(project);
        EventManager.subscribe(project, ConnectionLoadListener.TOPIC, new ConnectionLoadListener() {
            @Override
            public void contentsLoaded(final ConnectionHandler connectionHandler) {
                new SimpleLaterInvocator() {
                    @Override
                    public void execute() {
                        if (!project.isDisposed()) {
                            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
                            VirtualFile[] openFiles = fileEditorManager.getOpenFiles();
                            for (VirtualFile openFile : openFiles) {

                                ConnectionHandler activeConnection = connectionMappingManager.getActiveConnection(openFile);
                                if (activeConnection == connectionHandler) {
                                    FileEditor[] fileEditors = fileEditorManager.getEditors(openFile);
                                    for (FileEditor fileEditor : fileEditors) {
                                        Editor editor = EditorUtil.getEditor(fileEditor);

                                        if (editor != null) {
                                            DocumentUtil.refreshEditorAnnotations(editor);
                                        }

                                    }

                                }
                            }
                        }
                    }
                }.start();

            }
        });
    }

    public static DatabaseLoaderManager getInstance(Project project) {
        return project.getComponent(DatabaseLoaderManager.class);
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DatabaseLoaderManager";
    }

    public void disposeComponent() {
        super.disposeComponent();
        if (loaderQueue != null) {
            Disposer.dispose(loaderQueue);
            loaderQueue = null;
        }
    }
}
