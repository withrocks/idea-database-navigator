package com.dci.intellij.dbn.object.factory.ui.common;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.factory.ObjectFactoryInput;
import com.intellij.openapi.project.Project;

import javax.swing.JPanel;

public abstract class ObjectFactoryInputForm<T extends ObjectFactoryInput> extends DBNFormImpl {
    private int index;
    private ConnectionHandler connectionHandler;
    private DBObjectType objectType;

    protected ObjectFactoryInputForm(Project project, ConnectionHandler connectionHandler, DBObjectType objectType, int index) {
        super(project);
        this.connectionHandler = connectionHandler;
        this.objectType = objectType;
        this.index = index;
    }

    public abstract JPanel getComponent();

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    public abstract T createFactoryInput(ObjectFactoryInput parent);

    public abstract void focus();

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public void dispose() {
        super.dispose();
        connectionHandler = null;
        objectType = null;
    }
}
