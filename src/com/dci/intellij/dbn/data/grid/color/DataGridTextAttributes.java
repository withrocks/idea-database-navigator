package com.dci.intellij.dbn.data.grid.color;

import java.awt.Color;

import com.intellij.ui.SimpleTextAttributes;

public interface DataGridTextAttributes {
    void load();

    SimpleTextAttributes getSelection();

    SimpleTextAttributes getSearchResult();

    Color getCaretRowBgColor();

    SimpleTextAttributes getPlainData(boolean modified, boolean atCaretRow);

    SimpleTextAttributes getLoadingData(boolean atCaretRow);
}
