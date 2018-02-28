package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.tree.TreeNode;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.vfs.VirtualFile;

interface MessagesTreeNode extends TreeNode, Disposable {
    MessagesTreeModel getTreeModel();
    VirtualFile getVirtualFile();
}
