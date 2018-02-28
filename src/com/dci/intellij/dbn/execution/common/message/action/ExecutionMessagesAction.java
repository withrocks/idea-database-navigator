package com.dci.intellij.dbn.execution.common.message.action;

import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTree;
import com.intellij.openapi.project.DumbAwareAction;

import javax.swing.*;

public abstract class ExecutionMessagesAction extends DumbAwareAction {
    private MessagesTree messagesTree;

    public ExecutionMessagesAction(MessagesTree messagesTree, String text, Icon icon) {
        super(text, null, icon);
        this.messagesTree = messagesTree;
    }

    public MessagesTree getMessagesTree() {
        return messagesTree;
    }
}
