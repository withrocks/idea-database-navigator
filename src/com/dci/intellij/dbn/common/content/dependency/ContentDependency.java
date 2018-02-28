package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;

public abstract class ContentDependency implements Disposable {
    protected long changeTimestamp;

    @NotNull
    public abstract DynamicContent getSourceContent();

    public void reset() {
        changeTimestamp = getSourceContent().getChangeTimestamp();
    }

    public boolean isDirty() {
        return changeTimestamp != getSourceContent().getChangeTimestamp();
    }
}
