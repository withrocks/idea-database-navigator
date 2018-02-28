package com.dci.intellij.dbn.data.model;

import javax.swing.ListModel;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.ui.table.DBNTableModel;
import com.dci.intellij.dbn.data.find.DataSearchResult;
import com.intellij.openapi.project.Project;

public interface DataModel<T extends DataModelRow> extends DBNTableModel, ListModel {
    boolean isReadonly();

    Project getProject();

    void setFilter(Filter<T> filter);

    @Nullable
    Filter<T> getFilter();

    List<T> getRows();

    int indexOfRow(T row);

    T getRowAtIndex(int index);

    DataModelHeader getHeader();

    int getColumnCount();

    ColumnInfo getColumnInfo(int columnIndex);

    @NotNull
    DataModelState getState();

    void setState(DataModelState state);

    DataSearchResult getSearchResult();

    void addDataModelListener(DataModelListener listener);

    void removeDataModelListener(DataModelListener listener);

    boolean hasSearchResult();

    int getColumnIndex(String columnName);
}
