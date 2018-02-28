package com.dci.intellij.dbn.object.properties;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.pom.Navigatable;

import javax.swing.*;

public class ConnectionPresentableProperty extends PresentableProperty{
    private ConnectionHandler connectionHandler;

    public ConnectionPresentableProperty(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public String getName() {
        return "Connection";
    }

    public String getValue() {
        return connectionHandler.getName();
    }

    public Icon getIcon() {
        return connectionHandler.getIcon();
    }

    @Override
    public Navigatable getNavigatable() {
        return connectionHandler.getObjectBundle();
    }
}
