package com.dci.intellij.dbn.editor.session;

import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.vfs.DBSessionBrowserVirtualFile;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SessionBrowserEditorProvider implements FileEditorProvider, ApplicationComponent, DumbAware {
    /*********************************************************
     *                  FileEditorProvider                   *
     *********************************************************/

    public boolean accept(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return virtualFile instanceof DBSessionBrowserVirtualFile;
    }

    @NotNull
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        DBSessionBrowserVirtualFile sessionBrowserFile = (DBSessionBrowserVirtualFile) file;
        return new SessionBrowser(sessionBrowserFile);
    }

    public void disposeEditor(@NotNull final FileEditor editor) {
        Disposer.dispose(editor);
    }

    @NotNull
    public FileEditorState readState(@NotNull Element sourceElement, @NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (virtualFile instanceof DBSessionBrowserVirtualFile) {
            SessionBrowserState sessionBrowserState = new SessionBrowserState();
            sessionBrowserState.readState(sourceElement);
            return sessionBrowserState;
        }
        return new SessionBrowserState();
    }

    public void writeState(@NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement) {
        if (state instanceof SessionBrowserState) {
            SessionBrowserState editorState = (SessionBrowserState) state;
            editorState.writeState(targetElement);
        }
    }

    @NotNull
    @NonNls
    public String getEditorTypeId() {
        return EditorProviderId.SESSION_BROWSER.getId();
    }

    @NotNull
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }

    /*********************************************************
     *                ApplicationComponent                   *
     *********************************************************/
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.SessionBrowserProvider";
    }

    public void initComponent() {

    }

    public void disposeComponent() {

    }
}

