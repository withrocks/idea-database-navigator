package com.dci.intellij.dbn.execution.common.message.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTree;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class CollapseMessagesTreeAction extends ExecutionMessagesAction {

    public CollapseMessagesTreeAction(MessagesTree messagesTree) {
        super(messagesTree, "Collapse All", Icons.ACTION_COLLAPSE_ALL);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        TreeUtil.collapseAll(getMessagesTree());
    }
}