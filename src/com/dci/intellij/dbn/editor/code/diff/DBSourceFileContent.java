package com.dci.intellij.dbn.editor.code.diff;

import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.intellij.openapi.diff.FileContent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class DBSourceFileContent extends FileContent {
    private Document document;
    public DBSourceFileContent(Project project, @NotNull VirtualFile file) {
        super(project, file);
    }

    public Document getDocument() {
        if (document == null) {
            document = DocumentUtil.getDocument(getFile());
        }
        return document; 
    }

    @Override
    public FileType getContentType() {
        return getFile().getFileType();
    }
}
