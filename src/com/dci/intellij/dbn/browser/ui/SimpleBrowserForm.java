package com.dci.intellij.dbn.browser.ui;

import com.dci.intellij.dbn.browser.model.BrowserTreeModel;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.SimpleBrowserTreeModel;
import com.dci.intellij.dbn.browser.model.TabbedBrowserTreeModel;
import com.dci.intellij.dbn.browser.options.listener.ObjectDetailSettingsListener;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.IncorrectOperationException;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;

public class SimpleBrowserForm extends DatabaseBrowserForm{
    private JPanel mainPanel;
    private JScrollPane browserScrollPane;
    private DatabaseBrowserTree browserTree;

    public SimpleBrowserForm(DisposableProjectComponent parentComponent) {
        this(parentComponent, new SimpleBrowserTreeModel(parentComponent.getProject(), ConnectionManager.getInstance(parentComponent.getProject()).getConnectionBundle()));
    }

    public SimpleBrowserForm(DisposableProjectComponent parentComponent, ConnectionHandler connectionHandler) {
        this(parentComponent, new TabbedBrowserTreeModel(connectionHandler));
    }

    private SimpleBrowserForm(DisposableProjectComponent parentComponent, BrowserTreeModel treeModel) {
        super(parentComponent);
        browserTree = new DatabaseBrowserTree(treeModel);
        browserScrollPane.setViewportView(browserTree);
        browserScrollPane.setBorder(new EmptyBorder(1,0,0,0));
        ToolTipManager.sharedInstance().registerComponent(browserTree);

        EventManager.subscribe(getProject(), ObjectDetailSettingsListener.TOPIC, objectDetailSettingsListener);
        Disposer.register(this, browserTree);
    }
    
    public ConnectionHandler getConnectionHandler(){
        if (browserTree.getModel() instanceof TabbedBrowserTreeModel) {
            TabbedBrowserTreeModel treeModel = (TabbedBrowserTreeModel) browserTree.getModel();
            return treeModel.getConnectionHandler();
        }
        throw new IncorrectOperationException("Multiple connection tabs can not return one connection.");
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public DatabaseBrowserTree getBrowserTree() {
        return browserTree;
    }

    public void selectElement(BrowserTreeNode treeNode, boolean requestFocus) {
        browserTree.selectElement(treeNode, requestFocus);
    }

    public void rebuildTree() {
        browserTree.getModel().getRoot().rebuildTreeChildren();
    }

    public void rebuild() {
        BrowserTreeModel treeModel = browserTree.getModel();
        BrowserTreeNode rootNode = treeModel.getRoot();
        treeModel.notifyListeners(rootNode, TreeEventType.STRUCTURE_CHANGED);
    }

    public void dispose() {
        super.dispose();
        EventManager.unsubscribe(objectDetailSettingsListener);
    }

    /********************************************************
     *                       Listeners                      *
     ********************************************************/
    private ObjectDetailSettingsListener objectDetailSettingsListener = new ObjectDetailSettingsListener() {
        @Override
        public void displayDetailsChanged() {
            browserTree.revalidate();
            browserTree.repaint();
        }
    };
}
