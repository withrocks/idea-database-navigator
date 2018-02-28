package com.dci.intellij.dbn.object.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class EditObjectDataAction extends AnAction {
    private DBSchemaObject object;
    public EditObjectDataAction(DBSchemaObject object) {
        super("Edit Data", null, Icons.OBEJCT_EDIT_DATA);
        this.object = object;
        setDefaultIcon(true);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DatabaseFileSystem.getInstance().openEditor(object, EditorProviderId.DATA, true);
    }
}