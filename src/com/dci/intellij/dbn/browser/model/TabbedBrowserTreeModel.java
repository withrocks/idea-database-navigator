package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.connection.ConnectionHandler;

public class TabbedBrowserTreeModel extends BrowserTreeModel {
    public TabbedBrowserTreeModel(ConnectionHandler connectionHandler) {
        super(connectionHandler.getObjectBundle());
    }

    @Override
    public boolean contains(BrowserTreeNode node) {
        return getConnectionHandler() == node.getConnectionHandler();
    }

    public ConnectionHandler getConnectionHandler() {
        return getRoot().getConnectionHandler();
    }
}
