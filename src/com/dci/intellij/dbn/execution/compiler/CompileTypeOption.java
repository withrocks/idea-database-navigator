package com.dci.intellij.dbn.execution.compiler;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.option.InteractiveOption;

public enum CompileTypeOption implements InteractiveOption {
    NORMAL("Normal", Icons.OBEJCT_COMPILE, true),
    DEBUG("Debug", Icons.OBEJCT_COMPILE_DEBUG, true),
    KEEP("Keep existing", null/*Icons.OBEJCT_COMPILE_KEEP*/, true),
    ASK("Ask", null/*Icons.OBEJCT_COMPILE_ASK*/, false);

    private String name;
    private Icon icon;
    private boolean persistable;

    CompileTypeOption(String name, Icon icon, boolean persistable) {
        this.name = name;
        this.icon = icon;
        this.persistable = persistable;
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
        return false;
    }

    @Override
    public boolean isAsk() {
        return this == ASK;
    }


    public static CompileTypeOption get(String name) {
        for (CompileTypeOption compileType : CompileTypeOption.values()) {
            if (compileType.name.equals(name) || compileType.name().equals(name)) {
                return compileType;
            }
        }
        return null;
    }}
