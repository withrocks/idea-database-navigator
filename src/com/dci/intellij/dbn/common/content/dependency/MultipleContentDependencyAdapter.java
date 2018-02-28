package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;

import java.util.ArrayList;
import java.util.List;

public class MultipleContentDependencyAdapter extends BasicDependencyAdapter implements ContentDependencyAdapter {
    private List<ContentDependency> dependencies;

    public MultipleContentDependencyAdapter(DynamicContent... sourceContents) {
        for (DynamicContent sourceContent : sourceContents) {
            if (sourceContent != null) {
                if (dependencies == null) dependencies = new ArrayList<ContentDependency>();
                dependencies.add(new BasicContentDependency(sourceContent));
            }
        }
    }

    public boolean canLoad(ConnectionHandler connectionHandler) {
        if (dependencies != null && canConnect(connectionHandler)) {
            for (ContentDependency dependency : dependencies) {
                if (!dependency.getSourceContent().isLoaded()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean isDirty() {
        if (dependencies != null) {
            for (ContentDependency dependency : dependencies) {
                if (dependency.isDirty()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canLoadFast() {
        if (dependencies != null) {
            for (ContentDependency dependency : dependencies) {
                if (!dependency.getSourceContent().isLoaded()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void beforeLoad() {
        // assuming all dependencies are hard, load them first
        if (dependencies != null) {
            for (ContentDependency dependency : dependencies) {
                dependency.getSourceContent().load(false);
            }
        }
    }

    @Override
    public void afterLoad() {
        if (dependencies != null) {
            for (ContentDependency dependency : dependencies) {
                dependency.reset();
            }
        }
    }

    @Override
    public void beforeReload(DynamicContent dynamicContent) {
        beforeLoad();
    }

    @Override
    public void afterReload(DynamicContent dynamicContent) {
        afterLoad();
    }

    @Override
    public void dispose() {
        DisposerUtil.dispose(dependencies);
        dependencies = null;
        super.dispose();
    }
}
