package com.dci.intellij.dbn.common.editor;

import com.dci.intellij.dbn.editor.EditorProviderId;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;

public interface BasicTextEditor<T extends VirtualFile> extends FileEditor {
    Project getProject();

    @NotNull
    Editor getEditor();

    T getVirtualFile();

    boolean canNavigateTo(@NotNull final Navigatable navigatable);

    void navigateTo(@NotNull final Navigatable navigatable);

    EditorProviderId getEditorProviderId();
}
