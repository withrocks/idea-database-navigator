package com.dci.intellij.dbn.common.ui;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TextPresentable implements Presentable{
    private String text;
    public TextPresentable(String text) {
        this.text = text;
    }

    @NotNull
    @Override
    public String getName() {
        return text;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }
}
