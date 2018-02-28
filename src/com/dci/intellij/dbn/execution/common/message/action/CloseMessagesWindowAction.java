package com.dci.intellij.dbn.execution.common.message.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTree;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class CloseMessagesWindowAction extends ExecutionMessagesAction {
    public CloseMessagesWindowAction(MessagesTree messagesTree) {
        super(messagesTree, "Close", Icons.EXEC_RESULT_CLOSE);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            ExecutionManager.getInstance(project).removeMessagesTab();
        }
    }
}
