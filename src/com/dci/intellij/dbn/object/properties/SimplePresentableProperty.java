package com.dci.intellij.dbn.object.properties;

import com.intellij.pom.Navigatable;

import javax.swing.*;

public class SimplePresentableProperty extends PresentableProperty{
    private String name;
    private String value;
    private Icon icon;

    public SimplePresentableProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public SimplePresentableProperty(String name, String value, Icon icon) {
        this.name = name;
        this.value = value;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public Icon getIcon() {
        return icon;
    }

    public Navigatable getNavigatable() {
        return null;
    }
}
