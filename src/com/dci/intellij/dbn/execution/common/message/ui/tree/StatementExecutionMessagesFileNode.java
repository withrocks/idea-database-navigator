package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;

public class StatementExecutionMessagesFileNode extends BundleTreeNode {
    private VirtualFile virtualFile;

    public StatementExecutionMessagesFileNode(StatementExecutionMessagesNode parent, VirtualFile virtualFile) {
        super(parent);
        this.virtualFile = virtualFile;
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    public TreePath addExecutionMessage(StatementExecutionMessage executionMessage) {
        StatementExecutionMessageNode execMessageNode = new StatementExecutionMessageNode(this, executionMessage);
        addChild(execMessageNode);
        getTreeModel().notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        return TreeUtil.createTreePath(execMessageNode);
    }

    @Nullable
    public TreePath getTreePath(StatementExecutionMessage executionMessage) {
        for (MessagesTreeNode messageNode : getChildren()) {
            StatementExecutionMessageNode executionMessageNode = (StatementExecutionMessageNode) messageNode;
            if (executionMessageNode.getExecutionMessage() == executionMessage) {
                return TreeUtil.createTreePath(executionMessageNode);
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
        virtualFile = null;
    }
}
