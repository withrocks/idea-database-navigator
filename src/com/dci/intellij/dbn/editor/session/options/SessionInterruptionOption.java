package com.dci.intellij.dbn.editor.session.options;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.option.InteractiveOption;

public enum SessionInterruptionOption implements InteractiveOption{
    ASK("Ask", null),
    NORMAL("Normal", null),
    IMMEDIATE("Immediate", null),
    POST_TRANSACTION("Post Transaction", null),
    CANCEL("Cancel", null);

    private String name;
    private Icon icon;

    SessionInterruptionOption(String name, Icon icon) {
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

    @Override
    public boolean isCancel() {
        return this == CANCEL;
    }

    @Override
    public boolean isAsk() {
        return this == ASK;
    }
}
