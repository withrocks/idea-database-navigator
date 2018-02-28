package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.tree.TreePath;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.intellij.openapi.vfs.VirtualFile;

public class ExplainPlanMessagesFileNode extends BundleTreeNode {
    private VirtualFile virtualFile;

    public ExplainPlanMessagesFileNode(ExplainPlanMessagesNode parent, VirtualFile virtualFile) {
        super(parent);
        this.virtualFile = virtualFile;
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    public TreePath addExplainPlanMessage(ExplainPlanMessage explainPlanMessage) {
        ExplainPlanMessageNode explainPlanMessageNode = new ExplainPlanMessageNode(this, explainPlanMessage);
        addChild(explainPlanMessageNode);
        getTreeModel().notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        return TreeUtil.createTreePath(explainPlanMessageNode);
    }

    @Nullable
    public TreePath getTreePath(ExplainPlanMessage explainPlanMessage) {
        for (MessagesTreeNode messageNode : getChildren()) {
            ExplainPlanMessageNode explainPlanMessageNode = (ExplainPlanMessageNode) messageNode;
            if (explainPlanMessageNode.getExplainPlanMessage() == explainPlanMessage) {
                return TreeUtil.createTreePath(explainPlanMessageNode);
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
