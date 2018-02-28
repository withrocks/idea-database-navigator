package com.dci.intellij.dbn.object.common.sorting;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public enum SortingType implements Presentable{
    NAME("Name"),
    POSITION("Position");
    private String name;

    SortingType(String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }


    @Override
    public String toString() {
        return name;
    }
}
