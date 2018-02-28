package com.dci.intellij.dbn.editor.ddl;

import com.dci.intellij.dbn.common.editor.BasicTextEditorImpl;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class DDLFileEditor extends BasicTextEditorImpl {
    public DDLFileEditor(Project project, VirtualFile virtualFile, EditorProviderId editorProviderId) {
        super(project, virtualFile, virtualFile.getName(), editorProviderId);
    }

}