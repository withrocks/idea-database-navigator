package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.tree.TreePath;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.intellij.openapi.vfs.VirtualFile;

public class ExplainPlanMessagesNode extends BundleTreeNode {
    public ExplainPlanMessagesNode(RootNode parent) {
        super(parent);
    }

    @Nullable
    public MessagesTreeNode getChildTreeNode(VirtualFile virtualFile) {
        for (MessagesTreeNode messagesTreeNode : getChildren()) {
            if (messagesTreeNode.getVirtualFile().equals(virtualFile)) {
                return messagesTreeNode;
            }
        }
        return null;
    }

    public TreePath addExplainPlanMessage(ExplainPlanMessage explainPlanMessage) {
        ExplainPlanMessagesFileNode node = getFileTreeNode(explainPlanMessage);
        if (node == null) {
            node = new ExplainPlanMessagesFileNode(this, explainPlanMessage.getVirtualFile());
            addChild(node);
            getTreeModel().notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        }
        return node.addExplainPlanMessage(explainPlanMessage);
    }


    @Nullable
    public TreePath getTreePath(ExplainPlanMessage explainPlanMessage) {
        ExplainPlanMessagesFileNode messagesFileNode = getFileTreeNode(explainPlanMessage);
        if (messagesFileNode != null) {
            return messagesFileNode.getTreePath(explainPlanMessage);
        }
        return null;
    }

    @Nullable
    private ExplainPlanMessagesFileNode getFileTreeNode(ExplainPlanMessage explainPlanMessage) {
        return (ExplainPlanMessagesFileNode) getChildTreeNode(explainPlanMessage.getVirtualFile());
    }


    public VirtualFile getVirtualFile() {
        return null;
    }
}
