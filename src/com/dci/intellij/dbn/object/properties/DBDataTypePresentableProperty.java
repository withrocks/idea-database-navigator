package com.dci.intellij.dbn.object.properties;

import javax.swing.Icon;

import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.object.DBType;
import com.intellij.pom.Navigatable;

public class DBDataTypePresentableProperty extends PresentableProperty{
    private DBDataType dataType;
    private String name = "Data type";

    public DBDataTypePresentableProperty(String name, DBDataType dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    public DBDataTypePresentableProperty(DBDataType dataType) {
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return dataType.getQualifiedName();
    }

    public Icon getIcon() {
        DBType declaredType = dataType.getDeclaredType();
        return declaredType == null ? null : declaredType.getIcon();
    }

    @Override
    public Navigatable getNavigatable() {
        return dataType.getDeclaredType();
    }
}
