package com.dci.intellij.dbn.object.factory;

import com.dci.intellij.dbn.object.common.DBObjectType;

import java.util.List;

public abstract class ObjectFactoryInput {
    private String objectName;
    private DBObjectType objectType;
    private ObjectFactoryInput parent;
    private int index;

    protected ObjectFactoryInput(String objectName, DBObjectType objectType, ObjectFactoryInput parent, int index) {
        this.objectName = objectName == null ? "" : objectName.trim();
        this.objectType = objectType;
        this.parent = parent;
        this.index = index;
    }

    public String getObjectName() {
        return objectName;
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    public int getIndex() {
        return index;
    }

    public ObjectFactoryInput getParent() {
        return parent;
    }

    public abstract void validate(List<String> errors);
}
