package com.dci.intellij.dbn.common.util;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class DBNOpenFileDescriptor extends OpenFileDescriptor {
    private String editorProviderId;

    public DBNOpenFileDescriptor(Project project, @NotNull VirtualFile file, int offset) {
        super(project, file, offset);
    }

    public DBNOpenFileDescriptor(Project project, @NotNull VirtualFile file, int line, int col) {
        super(project, file, line, col);
    }

    public DBNOpenFileDescriptor(Project project, @NotNull VirtualFile file) {
        super(project, file);
    }
}
