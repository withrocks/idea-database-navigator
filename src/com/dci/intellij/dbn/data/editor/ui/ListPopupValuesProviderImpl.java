package com.dci.intellij.dbn.data.editor.ui;

import java.util.Collections;
import java.util.List;

public abstract class ListPopupValuesProviderImpl implements ListPopupValuesProvider{
    private String description;
    private boolean longLoading;

    public ListPopupValuesProviderImpl(String description, boolean longLoading) {
        this.description = description;
        this.longLoading = longLoading;
    }

    @Override
    public final String getDescription() {
        return description;
    }

    @Override
    public abstract List<String> getValues();

    @Override
    public List<String> getSecondaryValues() {
        return Collections.emptyList();
    }

    @Override
    public final boolean isLongLoading() {
        return longLoading;
    }
}
