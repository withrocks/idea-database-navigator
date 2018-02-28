package com.dci.intellij.dbn.browser.ui;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.intellij.openapi.Disposable;
import com.intellij.ui.SpeedSearchBase;

public class DatabaseBrowserTreeSpeedSearch extends SpeedSearchBase<JTree> implements Disposable {
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private Object[] elements = null;

    public DatabaseBrowserTreeSpeedSearch(DatabaseBrowserTree tree) {
        super(tree);
        getComponent().getModel().addTreeModelListener(treeModelListener);
    }

    protected int getSelectedIndex() {
        Object[] elements = getAllElements();
        BrowserTreeNode treeNode = getSelectedTreeElement();
        if (treeNode != null) {
            for (int i=0; i<elements.length; i++) {
                if (treeNode == elements[i]) {
                    return i;
                }
            }
        }
        return -1;
    }

    private BrowserTreeNode getSelectedTreeElement() {
        TreePath selectionPath = getComponent().getSelectionPath();
        if (selectionPath != null) {
            return (BrowserTreeNode) selectionPath.getLastPathComponent();
        }
        return null;
    }

    protected Object[] getAllElements() {
        if (elements == null) {
            List<BrowserTreeNode> nodes = new ArrayList<BrowserTreeNode>();
            BrowserTreeNode root = getComponent().getModel().getRoot();
            loadElements(nodes, root);
            this.elements = nodes.toArray();
        }
        return elements;
    }

    private static void loadElements(List<BrowserTreeNode> nodes, BrowserTreeNode browserTreeNode) {
        if (browserTreeNode.isTreeStructureLoaded()) {
            if (browserTreeNode instanceof ConnectionBundle) {
                ConnectionBundle connectionBundle = (ConnectionBundle) browserTreeNode;
                for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()){
                    DBObjectBundle objectBundle = connectionHandler.getObjectBundle();
                    loadElements(nodes, objectBundle);
                }
            }
            else {
                for (BrowserTreeNode treeNode : browserTreeNode.getTreeChildren()) {
                    if (treeNode instanceof DBObject) {
                        nodes.add(treeNode);
                    }
                    loadElements(nodes, treeNode);
                }
            }
        }
    }

    @Override
    public DatabaseBrowserTree getComponent() {
        return (DatabaseBrowserTree) super.getComponent();
    }

    protected String getElementText(Object o) {
        BrowserTreeNode treeNode = (BrowserTreeNode) o;
        return treeNode.getPresentableText();
    }

    protected void selectElement(Object o, String s) {
        BrowserTreeNode treeNode = (BrowserTreeNode) o;
        getComponent().selectElement(treeNode, false);

/*
        TreePath treePath = DatabaseBrowserUtils.createTreePath(treeNode);
        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);
*/
    }

    TreeModelListener treeModelListener = new TreeModelListener() {

        public void treeNodesChanged(TreeModelEvent e) {
            elements = null;
        }

        public void treeNodesInserted(TreeModelEvent e) {
            elements = null;
        }

        public void treeNodesRemoved(TreeModelEvent e) {
            elements = null;
        }

        public void treeStructureChanged(TreeModelEvent e) {
            elements = null;
        }
    };

    @Override
    public void dispose() {
        getComponent().getModel().removeTreeModelListener(treeModelListener);
        elements = EMPTY_ARRAY;
        treeModelListener = null;
    }
}
