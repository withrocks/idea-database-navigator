package com.dci.intellij.dbn.common.locale;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

public enum DBNumberFormat implements Presentable{
    GROUPED("Grouped"),
    UNGROUPED("Ungrouped"),
    CUSTOM("Custom");

    private String name;

    DBNumberFormat(String name) {
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
