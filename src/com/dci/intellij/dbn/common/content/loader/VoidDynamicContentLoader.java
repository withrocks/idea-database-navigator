package com.dci.intellij.dbn.common.content.loader;

import com.dci.intellij.dbn.common.content.DynamicContent;

public class VoidDynamicContentLoader implements DynamicContentLoader{

    public static final VoidDynamicContentLoader INSTANCE = new VoidDynamicContentLoader();

    private VoidDynamicContentLoader() {

    }

    @Override
    public void loadContent(DynamicContent dynamicContent, boolean forceReload) throws DynamicContentLoadException {
        // do nothing
    }

    @Override
    public void reloadContent(DynamicContent dynamicContent) throws DynamicContentLoadException {
        // do nothing
    }
}
