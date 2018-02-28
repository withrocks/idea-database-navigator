package com.dci.intellij.dbn.data.editor.ui;

import com.dci.intellij.dbn.data.editor.text.TextContentType;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.project.Project;

public class UserValueHolderImpl<T> implements UserValueHolder<T>{
    private String name;
    private DBDataType dataType;
    private DBObjectType objectType;
    private Project project;
    private T userValue;
    private TextContentType contentType;

    public UserValueHolderImpl(String name, DBObjectType objectType, DBDataType dataType, Project project) {
        this.name = name;
        this.objectType = objectType;
        this.dataType = dataType;
        this.project = project;
    }

    public T getUserValue() {
        return userValue;
    }

    @Override
    public String getFormattedUserValue() {
        throw new UnsupportedOperationException();
    }

    public void setUserValue(T userValue) {
        this.userValue = userValue;
    }

    public void updateUserValue(T userValue, boolean bulk) {
        this.userValue = userValue;
    }

    public TextContentType getContentType() {
        return contentType;
    }

    public void setContentType(TextContentType contentType) {
        this.contentType = contentType;
    }

    public String getName() {
        return name;
    }

    @Override
    public DBObjectType getObjectType() {
        return objectType;
    }

    @Override
    public DBDataType getDataType() {
        return dataType;
    }

    public Project getProject() {
        return project;
    }
}
