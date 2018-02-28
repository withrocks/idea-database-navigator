package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.tree.TreePath;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.execution.method.MethodExecutionMessage;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;

public class MethodExecutionMessagesObjectNode extends BundleTreeNode {
    private DBEditableObjectVirtualFile databaseFile;

    public MethodExecutionMessagesObjectNode(MethodExecutionMessagesNode parent, DBEditableObjectVirtualFile databaseFile) {
        super(parent);
        this.databaseFile = databaseFile;
    }

    public DBEditableObjectVirtualFile getVirtualFile() {
        return databaseFile;
    }

    public DBSchemaObject getObject() {
        return databaseFile.getObject();
    }

    public TreePath addCompilerMessage(MethodExecutionMessage executionMessage) {
        clearChildren();
        MethodExecutionMessageNode messageNode = new MethodExecutionMessageNode(this, executionMessage);
        addChild(messageNode);

        TreePath treePath = TreeUtil.createTreePath(this);
        getTreeModel().notifyTreeModelListeners(treePath, TreeEventType.STRUCTURE_CHANGED);
        return treePath;
    }

    public TreePath getTreePath(MethodExecutionMessage executionMessage) {
        for (MessagesTreeNode messageNode : getChildren()) {
            MethodExecutionMessageNode methodExecutionMessageNode = (MethodExecutionMessageNode) messageNode;
            if (methodExecutionMessageNode.getExecutionMessage() == executionMessage) {
                return TreeUtil.createTreePath(methodExecutionMessageNode);
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
        databaseFile = null;
    }
}
