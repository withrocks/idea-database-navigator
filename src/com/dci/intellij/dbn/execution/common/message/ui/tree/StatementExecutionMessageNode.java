package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;

public class StatementExecutionMessageNode implements MessagesTreeNode {
    private StatementExecutionMessage executionMessage;
    private StatementExecutionMessagesFileNode parent;

    public StatementExecutionMessageNode(StatementExecutionMessagesFileNode parent, StatementExecutionMessage executionMessage) {
        this.parent = parent;
        this.executionMessage = executionMessage;

        Disposer.register(this, executionMessage);
    }

    public StatementExecutionMessage getExecutionMessage() {
        return executionMessage;
    }

    public VirtualFile getVirtualFile() {
        return parent.getVirtualFile();
    }

    public void dispose() {
        executionMessage = null;
        parent = null;
    }

    public MessagesTreeModel getTreeModel() {
        return parent.getTreeModel();
    }

    /*********************************************************
     *                        TreeNode                       *
     *********************************************************/
    public TreeNode getChildAt(int childIndex) {
        return null;
    }

    public int getChildCount() {
        return 0;
    }

    public TreeNode getParent() {
        return parent;
    }

    public int getIndex(TreeNode node) {
        return -1;
    }

    public boolean getAllowsChildren() {
        return false;
    }

    public boolean isLeaf() {
        return true;
    }

    public Enumeration children() {
        return null;
    }

    @Override
    public String toString() {
        return
            executionMessage.getText() + " " +
            executionMessage.getCauseMessage() + " - Connection: " +
            executionMessage.getExecutionResult().getConnectionHandler().getName() + ": " +
            executionMessage.getExecutionResult().getExecutionDuration() + "ms";
    }
}
