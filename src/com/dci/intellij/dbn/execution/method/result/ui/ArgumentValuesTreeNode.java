package com.dci.intellij.dbn.execution.method.result.ui;

import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.dci.intellij.dbn.execution.method.ArgumentValue;
import com.dci.intellij.dbn.object.common.DBObject;

public class ArgumentValuesTreeNode implements TreeNode{
    private Object userValue;
    private ArgumentValuesTreeNode parent;
    private List<ArgumentValuesTreeNode> children = new ArrayList<ArgumentValuesTreeNode>();

    protected ArgumentValuesTreeNode(ArgumentValuesTreeNode parent, Object userValue) {
        this.parent = parent;
        if (parent != null) {
            parent.children.add(this);
        }
        this.userValue = userValue;
    }

    public Object getUserValue() {
        return userValue;
    }

    public List<ArgumentValuesTreeNode> getChildren() {
        return children;
    }

    public void dispose() {
        for (ArgumentValuesTreeNode treeNode : children) {
            treeNode.dispose();
        }
    }

    @Override
    public String toString() {
        if (userValue instanceof ArgumentValue) {
            ArgumentValue argumentValue = (ArgumentValue) userValue;
            return String.valueOf(argumentValue.getValue());
        }

        if (userValue instanceof DBObject) {
            DBObject object = (DBObject) userValue;
            return object.getName();
        }

        return userValue.toString();
    }

    /*********************************************************
     *                        TreeNode                       *
     *********************************************************/
    public TreeNode getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    public int getChildCount() {
        return children.size();
    }

    public TreeNode getParent() {
        return parent;
    }

    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public Enumeration children() {
        return Collections.enumeration(children);
    }
}
