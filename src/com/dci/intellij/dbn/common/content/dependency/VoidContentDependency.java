package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.VoidDynamicContent;
import org.jetbrains.annotations.NotNull;

public class VoidContentDependency extends ContentDependency{
    public static final VoidContentDependency INSTANCE = new VoidContentDependency();

    private VoidContentDependency() {

    }

    @NotNull
    @Override
    public DynamicContent getSourceContent() {
        return VoidDynamicContent.INSTANCE;
    }

    @Override
    public void dispose() {

    }
}
