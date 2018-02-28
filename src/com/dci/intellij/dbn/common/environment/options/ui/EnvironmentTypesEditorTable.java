package com.dci.intellij.dbn.common.environment.options.ui;

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
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.dci.intellij.dbn.common.environment.EnvironmentTypeBundle;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColorChooser;
import com.intellij.ui.TableUtil;
import com.intellij.util.ui.UIUtil;

public class EnvironmentTypesEditorTable extends DBNTable<EnvironmentTypesTableModel> {

    public EnvironmentTypesEditorTable(Project project, EnvironmentTypeBundle environmentTypes) {
        super(project, new EnvironmentTypesTableModel(project, environmentTypes), true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        getSelectionModel().addListSelectionListener(selectionListener);
        setSelectionBackground(UIUtil.getTableBackground());
        setSelectionForeground(UIUtil.getTableForeground());
        setCellSelectionEnabled(true);
        setDefaultRenderer(Object.class, new EnvironmentTypesTableCellRenderer());
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(2).setMaxWidth(50);

        addMouseListener(mouseListener);
    }

    public void setEnvironmentTypes(EnvironmentTypeBundle environmentTypes) {
        super.setModel(new EnvironmentTypesTableModel(getProject(), environmentTypes));
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(2).setMaxWidth(50);
    }

    MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                Point point = e.getPoint();
                int columnIndex = columnAtPoint(point);
                if (columnIndex == 2) {
                    int rowIndex = rowAtPoint(point);
                    Color color = (Color) getValueAt(rowIndex, columnIndex);
                    color = ColorChooser.chooseColor(EnvironmentTypesEditorTable.this, "Select environment color", color);
                    if (color != null) {
                        setValueAt(color, rowIndex, columnIndex);
                    }
                }
            }
        }
    };    
    
    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        Object value = getValueAtMouseLocation();
        if (value instanceof Color) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    

    ListSelectionListener selectionListener = new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
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

    @Override
    public boolean isCellEditable(int row, int column) {
        return column < 2;
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

        new SimpleLaterInvocator() {
            public void execute() {
                textField.selectAll();
                textField.grabFocus();
            }
        }.start();
        selectCell(rowIndex, columnIndex);
        return textField;
    }

    public void insertRow() {
        stopCellEditing();
        int rowIndex = getSelectedRow();
        rowIndex = getModel().getRowCount() == 0 ? 0 : rowIndex + 1;
        getModel().insertRow(rowIndex);
        getSelectionModel().setSelectionInterval(rowIndex, rowIndex);

        revalidate();
        repaint();
    }


    public void removeRow() {
        stopCellEditing();
        int selectedRow = getSelectedRow();
        getModel().removeRow(selectedRow);

        revalidate();
        repaint();

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
