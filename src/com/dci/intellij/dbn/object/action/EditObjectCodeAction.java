package com.dci.intellij.dbn.object.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class EditObjectCodeAction extends AnAction {
    private DBSchemaObject object;
    public EditObjectCodeAction(DBSchemaObject object) {
        super("Edit Code", null, Icons.OBEJCT_EDIT_SOURCE);
        this.object = object;
        setDefaultIcon(true);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DatabaseFileSystem.getInstance().openEditor(object, EditorProviderId.CODE, true);
    }
}
