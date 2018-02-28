package com.dci.intellij.dbn.common.content;

import com.dci.intellij.dbn.common.content.dependency.BasicDependencyAdapter;
import com.dci.intellij.dbn.common.content.dependency.ContentDependencyAdapter;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoader;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.intellij.openapi.project.Project;

public class SimpleDynamicContent<T extends DynamicContentElement> extends DynamicContentImpl<T> {
    private static ContentDependencyAdapter DEPENDENCY_ADAPTER = new BasicDependencyAdapter();

    public SimpleDynamicContent(GenericDatabaseElement parent, DynamicContentLoader<T> loader, boolean indexed) {
        super(parent, loader, DEPENDENCY_ADAPTER, indexed);
    }

    @Override
    public Filter getFilter() {
        return null;
    }

    public void notifyChangeListeners() {

    }

    public Project getProject() {
        return null;
    }

    public String getContentDescription() {
        return null;
    }

    public String getName() {
        return null;
    }
}
