package com.dci.intellij.dbn.data.grid.options;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

public enum NullSortingOption implements Presentable{
    FIRST("FIRST"),
    LAST("LAST");

    String name;

    NullSortingOption(String name) {
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
