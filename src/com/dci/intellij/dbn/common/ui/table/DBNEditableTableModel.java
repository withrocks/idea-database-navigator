package com.dci.intellij.dbn.common.ui.table;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.HashSet;
import java.util.Set;

public abstract class DBNEditableTableModel implements DBNTableModel {
    private Set<TableModelListener> tableModelListeners = new HashSet<TableModelListener>();
    private Set<ListDataListener> listDataListeners = new HashSet<ListDataListener>();

    public void addTableModelListener(TableModelListener listener) {
        tableModelListeners.add(listener);
    }

    public void removeTableModelListener(TableModelListener listener) {
        tableModelListeners.remove(listener);
    }

    @Override
    public void addListDataListener(ListDataListener listener) {
        listDataListeners.add(listener);
    }

    @Override
    public void removeListDataListener(ListDataListener listener) {
        listDataListeners.remove(listener);
    }

    public abstract void insertRow(int rowIndex);

    public abstract void removeRow(int rowIndex);

    public void notifyListeners(int firstRowIndex, int lastRowIndex, int columnIndex) {
        TableModelEvent modelEvent = new TableModelEvent(this, firstRowIndex, lastRowIndex, columnIndex);
        for (TableModelListener listener : tableModelListeners) {
            listener.tableChanged(modelEvent);
        }

        ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, firstRowIndex, lastRowIndex);
        for (ListDataListener listDataListener : listDataListeners) {
            listDataListener.contentsChanged(listDataEvent);
        }
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
            tableModelListeners.clear();
        }
    }
}
