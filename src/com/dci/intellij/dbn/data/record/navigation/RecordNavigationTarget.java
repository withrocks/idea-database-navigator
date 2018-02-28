package com.dci.intellij.dbn.data.record.navigation;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

public enum RecordNavigationTarget implements Presentable{
    VIEWER("Record Viewer", null),
    EDITOR("Table Editor", null),
    PROMPT("Ask", null);

    private String name;
    private Icon icon;

    RecordNavigationTarget(String name, Icon icon) {
        this.name = name;
        this.icon = icon;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return icon;
    }
}
