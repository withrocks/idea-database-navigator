package com.dci.intellij.dbn.data.find;

import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTable;
import com.dci.intellij.dbn.data.model.basic.BasicDataModel;

public interface SearchableDataComponent {
    void showSearchHeader();
    void hideSearchHeader();
    void cancelEditActions();
    String getSelectedText();
    BasicTable<? extends BasicDataModel> getTable();
}
