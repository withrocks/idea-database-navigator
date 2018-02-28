package com.dci.intellij.dbn.connection.config.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.ui.ListUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.JList;

public class MoveConnectionDownAction extends DumbAwareAction {
    private JList list;
    private ConnectionBundleSettings connectionBundleSettings;

    public MoveConnectionDownAction(JList list, ConnectionBundleSettings connectionBundleSettings) {
        super("Move selection down", null, Icons.ACTION_MOVE_DOWN);
        this.list = list;
        this.connectionBundleSettings = connectionBundleSettings;
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        connectionBundleSettings.setModified(true);
        ListUtil.moveSelectedItemsDown(list);
    }

    public void update(@NotNull AnActionEvent e) {
        int length = list.getSelectedValues().length;
        boolean enabled = length > 0 && list.getMaxSelectionIndex() < list.getModel().getSize() - 1;
        e.getPresentation().setEnabled(enabled);
    }
}
