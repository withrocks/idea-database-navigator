package com.dci.intellij.dbn.object.dependency;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

public enum ObjectDependencyType implements Presentable{
    OUTGOING("Outgoing references (objects depending on this)"),
    INCOMING("Incoming references (objects this depends on)");

    private String name;

    ObjectDependencyType(String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }


}
