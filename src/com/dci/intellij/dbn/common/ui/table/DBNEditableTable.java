package com.dci.intellij.dbn.common.ui.table;

import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseEvent;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TableUtil;
import com.intellij.util.ui.UIUtil;

public class DBNEditableTable<T extends DBNEditableTableModel> extends DBNTable<T> {

    public DBNEditableTable(Project project, T model, boolean showHeader) {
        super(project, model, showHeader);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        getSelectionModel().addListSelectionListener(selectionListener);
        setSelectionBackground(UIUtil.getTableBackground());
        setCellSelectionEnabled(true);
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        Object value = getValueAtMouseLocation();
        if (value instanceof Color) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    

    private ListSelectionListener selectionListener = new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting() && getSelectedRowCount() == 1) {
                startCellEditing();
            }
        }
    };

    public void columnSelectionChanged(ListSelectionEvent e) {
        super.columnSelectionChanged(e);
        JTableHeader tableHeader = getTableHeader();
        if (tableHeader != null && tableHeader.getDraggedColumn() == null) {
            if (!e.getValueIsAdjusting()) {
                startCellEditing();
            }
        }
    }

    private void startCellEditing() {
        if (getModel().getRowCount() > 0) {
            int[] selectedRows = getSelectedRows();
            int selectedRow = selectedRows.length > 0 ? selectedRows[0] : 0;

            int[] selectedColumns = getSelectedColumns();
            int selectedColumn = selectedColumns.length > 0 ? selectedColumns[0] : 0;

            editCellAt(selectedRow, selectedColumn);
        }
    }
    
    @Override
    public void editingStopped(ChangeEvent e) {
        super.editingStopped(e);
        getModel().notifyListeners(0, getModel().getRowCount(), 0);
    }

    public Component prepareEditor(TableCellEditor editor, int rowIndex, int columnIndex) {
        final JTextField textField = (JTextField) super.prepareEditor(editor, rowIndex, columnIndex);
        textField.setBorder(new EmptyBorder(0,3,0,0));

        selectCell(rowIndex, columnIndex);
        new SimpleLaterInvocator() {
            public void execute() {
                textField.grabFocus();
                textField.selectAll();
            }
        }.start();
        return textField;
    }

    public void insertRow() {
        stopCellEditing();
        int rowIndex = getSelectedRow();
        rowIndex = getModel().getRowCount() == 0 ? 0 : rowIndex + 1;
        getModel().insertRow(rowIndex);
        resizeAndRepaint();
        getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
    }


    public void removeRow() {
        stopCellEditing();
        int selectedRow = getSelectedRow();
        getModel().removeRow(selectedRow);
        resizeAndRepaint();

        if (getModel().getRowCount() == selectedRow && selectedRow > 0) {
            getSelectionModel().setSelectionInterval(selectedRow -1, selectedRow -1);
        }
    }

    public void moveRowUp() {
        int selectedRow = getSelectedRow();
        int selectedColumn = getSelectedColumn();

        TableUtil.moveSelectedItemsUp(this);
        selectCell(selectedRow -1, selectedColumn);
    }

    public void moveRowDown() {
        int selectedRow = getSelectedRow();
        int selectedColumn = getSelectedColumn();

        TableUtil.moveSelectedItemsDown(this);
        selectCell(selectedRow + 1, selectedColumn);
    }
}
