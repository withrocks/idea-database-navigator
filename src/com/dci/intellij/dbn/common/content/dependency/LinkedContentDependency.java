package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.VoidDynamicContent;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import org.jetbrains.annotations.NotNull;

public class LinkedContentDependency extends ContentDependency {
    private GenericDatabaseElement sourceContentOwner;
    private DynamicContentType sourceContentType;

    public LinkedContentDependency(GenericDatabaseElement sourceContentOwner, DynamicContentType sourceContentType) {
        this.sourceContentOwner = sourceContentOwner;
        this.sourceContentType = sourceContentType;
        reset();
    }

    @NotNull
    public DynamicContent getSourceContent() {
        if (sourceContentOwner != null) {
            DynamicContent sourceContent = sourceContentOwner.getDynamicContent(sourceContentType);
            if (sourceContent != null) {
                return sourceContent;
            }
        }
        return VoidDynamicContent.INSTANCE;
    }

    public void dispose() {
        sourceContentOwner = null;
    }
}
