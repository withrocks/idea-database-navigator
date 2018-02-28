package com.dci.intellij.dbn.common.locale;

import javax.swing.Icon;
import java.text.DateFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

public enum DBDateFormat implements Presentable {
    SHORT("Short", DateFormat.SHORT),
    MEDIUM("Medium", DateFormat.MEDIUM),
    LONG("Long", DateFormat.LONG),
    CUSTOM("Custom", 0);

    private int dateFormat;
    private String name;

    DBDateFormat(String name, int dateFormat) {
        this.name = name;
        this.dateFormat = dateFormat;
    }

    public int getDateFormat() {
        return dateFormat;
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
