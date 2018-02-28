package com.dci.intellij.dbn.browser.model;

public class EmptyBrowserTreeModel extends BrowserTreeModel {
    public EmptyBrowserTreeModel() {
        super(null);
    }

    @Override
    public boolean contains(BrowserTreeNode node) {
        return true;
    }

    public void dispose() {
        super.dispose();
    }
}
