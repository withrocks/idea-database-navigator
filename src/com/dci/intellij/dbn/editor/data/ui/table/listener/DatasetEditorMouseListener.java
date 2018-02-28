package com.dci.intellij.dbn.editor.data.ui.table.listener;

import com.dci.intellij.dbn.common.ui.MouseUtil;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterInput;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelCell;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.dci.intellij.dbn.object.DBColumn;
import com.intellij.openapi.Disposable;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DatasetEditorMouseListener extends MouseAdapter implements Disposable {
    private DatasetEditorTable table;

    public DatasetEditorMouseListener(DatasetEditorTable table) {
        this.table = table;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    public void mouseReleased(final MouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON3) {
            Point mousePoint = event.getPoint();
            DatasetEditorModelCell cell = (DatasetEditorModelCell) table.getCellAtLocation(mousePoint);
            if (cell != null) {

                if (table.getSelectedRowCount() <= 1 && table.getSelectedColumnCount() <= 1) {
                    table.cancelEditing();
                    boolean oldEditingSatus = table.isEditingEnabled();
                    table.setEditingEnabled(false);
                    table.selectCell(table.rowAtPoint(mousePoint), table.columnAtPoint(mousePoint));
                    table.setEditingEnabled(oldEditingSatus);
                }

                table.showPopupMenu(event, cell, cell.getColumnInfo());
            }
        }
    }

    public void mouseClicked(MouseEvent event) {
        if (MouseUtil.isNavigationEvent(event)) {
            DatasetEditorModelCell cell = (DatasetEditorModelCell) table.getCellAtLocation(event.getPoint());
            DBColumn column = cell.getColumnInfo().getColumn();

            if (column!= null && column.isForeignKey() && cell.getUserValue() != null) {
                table.clearSelection();
                DatasetFilterInput filterInput = table.getModel().resolveForeignKeyRecord(cell);
                if (filterInput.getColumns().size() > 0) {
                    DatasetEditorManager datasetEditorManager = DatasetEditorManager.getInstance(column.getProject());
                    datasetEditorManager.navigateToRecord(filterInput, event);
                    event.consume();
                }
            }
        }
    }

    @Override
    public void dispose() {
        table = null;
    }
}