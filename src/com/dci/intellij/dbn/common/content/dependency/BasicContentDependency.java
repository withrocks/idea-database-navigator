package com.dci.intellij.dbn.common.content.dependency;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.VoidDynamicContent;

public class BasicContentDependency extends ContentDependency {
    private DynamicContent sourceContent;

    public BasicContentDependency(@NotNull DynamicContent sourceContent) {
        this.sourceContent = sourceContent;
        reset();
    }

    @NotNull
    @Override
    public DynamicContent getSourceContent() {
        return sourceContent == null ? VoidDynamicContent.INSTANCE : sourceContent;
    }

    public void dispose() {
        sourceContent = null;
    }
}
