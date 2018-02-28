package com.dci.intellij.dbn.editor.console;

import com.dci.intellij.dbn.common.editor.BasicTextEditorImpl;
import com.dci.intellij.dbn.common.editor.BasicTextEditorState;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.project.Project;

public class SQLConsoleEditor extends BasicTextEditorImpl<DBConsoleVirtualFile>{
    public SQLConsoleEditor(Project project, DBConsoleVirtualFile sqlConsoleFile, String name, EditorProviderId editorProviderId) {
        super(project, sqlConsoleFile, name, editorProviderId);
    }

    @Override
    protected BasicTextEditorState createEditorState() {
        return new SQLConsoleEditorState();
    }

}
