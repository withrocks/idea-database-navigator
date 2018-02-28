package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.execution.method.MethodExecutionMessage;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.tree.TreePath;

public class MethodExecutionMessagesNode extends BundleTreeNode {
    public MethodExecutionMessagesNode(RootNode parent) {
        super(parent);
    }

    public MessagesTreeNode getChildTreeNode(VirtualFile virtualFile) {
        for (MessagesTreeNode messagesTreeNode : getChildren()) {
            if (messagesTreeNode.getVirtualFile().equals(virtualFile)) {
                return messagesTreeNode;
            }
        }
        return null;
    }

    public TreePath addExecutionMessage(MethodExecutionMessage executionMessage) {
        MethodExecutionMessagesObjectNode objectNode = (MethodExecutionMessagesObjectNode)
                getChildTreeNode(executionMessage.getContentFile());
        if (objectNode == null) {
            objectNode = new MethodExecutionMessagesObjectNode(this, executionMessage.getDatabaseFile());
            addChild(objectNode);
            getTreeModel().notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        }
        return objectNode.addCompilerMessage(executionMessage);
    }

    public TreePath getTreePath(MethodExecutionMessage executionMessage) {
        MethodExecutionMessagesObjectNode objectNode = (MethodExecutionMessagesObjectNode)
                getChildTreeNode(executionMessage.getDatabaseFile());
        return objectNode.getTreePath(executionMessage);
    }

    public VirtualFile getVirtualFile() {
        return null;
    }
}