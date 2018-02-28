package com.dci.intellij.dbn.data.model;

import com.dci.intellij.dbn.data.editor.ui.UserValueHolder;
import com.intellij.openapi.Disposable;

public interface DataModelCell extends Disposable, UserValueHolder {
    ColumnInfo getColumnInfo();

    int getIndex();

    DataModelRow getRow();

    boolean isDisposed();
}
