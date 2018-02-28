package com.dci.intellij.dbn.common.ui.tree;

import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class FolderTreeNode extends LeafTreeNode{
    List<TreeNode> children = new ArrayList<TreeNode>();

    public FolderTreeNode(TreeNode parent, Object userObject, List<TreeNode> children) {
        super(parent, userObject);
        this.children = children;
    }

    public void addChild(TreeNode child) {
        children.add(child);           
    }

    public TreeNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    public int getChildCount() {
        return children.size();
    }

    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public boolean isLeaf() {
        return false;
    }

    public Enumeration children() {
        final Iterator iterator = children.iterator();
        return new Enumeration() {
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            public Object nextElement() {
                return iterator.next();
            }
        };
    }
}
