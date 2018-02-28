package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.connection.ConnectionHandler;

public class BasicDependencyAdapter implements ContentDependencyAdapter {

    @Override
    public boolean canConnect(ConnectionHandler connectionHandler) {
        return connectionHandler != null && connectionHandler.canConnect() && connectionHandler.isValid();
    }

    public boolean canLoad(ConnectionHandler connectionHandler) {
        //should reload if connection is valid
        return canConnect(connectionHandler);
    }

    public boolean isDirty() {
        return false;
    }

    public void beforeLoad() {
        // nothing to do before load
    }

    public void afterLoad() {
        // nothing to do after load
    }

    public void beforeReload(DynamicContent dynamicContent) {

    }

    public void afterReload(DynamicContent dynamicContent) {

    }

    public boolean canLoadFast() {
        return false;
    }

    @Override
    public boolean isSubContent() {
        return false;
    }

    public void dispose() {
    }
}
