package com.dci.intellij.dbn.data.model.basic;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.data.find.DataSearchResult;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.DataModel;
import com.dci.intellij.dbn.data.model.DataModelCell;
import com.dci.intellij.dbn.data.model.DataModelHeader;
import com.dci.intellij.dbn.data.model.DataModelListener;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.dci.intellij.dbn.data.model.DataModelState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BasicDataModel<T extends DataModelRow> implements DataModel<T> {
    private DataModelHeader header;
    private DataModelState state;
    private Set<TableModelListener> tableModelListeners = new HashSet<TableModelListener>();
    private Set<ListDataListener> listDataListeners = new HashSet<ListDataListener>();
    private Set<DataModelListener> dataModelListeners = new HashSet<DataModelListener>();
    private List<T> rows = new ArrayList<T>();
    private Project project;
    private Filter<T> filter;

    private DataSearchResult searchResult;
    RegionalSettings regionalSettings;

    public BasicDataModel(Project project) {
        this.project = project;
        this.regionalSettings = RegionalSettings.getInstance(project);
    }


    public RegionalSettings getRegionalSettings() {
        return regionalSettings;
    }

    @Override
    public boolean isReadonly() {
        return true;
    }

    public Project getProject() {
        return project;
    }

    public void setHeader(@NotNull DataModelHeader header) {
        this.header = header;
        Disposer.register(this, header);
    }

    public DataModelHeader getHeader() {
        return header;
    }

    @NotNull
    public DataModelState getState() {
        if (state == null) {
            state = createState();
        }
        return state;
    }

    public void setState(DataModelState state) {
        this.state = state;
    }

    @Override
    public void setFilter(Filter<T> filter) {
        if (filter == null) {
            if (rows instanceof FiltrableList) {
                FiltrableList<T> filtrableList = (FiltrableList<T>) rows;
                rows = filtrableList.getFullList();
            }
        }
        else {
            FiltrableList<T> filtrableList;
            if (rows instanceof FiltrableList) {
                filtrableList = (FiltrableList<T>) rows;
            } else {
                filtrableList = new FiltrableList<T>(rows);
                rows = filtrableList;
            }
            filtrableList.setFilter(filter);
        }
        this.filter = filter;
    }

    @Nullable
    @Override
    public Filter<T> getFilter() {
        return filter;
    }

    protected DataModelState createState() {
        return new DataModelState();
    }

    @Override
    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        if (filter != null) {
            this.rows = new FiltrableList<T>(rows, filter);
        } else {
            this.rows = rows;
        }

        getState().setRowCount(getRowCount());
    }

    public void addRow(T row) {
        rows.add(row);
        getState().setRowCount(getRowCount());
    }

    public void addRowAtIndex(int index, T row) {
        rows.add(index, row);
        updateRowIndexes(index);
        getState().setRowCount(getRowCount());
    }

    public void removeRowAtIndex(int index) {
        DataModelRow row = rows.remove(index);
        updateRowIndexes(index);
        getState().setRowCount(getRowCount());

        Disposer.dispose(row);
    }

    public T getRowAtIndex(int index) {
        // model may be reloading when this is called, hence
        // IndexOutOfBoundsException is thrown if the range is not checked
        return index > -1 && rows.size() > index ? rows.get(index) : null;
    }

    public DataModelCell getCellAt(int rowIndex, int columnIndex) {
        return rows.get(rowIndex).getCellAtIndex(columnIndex);
    }

    public ColumnInfo getColumnInfo(int columnIndex) {
        return getHeader().getColumnInfo(columnIndex);
    }

    public int indexOfRow(T row) {
        return rows.indexOf(row);
    }

    protected void updateRowIndexes(int startIndex) {
        updateRowIndexes(rows, startIndex);
    }

    protected void updateRowIndexes(List<T> rows, int startIndex) {
        for (int i=startIndex; i<rows.size();i++) {
            rows.get(i).setIndex(i);
        }
    }

    /*********************************************************
     *                 Listener notifiers                    *
     *********************************************************/
    public void notifyCellUpdated(int rowIndex, int columnIndex) {
        TableModelEvent tableModelEvent = new TableModelEvent(this, rowIndex, rowIndex, columnIndex);
        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, rowIndex, rowIndex);
        notifyListeners(listDataEvent, tableModelEvent);
    }

    public void notifyRowUpdated(int rowIndex) {
        TableModelEvent tableModelEvent = new TableModelEvent(this, rowIndex, rowIndex);
        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, rowIndex, rowIndex);
        notifyListeners(listDataEvent, tableModelEvent);
    }

    public void notifyRowsDeleted(int fromRowIndex, int toRowIndex) {
        TableModelEvent tableModelEvent = new TableModelEvent(this, fromRowIndex, toRowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, fromRowIndex, toRowIndex);
        notifyListeners(listDataEvent, tableModelEvent);
    }

    public void notifyRowsUpdated(int fromRowIndex, int toRowIndex) {
        TableModelEvent tableModelEvent = new TableModelEvent(this, fromRowIndex, toRowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE);
        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, fromRowIndex, toRowIndex);
        notifyListeners(listDataEvent, tableModelEvent);
    }

    public void notifyRowsInserted(int fromRowIndex, int toRowIndex) {
        TableModelEvent tableModelEvent = new TableModelEvent(this, fromRowIndex, toRowIndex, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, fromRowIndex, toRowIndex);
        notifyListeners(listDataEvent, tableModelEvent);
    }

    private void notifyListeners(final ListDataEvent listDataEvent, final TableModelEvent event) {
        new ConditionalLaterInvocator() {
            public void execute() {
                for (ListDataListener listDataListener : listDataListeners) {
                    listDataListener.contentsChanged(listDataEvent);
                }

                for (TableModelListener tableModelListener: tableModelListeners) {
                    tableModelListener.tableChanged(event);
                }

                for (DataModelListener tableModelListener: dataModelListeners) {
                    tableModelListener.modelChanged();
                }
            }
        }.start();
    }

    /*********************************************************
     *                   ListModel (for gutter)              *
     *********************************************************/
    public int getSize() {
        return getRowCount();
    }

    public Object getElementAt(int index) {
        return getRowAtIndex(index);
    }

    public void addListDataListener(ListDataListener l) {
        listDataListeners.add(l);
    }

    public void removeListDataListener(ListDataListener l) {
        listDataListeners.remove(l);
    }

    /*********************************************************
     *                     DataModel                        *
     *********************************************************/
    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return disposed ? 0 : getHeader().getColumnCount();
    }

    public String getColumnName(int columnIndex) {
        return getHeader().getColumnName(columnIndex);
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        // model may be reloading when this is called, hence
        // IndexOutOfBoundsException is thrown if the range is not checked
        return rows.size() > rowIndex && columnIndex > -1 ? rows.get(rowIndex).getCellAtIndex(columnIndex) : null;
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {}

    public void addTableModelListener(TableModelListener listener) {
        tableModelListeners.add(listener);
    }

    public void removeTableModelListener(TableModelListener listener) {
        tableModelListeners.remove(listener);
    }

    public void addDataModelListener(DataModelListener listener) {
        dataModelListeners.add(listener);
    }

    @Override
    public void removeDataModelListener(DataModelListener listener) {
        dataModelListeners.remove(listener);
    }

    @Override
    public DataSearchResult getSearchResult() {
        if (searchResult == null) {
            searchResult = new DataSearchResult();

            Disposer.register(this, searchResult);
        }
        return searchResult;
    }
    
    @Override
    public boolean hasSearchResult() {
        return searchResult != null && !searchResult.isEmpty();
    }

    @Override
    public int getColumnIndex(String columnName) {
        return header.getColumnIndex(columnName);
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        if (!disposed) {
            disposed = true;
            DisposerUtil.dispose(rows);
            header = null;
            tableModelListeners.clear();
            listDataListeners.clear();
            searchResult = null;
            regionalSettings = null;
            project = null;
        }
    }
}
