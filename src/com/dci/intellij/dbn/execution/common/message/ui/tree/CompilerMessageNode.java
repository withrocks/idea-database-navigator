package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;

import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.vfs.DBContentVirtualFile;
import com.intellij.openapi.util.Disposer;

public class CompilerMessageNode implements MessagesTreeNode {
    private CompilerMessage compilerMessage;
    private CompilerMessagesObjectNode parent;

    public CompilerMessageNode(CompilerMessagesObjectNode parent, CompilerMessage compilerMessage) {
        this.parent = parent;
        this.compilerMessage = compilerMessage;

        Disposer.register(this, compilerMessage);
    }

    public CompilerMessage getCompilerMessage() {
        return compilerMessage;
    }

    public DBContentVirtualFile getVirtualFile() {
        return compilerMessage.getContentFile();
    }

    public void dispose() {
        compilerMessage = null;
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
        return "[" + compilerMessage.getType() + "] " + compilerMessage.getText();
    }
}