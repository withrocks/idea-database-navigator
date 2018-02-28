package com.dci.intellij.dbn.common.ui.table;

import javax.swing.ListCellRenderer;

public class IndexTableGutter<T extends DBNTable> extends DBNTableGutter<T>{
    public IndexTableGutter(T table) {
        super(table);
    }

    @Override
    protected ListCellRenderer createCellRenderer() {
        return new IndexTableGutterCellRenderer();
    }
}
