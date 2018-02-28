package com.dci.intellij.dbn.execution.method.result.ui;

import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelListener;
import java.util.List;

import com.dci.intellij.dbn.common.ui.table.DBNTableModel;
import com.dci.intellij.dbn.execution.method.ArgumentValue;
import com.intellij.openapi.project.Project;

@Deprecated
public class ArgumentValuesTableModel implements DBNTableModel {
    private List<ArgumentValue> argumentValues;
    private Project project;


    public ArgumentValuesTableModel(Project project, List<ArgumentValue> argumentValues) {
        this.argumentValues = argumentValues;
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public int getRowCount() {
        int count = 0;
        for (ArgumentValue argumentValue : argumentValues) {
            if (!argumentValue.isCursor()) {
                count++;
            }
        }
        return count;
    }

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int columnIndex) {
        return
            columnIndex == 0 ? "Argument" :
            columnIndex == 1 ? "Value" : null ;
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        int index = 0;
        ArgumentValue resultArgumentValue = null;
        for (ArgumentValue argumentValue : argumentValues) {
            if (!argumentValue.isCursor()) {
                if (index == rowIndex) {
                    resultArgumentValue = argumentValue;
                    break;
                }
                index++;
            }
        }
        return
            resultArgumentValue == null ? null :
            columnIndex == 0 ? resultArgumentValue :
            columnIndex == 1 ? resultArgumentValue.getValue() : null;
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

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            project = null;
        }
    }
}
