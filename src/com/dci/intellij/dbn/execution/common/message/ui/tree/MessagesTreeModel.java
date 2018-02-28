package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;


public class MessagesTreeModel implements TreeModel, Disposable {
    private Set<TreeModelListener> treeModelListeners = new HashSet<TreeModelListener>();
    private RootNode rootNode = new RootNode(this);

    public MessagesTreeModel() {
        Disposer.register(this, rootNode);
    }

    public TreePath addExecutionMessage(StatementExecutionMessage executionMessage) {
        return rootNode.addExecutionMessage(executionMessage);
    }

    public TreePath addCompilerMessage(CompilerMessage compilerMessage) {
        return rootNode.addCompilerMessage(compilerMessage);
    }

    public TreePath addExplainPlanMessage(ExplainPlanMessage explainPlanMessage) {
        return rootNode.addExplainPlanMessage(explainPlanMessage);
    }

    @Nullable
    public TreePath getTreePath(CompilerMessage compilerMessage) {
        return rootNode.getTreePath(compilerMessage);
    }

    @Nullable
    public TreePath getTreePath(StatementExecutionMessage statementExecutionMessage) {
        return rootNode.getTreePath(statementExecutionMessage);
    }


    public void notifyTreeModelListeners(TreePath treePath, TreeEventType eventType) {
        TreeUtil.notifyTreeModelListeners(this, treeModelListeners, treePath, eventType);
    }
    public void notifyTreeModelListeners(TreeNode node, TreeEventType eventType) {
        TreePath treePath = TreeUtil.createTreePath(node);
        notifyTreeModelListeners(treePath, eventType);
    }

    public void dispose() {
        treeModelListeners.clear();
        rootNode = null;
    }

   /*********************************************************
    *                       TreeModel                      *
    *********************************************************/
    public Object getRoot() {
        return rootNode;
    }

    public Object getChild(Object o, int i) {
        TreeNode treeNode = (TreeNode) o;
        return treeNode.getChildAt(i);
    }

    public int getChildCount(Object o) {
        TreeNode treeNode = (TreeNode) o;
        return treeNode.getChildCount();
    }

    public boolean isLeaf(Object o) {
        TreeNode treeNode = (TreeNode) o;
        return treeNode.isLeaf();
    }

    public void valueForPathChanged(TreePath treePath, Object o) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getIndexOfChild(Object o, Object o1) {
        TreeNode treeNode = (TreeNode) o;
        TreeNode childTreeNode = (TreeNode) o1;
        return treeNode.getIndex(childTreeNode);
    }

    public void addTreeModelListener(TreeModelListener treeModelListener) {
        treeModelListeners.add(treeModelListener);
    }

    public void removeTreeModelListener(TreeModelListener treeModelListener) {
        treeModelListeners.remove(treeModelListener);
    }
}
