package com.dci.intellij.dbn.connection.transaction.ui;

import com.dci.intellij.dbn.common.ui.table.DBNTableModel;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.transaction.UncommittedChange;
import com.dci.intellij.dbn.connection.transaction.UncommittedChangeBundle;
import com.intellij.openapi.project.Project;

import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelListener;

public class UncommittedChangesTableModel implements DBNTableModel {
    private ConnectionHandler connectionHandler;

    public UncommittedChangesTableModel(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public Project getProject() {
        return connectionHandler.getProject();
    }

    public int getRowCount() {
        UncommittedChangeBundle uncommittedChanges = connectionHandler.getUncommittedChanges();
        return uncommittedChanges == null ? 0 : uncommittedChanges.size();
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int columnIndex) {
        return
            columnIndex == 0 ? "File" :
            columnIndex == 1 ? "Details" : null ;
    }

    public Class<?> getColumnClass(int columnIndex) {
        return UncommittedChange.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return connectionHandler.getUncommittedChanges().getChanges().get(rowIndex);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {}
    public void addTableModelListener(TableModelListener l) {}
    public void removeTableModelListener(TableModelListener l) {}
    @Override public int getSize() {return 0;}
    @Override public Object getElementAt(int index) {return null;}
    @Override public void addListDataListener(ListDataListener l) {}
    @Override public void removeListDataListener(ListDataListener l) {}

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
            connectionHandler = null;
        }
    }

}
