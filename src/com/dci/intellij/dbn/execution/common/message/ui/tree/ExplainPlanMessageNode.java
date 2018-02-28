package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;

import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

public class ExplainPlanMessageNode implements MessagesTreeNode {
    private ExplainPlanMessage explainPlanMessage;
    private ExplainPlanMessagesFileNode parent;

    public ExplainPlanMessageNode(ExplainPlanMessagesFileNode parent, ExplainPlanMessage explainPlanMessage) {
        this.parent = parent;
        this.explainPlanMessage = explainPlanMessage;

        Disposer.register(this, explainPlanMessage);
    }

    public ExplainPlanMessage getExplainPlanMessage() {
        return explainPlanMessage;
    }

    public VirtualFile getVirtualFile() {
        return parent.getVirtualFile();
    }

    public void dispose() {
        explainPlanMessage = null;
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
            explainPlanMessage.getText() + " - Connection: " +
            explainPlanMessage.getConnectionHandler().getName();
    }
}
