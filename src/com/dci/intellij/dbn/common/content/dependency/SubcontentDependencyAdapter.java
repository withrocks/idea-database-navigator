package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;

public interface SubcontentDependencyAdapter extends ContentDependencyAdapter{
    DynamicContent getSourceContent();
}
