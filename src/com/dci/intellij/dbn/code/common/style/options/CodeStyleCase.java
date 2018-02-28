package com.dci.intellij.dbn.code.common.style.options;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public enum CodeStyleCase implements Presentable{
    PRESERVE ("Preserve case"),
    UPPER("Upper case"),
    LOWER("Lower case"),
    CAPITALIZED("Capitalized");

    private String name;

    private CodeStyleCase(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
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
}
