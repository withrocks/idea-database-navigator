package com.dci.intellij.dbn.execution.compiler;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.option.InteractiveOption;

public enum CompileDependenciesOption implements InteractiveOption {
    YES("Yes", true),
    NO("No", true),
    ASK("Ask", false);

    private String name;
    private boolean persistable;

    CompileDependenciesOption(String name, boolean persistable) {
        this.name = name;
        this.persistable = persistable;
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
    public boolean isCancel() {
        return false;
    }

    @Override
    public boolean isAsk() {
        return this == ASK;
    }


    public static CompileDependenciesOption get(String name) {
        for (CompileDependenciesOption compileDependenciesOption : CompileDependenciesOption.values()) {
            if (compileDependenciesOption.name.equals(name) || compileDependenciesOption.name().equals(name)) {
                return compileDependenciesOption;
            }
        }
        return null;
    }}
