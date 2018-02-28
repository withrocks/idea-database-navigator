package com.dci.intellij.dbn.common.ui;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Presentable {
    @NotNull
    String getName();

    @Nullable
    Icon getIcon();
}
