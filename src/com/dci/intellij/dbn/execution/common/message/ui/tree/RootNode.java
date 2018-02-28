package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.intellij.openapi.vfs.VirtualFile;

public class RootNode extends BundleTreeNode {
    private MessagesTreeModel messagesTreeModel;

    public RootNode(MessagesTreeModel messagesTreeModel) {
        super(null);
        this.messagesTreeModel = messagesTreeModel;
    }

    public TreePath addExecutionMessage(StatementExecutionMessage executionMessage) {
        StatementExecutionMessagesNode execMessagesNode = null;
        for (TreeNode treeNode : getChildren()) {
            if (treeNode instanceof StatementExecutionMessagesNode) {
                execMessagesNode = (StatementExecutionMessagesNode) treeNode;
                break;
            }
        }
        if (execMessagesNode == null) {
            execMessagesNode = new StatementExecutionMessagesNode(this);
            addChild(execMessagesNode);
            messagesTreeModel.notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        }

        return execMessagesNode.addExecutionMessage(executionMessage);
    }

    public TreePath addExplainPlanMessage(ExplainPlanMessage explainPlanMessage) {
        ExplainPlanMessagesNode explainPlanMessagesNode = null;
        for (TreeNode treeNode : getChildren()) {
            if (treeNode instanceof ExplainPlanMessagesNode) {
                explainPlanMessagesNode = (ExplainPlanMessagesNode) treeNode;
                break;
            }
        }
        if (explainPlanMessagesNode == null) {
            explainPlanMessagesNode = new ExplainPlanMessagesNode(this);
            addChild(explainPlanMessagesNode);
            messagesTreeModel.notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        }

        return explainPlanMessagesNode.addExplainPlanMessage(explainPlanMessage);
    }

    public TreePath addCompilerMessage(CompilerMessage compilerMessage) {
        CompilerMessagesNode compilerMessagesNode = getCompilerMessagesNode();
        if (compilerMessagesNode == null) {
            compilerMessagesNode = new CompilerMessagesNode(this);
            addChild(compilerMessagesNode);
            messagesTreeModel.notifyTreeModelListeners(this, TreeEventType.STRUCTURE_CHANGED);
        }
        return compilerMessagesNode.addCompilerMessage(compilerMessage);
    }

    @Nullable
    public CompilerMessagesNode getCompilerMessagesNode() {
        for (TreeNode treeNode : getChildren()) {
            if (treeNode instanceof CompilerMessagesNode) {
                return (CompilerMessagesNode) treeNode;
            }
        }
        return null;
    }

    @Nullable
    public StatementExecutionMessagesNode getStatementExecutionMessagesNode() {
        for (TreeNode treeNode : getChildren()) {
            if (treeNode instanceof StatementExecutionMessagesNode) {
                return (StatementExecutionMessagesNode) treeNode;
            }
        }
        return null;
    }

    @Nullable
    public TreePath getTreePath(CompilerMessage compilerMessage) {
        CompilerMessagesNode compilerMessagesNode = getCompilerMessagesNode();
        if (compilerMessagesNode != null) {
            return compilerMessagesNode.getTreePath(compilerMessage);
        }
        return null;
    }

    @Nullable
    public TreePath getTreePath(StatementExecutionMessage executionMessage) {
        StatementExecutionMessagesNode executionMessagesNode = getStatementExecutionMessagesNode();
        if (executionMessagesNode != null) {
            return executionMessagesNode.getTreePath(executionMessage);
        }
        return null;
    }


    public MessagesTreeModel getTreeModel() {
        return messagesTreeModel;
    }

    public VirtualFile getVirtualFile() {
        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
        messagesTreeModel = null;
    }
}
