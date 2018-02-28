package com.dci.intellij.dbn.object.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.object.DBView;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class OpenViewDataAction extends AnAction {
    private DBView view;

    public OpenViewDataAction(DBView view) {
        super("View Data", null, Icons.OBEJCT_VIEW_DATA);
        this.view = view;
        setDefaultIcon(true);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DatabaseFileSystem.getInstance().openEditor(view, EditorProviderId.DATA, true);
    }
}