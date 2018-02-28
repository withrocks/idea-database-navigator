package com.dci.intellij.dbn.object.properties;

import javax.swing.Icon;

import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.pom.Navigatable;

public class DBObjectPresentableProperty extends PresentableProperty{
    private DBObjectRef objectRef;
    private boolean qualified = false;
    private String name;


    public DBObjectPresentableProperty(String name, DBObject object, boolean qualified) {
        this.objectRef = object.getRef();
        this.qualified = qualified;
        this.name = name;
    }

    public DBObjectPresentableProperty(DBObject object, boolean qualified) {
        this.objectRef = object.getRef();
        this.qualified = qualified;
    }

    public DBObjectPresentableProperty(DBObject object) {
        this.objectRef = object.getRef();
    }

    public String getName() {
        return name == null ? NamingUtil.capitalize(objectRef.getObjectType().getName()) : name;
    }

    public String getValue() {
        return qualified ? objectRef.getPath() : objectRef.getObjectName();
    }

    public Icon getIcon() {
        DBObject object = objectRef.get();
        return object == null ? null : object.getIcon();
    }

    @Override
    public Navigatable getNavigatable() {
        return objectRef.get();
    }
}
