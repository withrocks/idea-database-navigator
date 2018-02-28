package com.dci.intellij.dbn.browser.options;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

public enum BrowserDisplayMode implements Presentable{

    @Deprecated SINGLE("Single tree"),
    SIMPLE("Single tree"),
    TABBED("Multiple connection tabs");

    private String name;

    BrowserDisplayMode(String name) {
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
